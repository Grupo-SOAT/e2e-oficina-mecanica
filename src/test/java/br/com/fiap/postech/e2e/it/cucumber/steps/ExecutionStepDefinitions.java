package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Execution queue steps backed by DynamoDB.
 */
public class ExecutionStepDefinitions extends BaseStepDefinition {

    @Dado("que a ordem de serviço esteja na fila de execução")
    public void awaitQueued() {
        for (int attempt = 0; attempt < 90; attempt++) {
            if (queueContains(context.getServiceOrderId(), "QUEUED")) {
                return;
            }
            sleep();
        }
        failWith("OS " + context.getServiceOrderId() + " was not queued");
    }

    @Quando("eu iniciar a execução da ordem de serviço")
    public void startExecution() {
        HttpSupport.Result result = HttpSupport.send("POST", ServiceUrls.Service.EXPEDICAO,
                "/execution-queue/" + context.getServiceOrderId() + "/start",
                null, Map.of(), true);
        assertThat(result.status()).as("start failed: %s", result.bodyAsString()).isEqualTo(200);
    }

    @Quando("eu concluir a execução da ordem de serviço")
    public void completeExecution() {
        HttpSupport.Result result = HttpSupport.send("POST", ServiceUrls.Service.EXPEDICAO,
                "/execution-queue/" + context.getServiceOrderId() + "/complete",
                null, Map.of(), true);
        assertThat(result.status()).as("complete failed: %s", result.bodyAsString()).isEqualTo(200);
    }

    @Então("a fila do branch {long} no status {string} deve conter a ordem de serviço")
    public void assertQueueContains(long branchId, String status) {
        for (int attempt = 0; attempt < 90; attempt++) {
            if (fetchQueue(branchId, status)) {
                return;
            }
            sleep();
        }
        assertThat(fetchQueue(branchId, status))
                .as("fila %s do branch %s: %s", status, branchId, lastBody())
                .isTrue();
    }

    @Então("a fila do branch {long} no status {string} não deve conter a ordem de serviço")
    public void assertQueueDoesNotContain(long branchId, String status) {
        assertThat(fetchQueue(branchId, status)).isFalse();
    }

    @Então("o job da ordem de serviço deve estar com status {string}")
    public void assertJobStatus(String expected) {
        for (int attempt = 0; attempt < 30; attempt++) {
            if (fetchQueue(1L, expected)) {
                return;
            }
            sleep();
        }
        assertThat(fetchQueue(1L, expected))
                .as("job da OS %s nao esta em %s", context.getServiceOrderId(), expected)
                .isTrue();
    }

    private boolean fetchQueue(long branchId, String status) {
        get(ServiceUrls.Service.EXPEDICAO, "/execution-queue",
                Map.of("branchId", String.valueOf(branchId), "status", status));
        if (context.getLastStatusCode() == 204) {
            return false;
        }
        JsonNode body = context.getLastResponseBody();
        if (!body.isArray()) {
            return false;
        }
        for (JsonNode job : body) {
            if (job.path("serviceOrderId").asLong() == context.getServiceOrderId()) {
                return true;
            }
        }
        return false;
    }

    private boolean queueContains(Long serviceOrderId, String status) {
        get(ServiceUrls.Service.EXPEDICAO, "/execution-queue",
                Map.of("branchId", "1", "status", status));
        JsonNode body = context.getLastResponseBody();
        if (!body.isArray()) {
            return false;
        }
        for (JsonNode job : body) {
            if (job.path("serviceOrderId").asLong() == serviceOrderId) {
                return true;
            }
        }
        return false;
    }

    @Quando("eu iniciar uma execução inexistente")
    public void startUnknown() {
        HttpSupport.send("POST", ServiceUrls.Service.EXPEDICAO,
                "/execution-queue/99999/start", null, Map.of(), true);
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
