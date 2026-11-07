package br.com.fiap.postech.e2e.it.cucumber.support;

import br.com.fiap.postech.e2e.it.cucumber.context.CucumberTestContext;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Thin HTTP client used by the step definitions. It talks to the running fleet over HTTP, never to
 * an in-process application context.
 */
public final class HttpSupport {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private HttpSupport() {
    }

    public record Result(int status, byte[] body, String contentType, String contentDisposition) {

        public String bodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }
    }

    public static Result get(ServiceUrls.Service service, String path, Map<String, String> params) {
        return send("GET", service, path, null, params, true);
    }

    /**
     * Plain-text GET used for actuator endpoints. It sends no Authorization header and asks for
     * text/plain so Spring does not reject the response for an unaccepted media type.
     */
    public static Result getPlain(ServiceUrls.Service service, String path) {
        CucumberTestContext context = CucumberTestContext.getInstance();
        try {
            HttpResponse<byte[]> response = CLIENT.send(
                    HttpRequest.newBuilder(URI.create(ServiceUrls.baseUrl(service) + path))
                            .timeout(Duration.ofSeconds(30))
                            .header("Accept", "text/plain")
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            Result result = new Result(response.statusCode(), response.body(),
                    response.headers().firstValue("content-type").orElse(null),
                    response.headers().firstValue("content-disposition").orElse(null));
            context.setLastStatusCode(result.status());
            context.setLastResponseBody(result.bodyAsString());
            context.setLastResponseBytes(result.body());
            context.setLastResponseContentType(result.contentType());
            return result;
        } catch (Exception exception) {
            context.setLastStatusCode(0);
            context.setLastResponseBody(exception.getMessage());
            return new Result(0, new byte[0], null, null);
        }
    }

    public static Result post(ServiceUrls.Service service, String path, String body) {
        return send("POST", service, path, body, Map.of(), true);
    }

    public static Result patch(ServiceUrls.Service service, String path, String body) {
        return send("PATCH", service, path, body, Map.of(), true);
    }

    public static Result delete(ServiceUrls.Service service, String path) {
        return send("DELETE", service, path, null, Map.of(), true);
    }

    public static Result send(
            String method,
            ServiceUrls.Service service,
            String path,
            String body,
            Map<String, String> params,
            boolean authenticated
    ) {
        CucumberTestContext context = CucumberTestContext.getInstance();
        URI uri = URI.create(ServiceUrls.baseUrl(service) + path + query(params));

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(90))
                .header("Accept", "application/json, application/pdf, text/csv");

        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (authenticated && context.getAuthToken() != null) {
            builder.header("Authorization", "Bearer " + context.getAuthToken());
        }
        if (context.getCorrelationId() != null) {
            builder.header("X-Correlation-Id", context.getCorrelationId());
        }

        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        try {
            HttpResponse<byte[]> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            Result result = new Result(
                    response.statusCode(),
                    response.body(),
                    response.headers().firstValue("content-type").orElse(null),
                    response.headers().firstValue("content-disposition").orElse(null)
            );
            context.setLastStatusCode(result.status());
            context.setLastResponseBytes(result.body());
            context.setLastResponseBody(result.bodyAsString());
            context.setLastResponseContentType(result.contentType());
            context.setLastResponseContentDisposition(result.contentDisposition());
            context.setLastCorrelationId(
                    response.headers().firstValue("X-Correlation-Id").orElse(null));
            return result;
        } catch (Exception exception) {
            context.setLastStatusCode(0);
            context.setLastResponseBytes(new byte[0]);
            context.setLastResponseBody(exception.getMessage());
            context.setLastResponseContentType(null);
            context.setLastResponseContentDisposition(null);
            return new Result(0, new byte[0], null, null);
        }
    }

    private static String query(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&", "?", "");
        params.forEach((key, value) -> joiner.add(
                URLEncoder.encode(key, StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8)));
        return joiner.toString();
    }
}
