package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.PaymentProbe;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import br.com.fiap.postech.e2e.it.cucumber.support.WireMockAdmin;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checkout Pro, webhook, refund and timeout steps.
 */
public class PaymentStepDefinitions extends BaseStepDefinition {

    @Quando("eu criar a preferência de checkout do orçamento")
    public void createPreference() {
        HttpSupport.Result result = post(ServiceUrls.Service.ORCAMENTOS,
                "/payments/budgets/" + context.getBudgetId() + "/checkout", null);
        assertThat(result.status()).as("checkout failed: %s", result.bodyAsString()).isEqualTo(200);
        context.put("preferenceId", context.getLastResponseBody().path("preferenceId").asText());
        context.put("initPoint", context.getLastResponseBody().path("initPoint").asText());
    }

    @Então("a preferência de checkout deve ser reutilizada")
    public void assertPreferenceReused() {
        String first = context.getString("preferenceId");
        post(ServiceUrls.Service.ORCAMENTOS,
                "/payments/budgets/" + context.getBudgetId() + "/checkout", null);
        assertThat(context.getLastResponseBody().path("preferenceId").asText()).isEqualTo(first);
        assertThat(PaymentProbe.preferenceId(context.getServiceOrderId())).isEqualTo(first);
    }

    @Dado("que eu crie a preferência de checkout do orçamento")
    public void createPreferenceAlias() {
        createPreference();
    }

    @Quando("o pagamento for aprovado no Mercado Pago")
    public void approvePaymentAlias() {
        approvePayment();
    }

    @Quando("o pagamento for recusado no Mercado Pago")
    public void rejectPaymentAlias() {
        rejectPayment();
    }

    @Quando("o pagamento for enviado com status {string}")
    public void sendStatusAlias(String status) {
        sendStatus(status);
    }

    @Dado("que o pagamento seja aprovado no Mercado Pago")
    public void approvePayment() {
        String paymentId = "mp-" + context.getServiceOrderId();
        WireMockAdmin.resetMappings();
        WireMockAdmin.stubApprovedPayment(paymentId, context.getServiceOrderId());
        sendWebhook(paymentId);
        awaitPaymentStatus("APPROVED");
    }

    @Dado("que o pagamento seja recusado no Mercado Pago")
    public void rejectPayment() {
        String paymentId = "mp-" + context.getServiceOrderId();
        WireMockAdmin.resetMappings();
        WireMockAdmin.stubRejectedPayment(paymentId, context.getServiceOrderId());
        sendWebhook(paymentId);
        awaitPaymentStatus("REJECTED");
    }

    @Dado("que o pagamento seja enviado com status {string}")
    public void sendStatus(String status) {
        String paymentId = "mp-" + context.getServiceOrderId();
        WireMockAdmin.resetMappings();
        WireMockAdmin.stubPayment(paymentId, status, context.getServiceOrderId());
        sendWebhook(paymentId);
    }

    private void sendWebhook(String paymentId) {
        String body = "{\"type\":\"payment\",\"data\":{\"id\":\"" + paymentId + "\"}}";
        HttpSupport.Result result = post(ServiceUrls.Service.ORCAMENTOS, "/payments/webhook", body);
        assertThat(result.status()).as("webhook failed: %s", result.bodyAsString()).isEqualTo(200);
    }

    @Então("o pagamento deve estar com status {string}")
    public void assertPaymentStatus(String expected) {
        awaitPaymentStatus(expected);
    }

    private void awaitPaymentStatus(String expected) {
        for (int attempt = 0; attempt < 30; attempt++) {
            if (expected.equals(PaymentProbe.paymentStatus(context.getServiceOrderId()))) {
                return;
            }
            sleep();
        }
        assertThat(PaymentProbe.paymentStatus(context.getServiceOrderId())).isEqualTo(expected);
    }

    @Quando("eu solicitar o reembolso {int} vezes")
    public void refund(int times) {
        for (int index = 1; index <= times; index++) {
            HttpSupport.send("POST", ServiceUrls.Service.ORCAMENTOS,
                    "/payments/" + context.getServiceOrderId() + "/refund",
                    null, java.util.Map.of(), true);
            context.put("refundId" + index, context.getLastResponseBody().path("id").asLong());
        }
    }

    @Então("os reembolsos devem ser idempotentes")
    public void assertRefundsIdempotent() {
        Object first = context.get("refundId1");
        Object second = context.get("refundId2");
        assertThat(first).isNotNull();
        assertThat(second).isEqualTo(first);
        assertThat(PaymentProbe.refundCount(context.getServiceOrderId())).isEqualTo(1);
    }

    @Então("o valor do reembolso deve ser igual ao valor do pagamento")
    public void assertRefundAmount() {
        BigDecimal paid = PaymentProbe.paymentAmount(context.getServiceOrderId());
        BigDecimal refunded = PaymentProbe.refundAmount(context.getServiceOrderId());
        assertThat(refunded).isEqualByComparingTo(paid);
    }

    @Dado("que o pagamento esteja expirado")
    public void expirePayment() {
        PaymentProbe.expirePayment(context.getServiceOrderId());
    }

    @Então("o pagamento deve ser expirado")
    public void assertExpired() {
        awaitPaymentStatus("EXPIRED");
    }

    @Então("a ordem de serviço deve estar cancelada")
    public void assertServiceOrderCancelled() {
        for (int attempt = 0; attempt < 45; attempt++) {
            get(ServiceUrls.Service.PEDIDOS, "/service-orders/" + context.getServiceOrderId(),
                    java.util.Map.of());
            if ("CANCELLED".equals(context.getLastResponseBody().path("status").asText())) {
                return;
            }
            sleep();
        }
        assertThat(context.getLastResponseBody().path("status").asText()).isEqualTo("CANCELLED");
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
