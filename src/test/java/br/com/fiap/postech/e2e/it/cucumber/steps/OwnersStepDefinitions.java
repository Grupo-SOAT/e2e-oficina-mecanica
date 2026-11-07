package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;

/**
 * Owner (client) CRUD steps.
 */
public class OwnersStepDefinitions extends BaseStepDefinition {

    @Dado("que o id do cliente seja {long}")
    public void setClientId(long id) {
        context.setOwnerId(id);
    }

    @Dado("que o corpo do novo cliente seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do cliente seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os clientes")
    public void listClients() {
        get(ServiceUrls.Service.MNL, "/owners");
    }

    @Quando("eu consultar o cliente por id")
    public void getClientById() {
        get(ServiceUrls.Service.MNL, "/owners/" + context.getOwnerId(), java.util.Map.of());
    }

    @Quando("eu criar o cliente")
    public void createClient() {
        HttpSupport.Result result = post(ServiceUrls.Service.MNL, "/owners", context.getRequestBody());
        if (result.status() == 201) {
            context.setCreatedOwnerId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu atualizar o cliente")
    public void updateClient() {
        patch(ServiceUrls.Service.MNL, "/owners/" + context.getOwnerId(), context.getRequestBody());
    }

    @Quando("eu remover o cliente")
    public void deleteClient() {
        delete(ServiceUrls.Service.MNL, "/owners/" + context.getOwnerId());
    }
}
