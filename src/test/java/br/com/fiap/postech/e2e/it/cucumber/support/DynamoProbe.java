package br.com.fiap.postech.e2e.it.cucumber.support;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.net.URI;
import java.util.Map;

/**
 * Clears the execution table between scenarios so queue assertions are deterministic.
 */
public final class DynamoProbe {

    private static final String ENDPOINT = System.getenv().getOrDefault(
            "DYNAMODB_ENDPOINT", "http://localhost:8000");
    private static final String TABLE = System.getenv().getOrDefault(
            "DYNAMODB_TABLE", "execution");

    private DynamoProbe() {
    }

    public static void clear() {
        try (DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("local", "local")))
                .httpClient(UrlConnectionHttpClient.create())
                .build()) {
            ScanResponse scan = client.scan(ScanRequest.builder().tableName(TABLE).build());
            for (Map<String, AttributeValue> item : scan.items()) {
                client.deleteItem(DeleteItemRequest.builder()
                        .tableName(TABLE)
                        .key(Map.of("PK", item.get("PK"), "SK", item.get("SK")))
                        .build());
            }
        } catch (ResourceNotFoundException ignored) {
            // The queue table is created by the fleet bootstrap; nothing to clear before it exists.
        }
    }
}
