package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.GrafanaProbe;
import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import br.com.fiap.postech.e2e.it.cucumber.support.TempoProbe;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Metrics, traces, correlation and Grafana provisioning assertions.
 */
public class ObservabilityStepDefinitions extends BaseStepDefinition {

    @Quando("eu envie uma requisição com o correlation id {string}")
    public void sendWithCorrelationIdWhen(String correlationId) {
        sendWithCorrelationId(correlationId);
    }

    @Dado("que eu envie uma requisição com o correlation id {string}")
    public void sendWithCorrelationId(String correlationId) {
        context.setCorrelationId(correlationId);
        get(ServiceUrls.Service.MNL, "/owners", Map.of("pageSize", "1"));
        assertThat(context.getLastStatusCode()).isEqualTo(200);
    }

    @Então("a resposta deve devolver o correlation id {string}")
    public void assertCorrelationId(String expected) {
        assertThat(context.getLastCorrelationId()).isEqualTo(expected);
    }

    @Então("as métricas Prometheus devem estar disponíveis nos quatro serviços")
    public void assertMetrics() {
        assertMetric(ServiceUrls.Service.MNL, "jvm_memory_used_bytes");
        assertMetric(ServiceUrls.Service.PEDIDOS, "http_server_requests_seconds_count");
        assertMetric(ServiceUrls.Service.ORCAMENTOS, "events_consumed_total");
        assertMetric(ServiceUrls.Service.EXPEDICAO, "http_server_requests_seconds_count");
    }

    private void assertMetric(ServiceUrls.Service service, String metric) {
        HttpSupport.getPlain(service, "/actuator/prometheus");
        assertThat(context.getLastStatusCode())
                .as("metrics endpoint for %s returned %s", service, lastBody())
                .isEqualTo(200);
        assertThat(lastBody()).as("metric %s missing in %s", metric, service).contains(metric);
    }

    @Então("o dashboard {string} deve estar provisionado")
    public void assertDashboard(String uid) {
        var dashboard = GrafanaProbe.dashboard(uid);
        assertThat(dashboard.path("dashboard").path("title").asText())
                .isEqualTo("Oficina — Fleet Overview");
        assertThat(dashboard.path("dashboard").path("panels").isArray()).isTrue();
        assertThat(dashboard.path("dashboard").path("panels").size()).isGreaterThanOrEqualTo(4);
    }

    @Então("as regras de alerta {string} e {string} devem estar provisionadas")
    public void assertAlertRules(String first, String second) {
        String rules = GrafanaProbe.alertRules().toString();
        assertThat(rules).contains(first).contains(second);
    }

    @Então("a trace da saga deve conter os quatro serviços")
    public void assertSagaTrace() {
        var traceId = TempoProbe.awaitTrace("POST /service-orders/cascade", Duration.ofSeconds(90));
        assertThat(traceId).as("cascade trace not found in Tempo").isPresent();
        Set<String> services = TempoProbe.servicesInTrace(traceId.get());
        assertThat(services)
                .as("trace %s services: %s", traceId.get(), services)
                .contains("mnl-oficina-mecanica", "ms-pedidos", "ms-orcamentos", "ms-expedicao");
    }
}
