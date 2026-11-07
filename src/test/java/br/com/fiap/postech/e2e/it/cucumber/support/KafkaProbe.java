package br.com.fiap.postech.e2e.it.cucumber.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Reads events from Kafka without participating in the services' consumer groups.
 */
public final class KafkaProbe {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules();
    private static final String BOOTSTRAP = System.getenv().getOrDefault(
            "E2E_KAFKA_BOOTSTRAP_SERVERS",
            System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));

    private KafkaProbe() {
    }

    public static Optional<JsonNode> await(
            String topic,
            Predicate<JsonNode> predicate,
            Duration timeout
    ) {
        long deadline = System.nanoTime() + timeout.toNanos();
        List<JsonNode> seen = new ArrayList<>();
        while (System.nanoTime() < deadline) {
            for (JsonNode event : read(topic, Duration.ofSeconds(2))) {
                seen.add(event);
                if (predicate.test(event)) {
                    return Optional.of(event);
                }
            }
            if (!seen.isEmpty()) {
                // Give the producer one more poll window; the predicate may match a later record.
                continue;
            }
        }
        return Optional.empty();
    }

    public static List<JsonNode> read(String topic, Duration timeout) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "e2e-probe-" + UUID.randomUUID());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        List<JsonNode> events = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            List<PartitionInfo> partitions = consumer.partitionsFor(topic);
            if (partitions == null || partitions.isEmpty()) {
                return events;
            }
            List<TopicPartition> topicPartitions = partitions.stream()
                    .map(partition -> new TopicPartition(topic, partition.partition()))
                    .toList();
            consumer.assign(topicPartitions);
            consumer.seekToBeginning(topicPartitions);

            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (record.value() != null && !record.value().isBlank()) {
                        events.add(OBJECT_MAPPER.readTree(record.value()));
                    }
                }
                if (records.isEmpty() && !events.isEmpty()) {
                    break;
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read topic " + topic, exception);
        }
        return events;
    }
}
