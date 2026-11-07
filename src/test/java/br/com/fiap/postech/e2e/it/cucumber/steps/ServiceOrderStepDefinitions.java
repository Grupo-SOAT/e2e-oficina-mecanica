package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.KafkaProbe;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service order lifecycle steps exercised over the real HTTP API.
 */
public class ServiceOrderStepDefinitions extends BaseStepDefinition {

    private static final List<String> HAPPY_PATH = List.of(
            "IN_INSPECTION", "AWAITING_APPROVAL", "APPROVED", "IN_PROGRESS", "COMPLETED", "DELIVERED");

    @Dado("que o id da ordem de serviço seja {long}")
    public void setServiceOrderId(long id) {
        context.setServiceOrderId(id);
    }

    @Dado("que o id do serviço da ordem de serviço seja {long}")
    public void setServiceLineId(long id) {
        context.setServiceLineId(id);
    }

    @Dado("que o filtro serviceId seja o serviço da ordem de serviço")
    public void filterByCurrentServiceLine() {
        context.setFilterName("serviceId");
        context.setFilterValue(String.valueOf(context.getServiceLineId()));
    }

    @Dado("que exista uma ordem de serviço com um serviço")
    public void createOrderWithOneService() {
        createCascade(List.of(1L));
    }

    @Dado("que exista uma ordem de serviço com dois serviços")
    public void createOrderWithTwoServices() {
        createCascade(List.of(1L, 2L));
    }

    private void createCascade(List<Long> catalogIds) {
        String ids = catalogIds.toString();
        String body = "{\"ownerId\":1,\"vehicleId\":1,\"branchId\":1,"
                + "\"description\":\"OS E2E\",\"catalogServiceIds\":" + ids + "}";
        HttpSupport.Result result = post(ServiceUrls.Service.PEDIDOS, "/service-orders/cascade", body);
        assertThat(result.status()).as("cascade failed: %s", result.bodyAsString()).isEqualTo(201);
        JsonNode response = context.getLastResponseBody();
        context.setServiceOrderId(response.path("order").path("id").asLong());
        context.setServiceLineId(response.path("services").get(0).path("id").asLong());
    }

    @Dado("que a ordem de serviço esteja no status {string}")
    public void advanceTo(String target) {
        Long id = context.getServiceOrderId();
        if ("CANCELLED".equals(target)) {
            patch(ServiceUrls.Service.PEDIDOS, "/service-orders/" + id + "/status",
                    "{\"status\":\"CANCELLED\"}");
            return;
        }
        for (int guard = 0; guard < 12; guard++) {
            get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + id, Map.of());
            String current = context.getLastResponseBody().path("status").asText();
            if (target.equals(current)) {
                return;
            }
            int currentIndex = HAPPY_PATH.indexOf(current);
            int targetIndex = HAPPY_PATH.indexOf(target);
            if (targetIndex < 0 || currentIndex >= targetIndex) {
                failWith("cannot advance OS from " + current + " to " + target);
            }
            String next = HAPPY_PATH.get(currentIndex + 1);
            patch(ServiceUrls.Service.PEDIDOS, "/service-orders/" + id + "/status",
                    "{\"status\":\"" + next + "\"}");
            if (context.getLastStatusCode() != 200) {
                sleep();
            }
        }
        failWith("could not advance OS " + id + " to " + target + ": " + lastBody());
    }

    @Dado("que a ordem de serviço tenha o orçamento calculado")
    public void waitForBudget() {
        Long id = context.getServiceOrderId();
        for (int attempt = 0; attempt < 45; attempt++) {
            get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + id, Map.of());
            JsonNode amount = context.getLastResponseBody().path("estimatedAmount");
            if (!amount.isNull() && !amount.isMissingNode()) {
                return;
            }
            sleep();
        }
        failWith("budget was not calculated for OS " + id);
    }

    @Dado("que o corpo da nova ordem de serviço seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
        JsonNode root = objectMapper.readTree(context.getRequestBody());
        if (root instanceof ObjectNode objectNode) {
            objectNode.remove("id");
            if (!objectNode.has("branchId")) {
                objectNode.put("branchId", 1);
            }
            context.setRequestBody(objectMapper.writeValueAsString(objectNode));
        }
    }

    @Dado("que o corpo de atualização da ordem de serviço seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
        JsonNode root = objectMapper.readTree(context.getRequestBody());
        if (root instanceof ObjectNode objectNode) {
            objectNode.remove("id");
            context.setRequestBody(objectMapper.writeValueAsString(objectNode));
        }
    }

    @Dado("que o serviço relacionado seja o serviço da ordem de serviço")
    public void linkRelatedService() throws Exception {
        JsonNode root = objectMapper.readTree(context.getRequestBody());
        if (root instanceof ObjectNode objectNode) {
            objectNode.put("relatedServiceId", context.getServiceLineId());
            context.setRequestBody(objectMapper.writeValueAsString(objectNode));
        }
    }

    @Dado("que o status do serviço da ordem de serviço seja {string}")
    public void forceServiceStatus(String target) {
        if ("IN_PROGRESS".equals(target)) {
            String body = "{\"action\":\"START_SERVICE\",\"relatedServiceId\":"
                    + context.getServiceLineId() + "}";
            HttpSupport.Result result = post(ServiceUrls.Service.PEDIDOS,
                    "/service-orders/" + context.getServiceOrderId() + "/progress", body);
            assertThat(result.status()).as("start service failed: %s", result.bodyAsString())
                    .isEqualTo(202);
        }
    }

    @Dado("que o corpo da ação de progresso da ordem de serviço seja:")
    public void setProgressBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar as ordens de serviço")
    public void listOrders() {
        get(ServiceUrls.Service.PEDIDOS, "/service-orders");
    }

    @Quando("eu consultar a ordem de serviço por id")
    public void getOrder() {
        get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId(), Map.of());
    }

    @Quando("eu criar a ordem de serviço")
    public void createOrder() {
        HttpSupport.Result result = post(ServiceUrls.Service.PEDIDOS, "/service-orders",
                context.getRequestBody());
        if (result.status() == 201) {
            context.setServiceOrderId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu criar a ordem de serviço em cascata")
    public void createCascade() {
        HttpSupport.Result result = post(ServiceUrls.Service.PEDIDOS, "/service-orders/cascade",
                context.getRequestBody());
        if (result.status() == 201) {
            JsonNode response = context.getLastResponseBody();
            context.setServiceOrderId(response.path("order").path("id").asLong());
            JsonNode services = response.path("services");
            if (services.isArray() && !services.isEmpty()) {
                context.setServiceLineId(services.get(0).path("id").asLong());
            }
        }
    }

    @Quando("eu atualizar a ordem de serviço")
    public void updateOrder() {
        patch(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId(),
                context.getRequestBody());
    }

    @Quando("eu cancelar a ordem de serviço")
    public void cancelOrder() {
        patch(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId() + "/status",
                "{\"status\":\"CANCELLED\"}");
        assertThat(context.getLastStatusCode()).as("cancel failed: %s", lastBody()).isEqualTo(200);
    }

    @Quando("eu remover a ordem de serviço")
    public void deleteOrder() {
        delete(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId());
    }

    @Quando("eu registrar o progresso da ordem de serviço")
    public void registerProgress() {
        post(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId() + "/progress",
                context.getRequestBody());
    }

    @Dado("que o corpo do novo serviço da ordem de serviço seja:")
    public void setNewServiceBody(DataTable table) throws Exception {
        setServiceBody(table);
    }

    @Dado("que o corpo de atualização do serviço da ordem de serviço seja:")
    public void setServiceUpdateBody(DataTable table) throws Exception {
        setServiceBody(table);
    }

    private void setServiceBody(DataTable table) throws Exception {
        setJsonBody(table);
        JsonNode root = objectMapper.readTree(context.getRequestBody());
        if (root instanceof ObjectNode objectNode) {
            objectNode.remove("serviceOrderId");
            objectNode.remove("neededSupplies");
            objectNode.remove("id");
            context.setRequestBody(objectMapper.writeValueAsString(objectNode));
        }
    }

    @Dado("a ordem de serviço tenha o orçamento calculado")
    public void waitForBudgetAlias() {
        waitForBudget();
    }

    @Quando("eu listar os serviços da ordem de serviço")
    public void listServices() {
        get(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId() + "/services");
    }

    @Quando("eu consultar o serviço da ordem de serviço por id")
    public void getService() {
        get(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId()
                        + "/services/" + context.getServiceLineId(), Map.of());
    }

    @Quando("eu incluir o serviço na ordem de serviço")
    public void createService() {
        HttpSupport.Result result = post(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId() + "/services",
                context.getRequestBody());
        if (result.status() == 201) {
            context.setServiceLineId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu atualizar o serviço da ordem de serviço")
    public void updateService() {
        patch(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId()
                        + "/services/" + context.getServiceLineId(),
                context.getRequestBody());
    }

    @Quando("eu remover o serviço da ordem de serviço")
    public void deleteService() {
        delete(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId()
                        + "/services/" + context.getServiceLineId());
    }

    @Então("a resposta deve conter o id do novo veículo")
    public void assertNewVehicleId() {
        assertThat(context.getLastResponseBody().path("order").path("vehicleId").asLong()).isPositive();
    }

    @Então("a resposta deve conter o id do novo cliente")
    public void assertNewClientId() {
        assertThat(context.getLastResponseBody().path("order").path("clientId").asLong()).isPositive();
    }

    @Então("um evento do tipo {string} deve ser publicado no tópico {string} para a ordem de serviço")
    public void assertEventPublished(String type, String topic) {
        Long serviceOrderId = context.getServiceOrderId();
        var event = KafkaProbe.await(topic,
                node -> type.equals(node.path("type").asText())
                        && node.path("serviceOrderId").asLong() == serviceOrderId,
                Duration.ofSeconds(30));
        assertThat(event).as("evento %s nao publicado em %s", type, topic).isPresent();
    }

    @Então("o status da ordem de serviço deve ser {string}")
    public void assertOrderStatus(String expected) {
        for (int attempt = 0; attempt < 30; attempt++) {
            get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId(), Map.of());
            if (expected.equals(context.getLastResponseBody().path("status").asText())) {
                return;
            }
            sleep();
        }
        assertThat(context.getLastResponseBody().path("status").asText()).isEqualTo(expected);
    }

    @Então("o statusLabel da ordem de serviço deve ser {string}")
    public void assertOrderStatusLabel(String expected) {
        get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId(), Map.of());
        assertThat(context.getLastResponseBody().path("statusLabel").asText()).isEqualTo(expected);
    }

    @Então("o status do serviço da ordem de serviço deve ser {string}")
    public void assertServiceStatus(String expected) {
        get(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId()
                        + "/services/" + context.getServiceLineId(), Map.of());
        assertThat(context.getLastResponseBody().path("status").asText()).isEqualTo(expected);
    }

    @Então("o campo decimal {string} da ordem de serviço não deve ser vazio")
    public void assertDecimalNotEmpty(String field) {
        get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId(), Map.of());
        JsonNode value = context.getLastResponseBody().path(field);
        assertThat(value.isNumber()).as("campo %s vazio", field).isTrue();
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unused")
    private List<String> happyPath() {
        return new ArrayList<>(HAPPY_PATH);
    }
}
