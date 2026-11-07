package br.com.fiap.postech.e2e.it.cucumber.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Queries Tempo for distributed traces produced by the fleet.
 */
public final class TempoProbe {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TempoProbe() {
    }

    public static Optional<String> awaitTrace(String spanName, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            Optional<String> traceId = search(spanName);
            if (traceId.isPresent()) {
                return traceId;
            }
            sleep();
        }
        return Optional.empty();
    }

    public static Optional<String> search(String spanName) {
        String tags = URLEncoder.encode("name=\"" + spanName + "\"", StandardCharsets.UTF_8)
                .replace("+", "%20");
        JsonNode response = get("/api/search?tags=" + tags + "&limit=1");
        JsonNode traces = response.path("traces");
        if (traces.isArray() && !traces.isEmpty()) {
            return Optional.of(traces.get(0).path("traceID").asText());
        }
        return Optional.empty();
    }

    public static Set<String> servicesInTrace(String traceId) {
        JsonNode response = get("/api/traces/" + traceId);
        Set<String> services = new HashSet<>();
        for (JsonNode batch : response.path("batches")) {
            for (JsonNode attribute : batch.path("resource").path("attributes")) {
                if ("service.name".equals(attribute.path("key").asText())) {
                    services.add(attribute.path("value").path("stringValue").asText());
                }
            }
        }
        return services;
    }

    private static JsonNode get(String path) {
        try {
            HttpResponse<String> response = CLIENT.send(
                    HttpRequest.newBuilder(URI.create(ServiceUrls.tempo() + path))
                            .timeout(Duration.ofSeconds(10))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                return OBJECT_MAPPER.createObjectNode();
            }
            return OBJECT_MAPPER.readTree(response.body());
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Tempo call failed: " + path, exception);
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
