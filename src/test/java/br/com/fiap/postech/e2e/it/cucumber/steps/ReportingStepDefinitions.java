package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reporting export steps. The report itself is produced by {@code mnl}, which reads execution data
 * from {@code ms-pedidos} over HTTP.
 */
public class ReportingStepDefinitions extends BaseStepDefinition {

    @Dado("que o id do serviço seja {long}")
    public void setCatalogServiceId(long id) {
        context.setCatalogServiceId(id);
    }

    @Dado("que nenhum serviço esteja cadastrado")
    public void noServiceRegistered() {
        // The canonical reset truncates ms-pedidos before each scenario.
    }

    @Dado("que exista um serviço executado no catálogo")
    public void createExecutedService() {
        String body = """
                {"ownerId":1,"vehicleId":1,"branchId":1,
                 "description":"Relatorio E2E","catalogServiceIds":[1]}
                """;
        HttpSupport.Result created = post(ServiceUrls.Service.PEDIDOS,
                "/service-orders/cascade", body);
        assertThat(created.status())
                .as("cascade for reporting failed: %s", created.bodyAsString())
                .isEqualTo(201);
        context.setServiceOrderId(context.getLastResponseBody().path("order").path("id").asLong());
        context.setServiceLineId(context.getLastResponseBody().path("services").get(0).path("id").asLong());
        context.setCatalogServiceId(context.getLastResponseBody().path("services").get(0)
                .path("catalogServiceId").asLong());
    }

    @Então("o content-type da resposta deve ser {string}")
    public void verifyContentType(String expected) {
        assertThat(context.getLastResponseContentType())
                .as("content-type inesperado; body: %s", lastBody())
                .contains(expected);
    }

    @Então("o body da resposta não deve ser vazio")
    public void verifyBodyNotEmpty() {
        assertThat(context.getLastResponseBytes()).isNotEmpty();
    }

    @Então("o nome do arquivo deve indicar exportação recente")
    public void verifyRecentFilename() {
        String disposition = context.getLastResponseContentDisposition();
        assertThat(disposition).as("Content-Disposition ausente").isNotBlank();
        assertThat(disposition).contains("attachment");
        assertThat(disposition).containsPattern("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}");
    }
}
