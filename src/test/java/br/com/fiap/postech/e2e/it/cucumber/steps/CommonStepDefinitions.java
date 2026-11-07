package br.com.fiap.postech.e2e.it.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;

/**
 * Steps shared by every feature: pagination state and response assertions.
 */
public class CommonStepDefinitions extends BaseStepDefinition {

    @Dado("que o tamanho da pagina seja {int}")
    public void setPageSize(int pageSize) {
        context.setPageSize(pageSize);
    }

    @Dado("que o cursor seja {string}")
    public void setCursor(String cursor) {
        context.setCursor(cursor);
    }

    @Dado("que o filtro {word} seja {string}")
    public void setFilter(String name, String value) {
        context.setFilterName(name);
        context.setFilterValue(value);
    }

    @Então("devo receber uma resposta com status {string}")
    public void verifyStatus(String status) {
        assertStatus(Integer.parseInt(status));
    }

    @Então("devo receber um erro com status {string}")
    public void verifyErrorStatus(String status) {
        assertStatus(Integer.parseInt(status));
    }

    @Então("devo receber uma resposta com status diferente de {string}")
    public void verifyStatusDifferentFrom(String status) {
        assertStatusNot(Integer.parseInt(status));
    }

    @Então("a resposta deve conter uma lista de dados")
    public void verifyDataArray() {
        assertDataArray();
    }

    @Então("a resposta deve conter o campo {string}")
    public void verifyFieldExists(String field) {
        assertFieldExists(field);
    }

    @Então("a resposta deve conter o campo {word} com valor {string}")
    public void verifyFieldWithValue(String field, String value) {
        assertFieldValue(field, value);
    }

    @Então("a resposta deve conter no maximo {int} {}")
    public void verifyMaxDataSize(int maxSize, String resource) {
        assertMaxDataSize(maxSize, resource);
    }

    @Então("^a resposta deve conter ao menos (?:um|uma) (.+) com (\\w+) \"([^\"]*)\"$")
    public void verifyHasItemWithFieldRegex(String resource, String field, String value) {
        String jsonField = "nome".equals(field) ? "name" : field;
        assertHasItemWithField(jsonField, value, resource);
    }

    @Então("com a mensagem de erro igual a {string}")
    public void verifyErrorMessage(String expected) {
        assertReason(expected);
    }

    @Dado("que o filtro serviceId seja {long}")
    public void setNumericServiceIdFilter(long value) {
        context.setFilterName("serviceId");
        context.setFilterValue(String.valueOf(value));
    }

    @Dado("que o filtro id seja {long}")
    public void setNumericIdFilter(long value) {
        context.setFilterName("id");
        context.setFilterValue(String.valueOf(value));
    }

    @Então("a resposta não deve conter nenhuma {} com status {string}")
    public void verifyNoItemWithStatus(String resource, String status) {
        assertNoItemWithField("status", status, resource);
    }

    @Então("o primeiro item retornado deve ter id maior que {long}")
    public void verifyFirstItemIdGreaterThan(long minId) {
        assertFirstItemIdGreaterThan(minId);
    }

    @Então("a resposta deve refletir o payload enviado")
    public void verifyResponseMatchesRequest() {
        assertResponseMatchesRequest();
    }

    @Então("o campo {string} deve ser {string}")
    public void verifyFieldValue(String field, String value) {
        assertFieldValue(field, value);
    }

    @Então("a lista de dados deve conter o id {long}")
    public void verifyDataContainsId(long id) {
        JsonNode data = context.getLastResponseBody().path("data");
        boolean found = false;
        for (JsonNode item : data) {
            if (item.path("id").asLong() == id) {
                found = true;
                break;
            }
        }
        org.assertj.core.api.Assertions.assertThat(found)
                .as("id %s nao encontrado na lista: %s", id, lastBody())
                .isTrue();
    }
}
