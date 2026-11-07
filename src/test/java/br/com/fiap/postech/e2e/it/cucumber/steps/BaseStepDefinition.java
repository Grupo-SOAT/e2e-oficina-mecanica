package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.context.CucumberTestContext;
import br.com.fiap.postech.e2e.it.cucumber.support.ChatbotAuth;
import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Shared plumbing for the HTTP step definitions.
 */
public abstract class BaseStepDefinition {

    protected final CucumberTestContext context = CucumberTestContext.getInstance();
    protected final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    protected HttpSupport.Result get(ServiceUrls.Service service, String path) {
        return HttpSupport.get(service, path, paginationParams());
    }

    protected HttpSupport.Result get(ServiceUrls.Service service, String path, Map<String, String> params) {
        return HttpSupport.get(service, path, params);
    }

    protected HttpSupport.Result post(ServiceUrls.Service service, String path, String body) {
        return HttpSupport.post(service, path, body);
    }

    protected HttpSupport.Result patch(ServiceUrls.Service service, String path, String body) {
        return HttpSupport.patch(service, path, body);
    }

    protected HttpSupport.Result delete(ServiceUrls.Service service, String path) {
        return HttpSupport.delete(service, path);
    }

    protected Map<String, String> paginationParams() {
        Map<String, String> params = new LinkedHashMap<>();
        if (context.getPageSize() != null) {
            params.put("pageSize", String.valueOf(context.getPageSize()));
        }
        if (context.getCursor() != null) {
            params.put("cursor", context.getCursor());
        }
        if (context.getFilterName() != null && context.getFilterValue() != null) {
            params.put(context.getFilterName(), context.getFilterValue());
        }
        return params;
    }

    protected void loginAs(String role) {
        String username = role.toLowerCase();
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, username);
        HttpSupport.Result result = HttpSupport.send("POST", ServiceUrls.Service.MNL,
                "/auth/login", body, Map.of(), false);
        assertThat(result.status())
                .as("login for %s failed: %s", username, result.bodyAsString())
                .isEqualTo(200);
        String token = json(result.bodyAsString()).path("token").asText();
        assertThat(token).as("login token for %s", username).isNotBlank();
        context.setAuthToken(token);
        context.setCurrentUser(username);
        context.setCurrentRole(role.toLowerCase());
    }

    protected HttpSupport.Result loginChatbot(String apiKey) {
        HttpSupport.Result result = ChatbotAuth.login(apiKey);
        context.setLastStatusCode(result.status());
        context.setLastResponseBody(result.bodyAsString());
        context.setLastResponseContentType(result.contentType());
        if (result.status() == 200) {
            context.setAuthToken(json(result.bodyAsString()).path("token").asText());
            context.setCurrentUser("chatbot");
            context.setCurrentRole("chatbot");
        }
        return result;
    }

    protected JsonNode json(String raw) {
        try {
            return objectMapper.readTree(raw == null || raw.isBlank() ? "{}" : raw);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot parse JSON: " + raw, exception);
        }
    }

    protected String dataTableToJson(DataTable table) throws Exception {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : row.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                normalized.put(entry.getKey(), null);
                continue;
            }
            if (value.matches("^-?\\d+$")) {
                normalized.put(entry.getKey(), Long.parseLong(value));
            } else if (value.matches("^-?\\d+\\.\\d+$")) {
                normalized.put(entry.getKey(), Double.parseDouble(value));
            } else if ((value.startsWith("[") && value.endsWith("]"))
                    || (value.startsWith("{") && value.endsWith("}"))) {
                normalized.put(entry.getKey(), objectMapper.readTree(value));
            } else {
                normalized.put(entry.getKey(), value);
            }
        }
        return objectMapper.writeValueAsString(normalized);
    }

    protected void setJsonBody(DataTable table) throws Exception {
        context.setRequestBody(dataTableToJson(table));
    }

    protected void assertStatus(int expected) {
        assertThat(context.getLastStatusCode())
                .as("status inesperado; body: %s", context.getLastResponseBodyAsString())
                .isEqualTo(expected);
    }

    protected void assertStatusNot(int forbidden) {
        assertThat(context.getLastStatusCode())
                .as("endpoint nao deveria retornar %s; body: %s", forbidden,
                        context.getLastResponseBodyAsString())
                .isNotEqualTo(forbidden);
    }

    protected void assertFieldExists(String field) {
        JsonNode node = resolve(field);
        assertThat(node.isMissingNode())
                .as("campo ausente: %s; body: %s", field, context.getLastResponseBodyAsString())
                .isFalse();
    }

    protected void assertFieldValue(String field, String expected) {
        JsonNode node = resolve(field);
        assertThat(node.isMissingNode() || node.isNull())
                .as("campo ausente: %s; body: %s", field, context.getLastResponseBodyAsString())
                .isFalse();
        assertThat(node.asText()).isEqualTo(expected);
    }

    private JsonNode resolve(String field) {
        JsonNode node = context.getLastResponseBody();
        for (String part : field.split("\\.")) {
            node = node.path(part);
        }
        return node;
    }

    protected void assertReason(String reason) {
        assertThat(context.getLastResponseBody().path("reason").asText())
                .as("reason inesperado; body: %s", context.getLastResponseBodyAsString())
                .isEqualTo(reason);
    }

    protected void assertDataArray() {
        JsonNode root = context.getLastResponseBody();
        assertThat(root.has("data")).as("campo data ausente").isTrue();
        assertThat(root.get("data").isArray()).as("campo data nao e array").isTrue();
    }

    protected void assertMaxDataSize(int maxSize, String resource) {
        JsonNode root = context.getLastResponseBody();
        assertThat(root.has("data")).as("campo data ausente").isTrue();
        assertThat(root.get("data").isArray()).as("campo data nao e array").isTrue();
        assertThat(root.get("data").size())
                .as("quantidade de %s acima do esperado", resource)
                .isLessThanOrEqualTo(maxSize);
    }

    protected void assertHasItemWithField(String field, String expectedValue, String resource) {
        JsonNode root = context.getLastResponseBody();
        assertThat(root.has("data")).as("campo data ausente").isTrue();
        assertThat(root.get("data").isArray()).as("campo data nao e array").isTrue();
        List<String> values = new ArrayList<>();
        root.get("data").forEach(item -> values.add(item.path(field).asText()));
        assertThat(values)
                .as("nenhum %s com %s=%s; body: %s", resource, field, expectedValue,
                        context.getLastResponseBodyAsString())
                .contains(expectedValue);
    }

    protected void assertNoItemWithField(String field, String unexpectedValue, String resource) {
        JsonNode root = context.getLastResponseBody();
        assertThat(root.has("data")).as("campo data ausente").isTrue();
        List<String> values = new ArrayList<>();
        root.get("data").forEach(item -> values.add(item.path(field).asText()));
        assertThat(values)
                .as("%s com %s=%s nao deveria estar presente", resource, field, unexpectedValue)
                .doesNotContain(unexpectedValue);
    }

    protected void assertFirstItemIdGreaterThan(long minId) {
        JsonNode root = context.getLastResponseBody();
        assertThat(root.has("data")).as("campo data ausente").isTrue();
        assertThat(root.get("data").isEmpty()).as("lista vazia").isFalse();
        assertThat(root.get("data").get(0).path("id").asLong()).isGreaterThan(minId);
    }

    protected void assertResponseMatchesRequest() {
        try {
            JsonNode request = objectMapper.readTree(context.getRequestBody());
            JsonNode response = context.getLastResponseBody();
            request.fieldNames().forEachRemaining(field -> {
                if (request.get(field) == null || request.get(field).isNull()) {
                    return;
                }
                assertThat(response.has(field))
                        .as("campo ausente na resposta: %s; body: %s", field,
                                context.getLastResponseBodyAsString())
                        .isTrue();
                assertThat(response.get(field).asText())
                        .as("valor divergente para %s", field)
                        .isEqualTo(request.get(field).asText());
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot compare request and response", exception);
        }
    }

    protected void assertDataIdsInOrder(String expectedIds) {
        JsonNode root = context.getLastResponseBody();
        assertThat(root.has("data")).as("campo data ausente").isTrue();
        List<Long> expected = Arrays.stream(expectedIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();
        List<Long> actual = new ArrayList<>();
        root.get("data").forEach(item -> actual.add(item.path("id").asLong()));
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    protected String lastBody() {
        return context.getLastResponseBodyAsString();
    }

    protected void failWith(String message) {
        fail(message);
    }
}
