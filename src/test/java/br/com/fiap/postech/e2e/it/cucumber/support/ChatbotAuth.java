package br.com.fiap.postech.e2e.it.cucumber.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Performs the chatbot authentication, which requires the {@code X-API-Key} header.
 */
public final class ChatbotAuth {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private ChatbotAuth() {
    }

    public static HttpSupport.Result login(String apiKey) {
        try {
            HttpResponse<byte[]> response = CLIENT.send(
                    HttpRequest.newBuilder(URI.create(ServiceUrls.baseUrl(ServiceUrls.Service.MNL)
                                    + "/auth/chatbot"))
                            .timeout(Duration.ofSeconds(15))
                            .header("Content-Type", "application/json")
                            .header("X-API-Key", apiKey)
                            .POST(HttpRequest.BodyPublishers.ofString("{}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            return new HttpSupport.Result(
                    response.statusCode(),
                    response.body(),
                    response.headers().firstValue("content-type").orElse(null),
                    response.headers().firstValue("content-disposition").orElse(null));
        } catch (Exception exception) {
            throw new IllegalStateException("Chatbot authentication failed", exception);
        }
    }
}
