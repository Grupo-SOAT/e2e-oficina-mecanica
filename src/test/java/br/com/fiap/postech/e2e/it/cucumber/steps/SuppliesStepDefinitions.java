package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;

import java.util.Map;

/**
 * Supply (insumo) CRUD steps.
 */
public class SuppliesStepDefinitions extends BaseStepDefinition {

    @Dado("que o id do insumo seja {long}")
    public void setSupplyId(long id) {
        context.setSupplyId(id);
    }

    @Dado("que o corpo do novo insumo seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setSupplyBody(table);
    }

    @Dado("que o corpo de atualização do insumo seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setSupplyBody(table);
    }

    private void setSupplyBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os insumos")
    public void listSupplies() {
        get(ServiceUrls.Service.MNL, "/supplies");
    }

    @Quando("eu consultar o insumo por id")
    public void getSupplyById() {
        get(ServiceUrls.Service.MNL, "/supplies/" + context.getSupplyId(), Map.of());
    }

    @Quando("eu criar o insumo")
    public void createSupply() {
        HttpSupport.Result result = post(ServiceUrls.Service.MNL, "/supplies", context.getRequestBody());
        if (result.status() == 201) {
            context.setSupplyId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu atualizar o insumo")
    public void updateSupply() {
        patch(ServiceUrls.Service.MNL, "/supplies/" + context.getSupplyId(), context.getRequestBody());
    }

    @Quando("eu remover o insumo")
    public void deleteSupply() {
        delete(ServiceUrls.Service.MNL, "/supplies/" + context.getSupplyId());
    }
}
