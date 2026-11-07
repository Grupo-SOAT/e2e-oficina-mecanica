package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.BudgetProbe;
import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.KafkaProbe;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Budget pricing and audit steps.
 */
public class BudgetStepDefinitions extends BaseStepDefinition {

    @Dado("que a inspeção da ordem de serviço seja concluída")
    public void completeInspection() {
        patch(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId() + "/status",
                "{\"status\":\"IN_INSPECTION\"}");
        assertThat(context.getLastStatusCode()).isEqualTo(200);
        patch(ServiceUrls.Service.PEDIDOS,
                "/service-orders/" + context.getServiceOrderId() + "/status",
                "{\"status\":\"AWAITING_APPROVAL\"}");
        assertThat(context.getLastStatusCode()).isEqualTo(200);
        awaitBudget();
    }

    @Dado("que a ordem de serviço tenha orçamento calculado")
    public void awaitBudgetStep() {
        awaitBudget();
    }

    private void awaitBudget() {
        Long serviceOrderId = context.getServiceOrderId();
        for (int attempt = 0; attempt < 45; attempt++) {
            if (BudgetProbe.forServiceOrder(serviceOrderId) > 0) {
                context.setBudgetId(BudgetProbe.budgetId(serviceOrderId));
                return;
            }
            sleep();
        }
        failWith("budget was not persisted for OS " + serviceOrderId);
    }

    @Então("o orçamento da ordem de serviço deve ter sido persistido")
    public void assertBudgetPersisted() {
        assertThat(BudgetProbe.forServiceOrder(context.getServiceOrderId())).isPositive();
        context.setBudgetId(BudgetProbe.budgetId(context.getServiceOrderId()));
    }

    @Então("o orçamento da ordem de serviço deve conter {int} itens")
    public void assertBudgetItems(int expected) {
        assertThat(BudgetProbe.itemCount(context.getServiceOrderId())).isEqualTo(expected);
    }

    @Então("o total do orçamento da ordem de serviço deve ser maior que zero")
    public void assertTotalPositive() {
        BigDecimal total = BudgetProbe.totalAmount(context.getServiceOrderId());
        assertThat(total).isNotNull();
        assertThat(total.signum()).isPositive();
    }

    @Então("a auditoria de preço do orçamento deve estar preenchida")
    public void assertPriceAudit() {
        assertThat(BudgetProbe.itemsHavePriceFetchedAt(context.getServiceOrderId())).isTrue();
    }

    @Então("o valor unitário do item de catálogo {long} deve ser {string}")
    public void assertUnitPrice(long catalogServiceId, String expected) {
        assertThat(BudgetProbe.unitPrice(context.getServiceOrderId(), catalogServiceId))
                .isEqualByComparingTo(new BigDecimal(expected));
    }

    @Então("um evento do tipo {string} deve ser publicado no tópico {string} para a ordem de serviço atual")
    public void assertEvent(String type, String topic) {
        Long serviceOrderId = context.getServiceOrderId();
        var event = KafkaProbe.await(topic,
                node -> type.equals(node.path("type").asText())
                        && node.path("serviceOrderId").asLong() == serviceOrderId,
                Duration.ofSeconds(30));
        assertThat(event).as("evento %s nao publicado em %s", type, topic).isPresent();
    }

    @Então("o evento {string} deve conter o campo {string} com um valor não vazio")
    public void assertEventField(String type, String field) {
        var events = KafkaProbe.read("budget-events", Duration.ofSeconds(5));
        var match = events.stream()
                .filter(node -> type.equals(node.path("type").asText()))
                .filter(node -> node.path("serviceOrderId").asLong() == context.getServiceOrderId())
                .findFirst();
        assertThat(match).isPresent();
        assertThat(match.get().path("payload").path(field).asText()).isNotBlank();
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unused")
    private Map<String, String> noParams() {
        return Map.of();
    }

    @SuppressWarnings("unused")
    private HttpSupport.Result unused() {
        return null;
    }
}
