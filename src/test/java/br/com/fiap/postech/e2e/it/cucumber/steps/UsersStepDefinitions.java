package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;

import java.util.Map;

/**
 * User administration steps.
 */
public class UsersStepDefinitions extends BaseStepDefinition {

    @Dado("que o id do usuario seja {long}")
    public void setUserId(long id) {
        context.setUserId(id);
    }

    @Dado("que o corpo do novo usuario seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do usuario seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os usuarios")
    public void listUsers() {
        get(ServiceUrls.Service.MNL, "/users");
    }

    @Quando("eu consultar o usuario por id")
    public void getUserById() {
        get(ServiceUrls.Service.MNL, "/users/" + context.getUserId(), Map.of());
    }

    @Quando("eu criar o usuario")
    public void createUser() {
        HttpSupport.Result result = post(ServiceUrls.Service.MNL, "/users", context.getRequestBody());
        if (result.status() == 201) {
            context.setUserId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu atualizar o usuario")
    public void updateUser() {
        patch(ServiceUrls.Service.MNL, "/users/" + context.getUserId(), context.getRequestBody());
    }

    @Quando("eu remover o usuario")
    public void deleteUser() {
        delete(ServiceUrls.Service.MNL, "/users/" + context.getUserId());
    }

    @Quando("eu resetar a senha do usuario")
    public void resetPassword() {
        HttpSupport.send("POST", ServiceUrls.Service.MNL,
                "/users/" + context.getUserId() + "/reset-password", null, Map.of(), true);
    }
}
