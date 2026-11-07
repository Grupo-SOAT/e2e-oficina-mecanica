package br.com.fiap.postech.e2e.it.cucumber.support;

/**
 * Resolves the base URL of each service under test.
 */
public final class ServiceUrls {

    public enum Service {
        MNL,
        PEDIDOS,
        ORCAMENTOS,
        EXPEDICAO
    }

    private ServiceUrls() {
    }

    public static String baseUrl(Service service) {
        return switch (service) {
            case MNL -> property("e2e.mnl.url", "E2E_MNL_URL", "http://localhost:8080");
            case PEDIDOS -> property("e2e.pedidos.url", "E2E_PEDIDOS_URL", "http://localhost:8082");
            case ORCAMENTOS -> property("e2e.orcamentos.url", "E2E_ORCAMENTOS_URL", "http://localhost:8081");
            case EXPEDICAO -> property("e2e.expedicao.url", "E2E_EXPEDICAO_URL", "http://localhost:8083");
        };
    }

    public static String tempo() {
        return property("e2e.tempo.url", "E2E_TEMPO_URL", "http://localhost:3200");
    }

    public static String grafana() {
        return property("e2e.grafana.url", "E2E_GRAFANA_URL", "http://localhost:3000");
    }

    public static String wireMock() {
        return property("e2e.wiremock.url", "E2E_WIREMOCK_URL", "http://localhost:8089");
    }

    private static String property(String systemProperty, String envVariable, String fallback) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(envVariable);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
