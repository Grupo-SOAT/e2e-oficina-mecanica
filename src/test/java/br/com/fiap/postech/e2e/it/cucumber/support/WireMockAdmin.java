package br.com.fiap.postech.e2e.it.cucumber.support;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Controls the Mercado Pago WireMock instance through its admin API. Only the external payment
 * provider is simulated; every workshop service is real.
 */
public final class WireMockAdmin {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WireMockAdmin() {
    }

    public static void resetMappings() {
        send("POST", "/__admin/mappings/reset", null);
    }

    public static void stubApprovedPayment(String paymentId, long serviceOrderId) {
        stubPayment(paymentId, "approved", serviceOrderId);
    }

    public static void stubRejectedPayment(String paymentId, long serviceOrderId) {
        stubPayment(paymentId, "rejected", serviceOrderId);
    }

    public static void stubPayment(String paymentId, String status, long serviceOrderId) {
        Map<String, Object> mapping = Map.of(
                "request", Map.of(
                        "method", "GET",
                        "url", "/v1/payments/" + paymentId),
                "response", Map.of(
                        "status", 200,
                        "headers", Map.of("Content-Type", "application/json"),
                        "jsonBody", Map.of(
                                "id", paymentId,
                                "status", status,
                                "external_reference", serviceOrderId)));
        send("POST", "/__admin/mappings", mapping);
    }

    private static void send(String method, String path, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(
                            URI.create(ServiceUrls.wireMock() + path))
                    .timeout(Duration.ofSeconds(10));
            if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(body)));
            }
            HttpResponse<String> response = CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("WireMock admin call failed: " + method + " " + path
                        + " -> " + response.statusCode() + " " + response.body());
            }
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("WireMock admin call failed: " + method + " " + path, exception);
        }
    }
}
