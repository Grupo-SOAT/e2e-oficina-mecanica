package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;

/**
 * Vehicle CRUD steps.
 */
public class VehiclesStepDefinitions extends BaseStepDefinition {

    @Dado("que o id do veiculo seja {long}")
    public void setVehicleId(long id) {
        context.setVehicleId(id);
    }

    @Dado("que o corpo do novo veiculo seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do veiculo seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os veiculos")
    public void listVehicles() {
        get(ServiceUrls.Service.MNL, "/vehicles");
    }

    @Quando("eu consultar o veiculo por id")
    public void getVehicleById() {
        get(ServiceUrls.Service.MNL, "/vehicles/" + context.getVehicleId(), java.util.Map.of());
    }

    @Quando("eu criar o veiculo")
    public void createVehicle() {
        HttpSupport.Result result = post(ServiceUrls.Service.MNL, "/vehicles", context.getRequestBody());
        if (result.status() == 201) {
            context.setCreatedVehicleId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu atualizar o veiculo")
    public void updateVehicle() {
        patch(ServiceUrls.Service.MNL, "/vehicles/" + context.getVehicleId(), context.getRequestBody());
    }

    @Quando("eu remover o veiculo")
    public void deleteVehicle() {
        delete(ServiceUrls.Service.MNL, "/vehicles/" + context.getVehicleId());
    }
}
