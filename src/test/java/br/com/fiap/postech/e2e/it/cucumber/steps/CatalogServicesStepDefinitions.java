package br.com.fiap.postech.e2e.it.cucumber.steps;

import br.com.fiap.postech.e2e.it.cucumber.support.HttpSupport;
import br.com.fiap.postech.e2e.it.cucumber.support.ServiceUrls;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;

import java.util.Map;

/**
 * Catalog service CRUD steps. The legacy table names ({@code nome}, {@code descricao}) are
 * translated to the API contract.
 */
public class CatalogServicesStepDefinitions extends BaseStepDefinition {

    private static final Map<String, Long> SUPPLY_IDS_BY_SKU = Map.of(
            "SKU-001", 1L,
            "SKU-002", 2L,
            "SKU-003", 3L);

    @Dado("que o id do serviço catalogado seja {long}")
    public void setCatalogServiceId(long id) {
        context.setCatalogServiceId(id);
    }

    @Dado("que o corpo do novo serviço catalogado seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setCatalogServiceBody(table);
    }

    @Dado("que o corpo de atualização do serviço catalogado seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setCatalogServiceBody(table);
    }

    private void setCatalogServiceBody(DataTable table) throws Exception {
        setJsonBody(table);
        JsonNode root = objectMapper.readTree(context.getRequestBody());
        if (!(root instanceof ObjectNode objectNode)) {
            return;
        }
        remap(objectNode, "nome", "name");
        remap(objectNode, "descricao", "description");
        if (objectNode.has("name") && objectNode.get("name").isNull()) {
            objectNode.put("name", "");
        }
        if (objectNode.has("description") && objectNode.get("description").isNull()) {
            objectNode.put("description", "");
        }
        objectNode.set("neededSupplies",
                parseNeededSupplies(objectNode.path("neededSupplies").asText(null)));
        context.setRequestBody(objectMapper.writeValueAsString(objectNode));
    }

    private void remap(ObjectNode node, String source, String target) {
        if (node.has(source) && !node.has(target)) {
            node.set(target, node.get(source));
            node.remove(source);
        }
    }

    private ArrayNode parseNeededSupplies(String raw) {
        ArrayNode supplies = objectMapper.createArrayNode();
        if (raw == null || raw.isBlank()) {
            return supplies;
        }
        for (String item : raw.split(";")) {
            String trimmed = item.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(",", -1);
            String sku = parts.length > 0 ? parts[0].trim() : "";
            ObjectNode supply = objectMapper.createObjectNode();
            supply.put("idSupply", SUPPLY_IDS_BY_SKU.getOrDefault(sku, 9999L));
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                supply.put("note", parts[1].trim());
            }
            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                supply.put("quantity", Integer.parseInt(parts[2].trim()));
            }
            supplies.add(supply);
        }
        return supplies;
    }

    @Quando("eu listar os serviços catalogados")
    public void listCatalogServices() {
        get(ServiceUrls.Service.MNL, "/catalog/services");
    }

    @Quando("eu consultar o serviço catalogado por id")
    public void getCatalogServiceById() {
        get(ServiceUrls.Service.MNL, "/catalog/services/" + context.getCatalogServiceId(), Map.of());
    }

    @Quando("eu criar o serviço catalogado")
    public void createCatalogService() {
        HttpSupport.Result result = post(ServiceUrls.Service.MNL, "/catalog/services",
                context.getRequestBody());
        if (result.status() == 201) {
            context.setCreatedCatalogServiceId(context.getLastResponseBody().path("id").asLong());
        }
    }

    @Quando("eu atualizar o serviço catalogado")
    public void updateCatalogService() {
        patch(ServiceUrls.Service.MNL, "/catalog/services/" + context.getCatalogServiceId(),
                context.getRequestBody());
    }

    @Quando("eu remover o serviço catalogado")
    public void deleteCatalogService() {
        delete(ServiceUrls.Service.MNL, "/catalog/services/" + context.getCatalogServiceId());
    }
}
