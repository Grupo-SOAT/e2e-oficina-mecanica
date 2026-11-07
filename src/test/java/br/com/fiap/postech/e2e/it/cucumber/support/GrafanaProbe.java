package br.com.fiap.postech.e2e.it.cucumber.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Queries the Grafana API for the provisioned dashboard and alert rules.
 */
public final class GrafanaProbe {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GrafanaProbe() {
    }

    public static JsonNode dashboard(String uid) {
        return get("/api/dashboards/uid/" + uid);
    }

    public static JsonNode alertRules() {
        return get("/api/v1/provisioning/alert-rules");
    }

    private static JsonNode get(String path) {
        try {
            HttpResponse<String> response = CLIENT.send(
                    HttpRequest.newBuilder(URI.create(ServiceUrls.grafana() + path))
                            .timeout(Duration.ofSeconds(10))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Grafana call failed: " + path + " -> " + response.statusCode());
            }
            return OBJECT_MAPPER.readTree(response.body());
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Grafana call failed: " + path, exception);
        }
    }
}
