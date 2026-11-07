package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Authentication, session and role authorization steps.
 */
public class AuthenticationStepDefinitions extends BaseStepDefinition {

    @Dado("que eu esteja deslogado")
    public void userIsLoggedOut() {
        context.setAuthToken(null);
        context.setCurrentUser(null);
        context.setCurrentRole(null);
    }

    @Dado("que eu esteja devidamente logado")
    public void userIsLoggedIn() {
        loginAs("admin");
    }

    @Quando("informo o usuário {string} e a senha {string}")
    public void submitCredentials(String username, String password) {
        context.setCurrentUser(username);
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        HttpSupport.send("POST", ServiceUrls.Service.MNL, "/auth/login", body, Map.of(), false);
    }

    @E("com um token de sessão válido")
    public void verifyValidSessionToken() {
        assertThat(context.getLastStatusCode()).isLessThan(300);
        String token = context.getLastResponseBody().path("token").asText();
        assertThat(token).as("token ausente: %s", lastBody()).isNotBlank();
        context.setAuthToken(token);
    }

    @E("devo ser capaz de obter meus dados de usuário com {string} igual a {string}")
    public void verifyUserField(String field, String expected) {
        String username = context.getCurrentUser();
        get(ServiceUrls.Service.MNL, "/users", Map.of("username", username, "pageSize", "10"));
        assertStatus(200);
        JsonNode data = context.getLastResponseBody().path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data.isEmpty()).isFalse();
        JsonNode actual = data.get(0).path(field);
        if (actual.isArray()) {
            boolean contains = false;
            for (JsonNode value : actual) {
                if (value.asText().equalsIgnoreCase(expected)) {
                    contains = true;
                    break;
                }
            }
            assertThat(contains).as("roles %s nao contem %s", actual, expected).isTrue();
        } else {
            assertThat(actual.asText()).isEqualToIgnoringCase(expected);
        }
    }

    @Quando("informo a chave de API {string}")
    public void submitApiKey(String apiKey) {
        loginChatbot(apiKey);
    }

    @E("devo ser capaz de obter o tempo de validade da minha chave de API")
    public void verifyApiKeyExpiration() {
        assertThat(context.getLastStatusCode()).isLessThan(300);
        String expiresAt = context.getLastResponseBody().path("expiresAt").asText();
        assertThat(expiresAt).as("expiresAt ausente: %s", lastBody()).isNotBlank();
        assertThat(OffsetDateTime.parse(expiresAt)).isAfter(OffsetDateTime.now());
    }

    @Dado("que eu esteja logado como {string}")
    public void userIsLoggedInAs(String role) {
        context.setCurrentRole(role.toLowerCase());
        context.setCurrentUser(role.toLowerCase());
        if ("admin".equalsIgnoreCase(role)) {
            loginAs("admin");
            return;
        }
        loginAs(role);
    }

    @Dado("que eu esteja autenticado como {string}")
    public void userIsAuthenticatedAs(String role) {
        if ("chatbot".equalsIgnoreCase(role)) {
            loginChatbot(chatbotApiKey());
            return;
        }
        userIsLoggedInAs(role);
    }

    @Quando("acesso o endpoint {string} {string}")
    public void accessEndpoint(String method, String endpoint) {
        String id = context.getCatalogServiceId() == null
                ? "1" : String.valueOf(context.getCatalogServiceId());
        String serviceId = context.getServiceLineId() == null
                ? "1" : String.valueOf(context.getServiceLineId());
        String path = endpoint
                .replace(":serviceId", serviceId)
                .replace(":id", id);
        String body = "GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)
                ? null
                : payloadFor(method, endpoint);
        HttpSupport.send(method, ServiceUrls.Service.MNL, path, body, Map.of(), true);
    }

    @E("quando eu fizer logout")
    public void logout() {
        context.setAuthToken(null);
        context.setCurrentUser(null);
        context.setCurrentRole(null);
    }

    @E("eu fazer login com username {string} e senha padrão")
    public void loginWithDefaultPassword(String username) {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, "default123");
        HttpSupport.send("POST", ServiceUrls.Service.MNL, "/auth/login", body, Map.of(), false);
    }

    @Então("a resposta deve conter um token JWT válido")
    public void verifyJwt() {
        assertThat(context.getLastStatusCode()).isLessThan(300);
        String token = context.getLastResponseBody().path("token").asText();
        assertThat(token).as("token ausente: %s", lastBody()).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        context.setAuthToken(token);
    }

    private String chatbotApiKey() {
        return System.getenv().getOrDefault("E2E_CHATBOT_API_KEY", "local-chatbot-api-key");
    }

    private String payloadFor(String method, String endpoint) {
        if (endpoint.contains("/users")) {
            return "{\"username\":\"testuser\",\"roles\":[\"ADMIN\"]}";
        }
        if (endpoint.contains("/owners")) {
            return "{\"name\":\"Test Owner\",\"document\":\"12345678909\",\"documentType\":\"CPF\","
                    + "\"email\":\"owner@test.com\",\"phone\":\"11999999999\"}";
        }
        if (endpoint.contains("/vehicles")) {
            return "{\"ownerId\":1,\"licensePlate\":\"ABC-1234\",\"brand\":\"Toyota\",\"model\":\"Corolla\","
                    + "\"year\":2020,\"color\":\"Red\"}";
        }
        if (endpoint.contains("/catalog/services")) {
            return "PATCH".equalsIgnoreCase(method)
                    ? "{\"id\":1,\"name\":\"Test Service\",\"description\":\"Test\",\"basePrice\":100.00,"
                    + "\"neededSupplies\":[]}"
                    : "{\"name\":\"Test Service\",\"description\":\"Test\",\"basePrice\":100.00,"
                    + "\"neededSupplies\":[]}";
        }
        if (endpoint.contains("/supplies")) {
            return "{\"sku\":\"TEST-SKU\",\"name\":\"Test Supply\",\"description\":\"Test\","
                    + "\"unitPrice\":10.00,\"suppliedBy\":1}";
        }
        return "{}";
    }
}
