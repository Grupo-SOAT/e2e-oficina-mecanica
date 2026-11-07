package br.com.fiap.postech.e2e.it.cucumber.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-scenario state shared by the step definitions.
 *
 * <p>Cucumber instantiates step definition classes per scenario, so the state lives in a single
 * static instance that the hooks reset before each scenario.</p>
 */
public final class CucumberTestContext {

    private static final CucumberTestContext INSTANCE = new CucumberTestContext();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String currentUser;
    private String currentRole;
    private String authToken;
    private String correlationId;
    private int lastStatusCode;
    private JsonNode lastResponseBody = NullNode.getInstance();
    private byte[] lastResponseBytes = new byte[0];
    private String lastResponseContentType;
    private String lastResponseContentDisposition;
    private String lastCorrelationId;
    private Integer pageSize;
    private String cursor;
    private String filterName;
    private String filterValue;
    private String requestBody;

    private Long ownerId;
    private Long vehicleId;
    private Long userId;
    private Long catalogServiceId;
    private Long supplyId;
    private Long serviceOrderId;
    private Long serviceLineId;
    private Long budgetId;

    private Long createdOwnerId;
    private Long createdVehicleId;
    private Long createdServiceOrderId;
    private Long createdServiceLineId;
    private Long createdBudgetId;
    private Long createdCatalogServiceId;

    private final Map<String, Object> scenarioData = new LinkedHashMap<>();

    private CucumberTestContext() {
    }

    public static synchronized CucumberTestContext getInstance() {
        return INSTANCE;
    }

    public void reset() {
        currentUser = null;
        currentRole = null;
        authToken = null;
        correlationId = null;
        lastStatusCode = 0;
        lastResponseBody = NullNode.getInstance();
        lastResponseBytes = new byte[0];
        lastResponseContentType = null;
        lastResponseContentDisposition = null;
        lastCorrelationId = null;
        pageSize = null;
        cursor = null;
        filterName = null;
        filterValue = null;
        requestBody = null;
        ownerId = null;
        vehicleId = null;
        userId = null;
        catalogServiceId = null;
        supplyId = null;
        serviceOrderId = null;
        serviceLineId = null;
        budgetId = null;
        createdOwnerId = null;
        createdVehicleId = null;
        createdServiceOrderId = null;
        createdServiceLineId = null;
        createdBudgetId = null;
        createdCatalogServiceId = null;
        scenarioData.clear();
    }

    public void setLastResponseBody(String rawBody) {
        if (rawBody == null) {
            lastResponseBody = NullNode.getInstance();
            return;
        }
        try {
            lastResponseBody = OBJECT_MAPPER.readTree(rawBody);
        } catch (Exception ignored) {
            lastResponseBody = TextNode.valueOf(rawBody);
        }
    }

    public String getLastResponseBodyAsString() {
        if (lastResponseBody == null || lastResponseBody.isNull()) {
            return "";
        }
        if (lastResponseBody.isTextual()) {
            return lastResponseBody.asText();
        }
        return lastResponseBody.toString();
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(int lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }

    public JsonNode getLastResponseBody() {
        return lastResponseBody;
    }

    public void setLastResponseBody(JsonNode lastResponseBody) {
        this.lastResponseBody = lastResponseBody;
    }

    public byte[] getLastResponseBytes() {
        return lastResponseBytes;
    }

    public void setLastResponseBytes(byte[] lastResponseBytes) {
        this.lastResponseBytes = lastResponseBytes;
    }

    public String getLastResponseContentType() {
        return lastResponseContentType;
    }

    public void setLastResponseContentType(String lastResponseContentType) {
        this.lastResponseContentType = lastResponseContentType;
    }

    public String getLastResponseContentDisposition() {
        return lastResponseContentDisposition;
    }

    public String getLastCorrelationId() {
        return lastCorrelationId;
    }

    public void setLastCorrelationId(String lastCorrelationId) {
        this.lastCorrelationId = lastCorrelationId;
    }

    public void setLastResponseContentDisposition(String lastResponseContentDisposition) {
        this.lastResponseContentDisposition = lastResponseContentDisposition;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCatalogServiceId() {
        return catalogServiceId;
    }

    public void setCatalogServiceId(Long catalogServiceId) {
        this.catalogServiceId = catalogServiceId;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public Long getServiceOrderId() {
        return serviceOrderId;
    }

    public void setServiceOrderId(Long serviceOrderId) {
        this.serviceOrderId = serviceOrderId;
    }

    public Long getServiceLineId() {
        return serviceLineId;
    }

    public void setServiceLineId(Long serviceLineId) {
        this.serviceLineId = serviceLineId;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public Long getCreatedOwnerId() {
        return createdOwnerId;
    }

    public void setCreatedOwnerId(Long createdOwnerId) {
        this.createdOwnerId = createdOwnerId;
    }

    public Long getCreatedVehicleId() {
        return createdVehicleId;
    }

    public void setCreatedVehicleId(Long createdVehicleId) {
        this.createdVehicleId = createdVehicleId;
    }

    public Long getCreatedServiceOrderId() {
        return createdServiceOrderId;
    }

    public void setCreatedServiceOrderId(Long createdServiceOrderId) {
        this.createdServiceOrderId = createdServiceOrderId;
    }

    public Long getCreatedServiceLineId() {
        return createdServiceLineId;
    }

    public void setCreatedServiceLineId(Long createdServiceLineId) {
        this.createdServiceLineId = createdServiceLineId;
    }

    public Long getCreatedBudgetId() {
        return createdBudgetId;
    }

    public void setCreatedBudgetId(Long createdBudgetId) {
        this.createdBudgetId = createdBudgetId;
    }

    public Long getCreatedCatalogServiceId() {
        return createdCatalogServiceId;
    }

    public void setCreatedCatalogServiceId(Long createdCatalogServiceId) {
        this.createdCatalogServiceId = createdCatalogServiceId;
    }

    public void put(String key, Object value) {
        scenarioData.put(key, value);
    }

    public Object get(String key) {
        return scenarioData.get(key);
    }

    public String getString(String key) {
        Object value = scenarioData.get(key);
        return value == null ? null : value.toString();
    }
}
