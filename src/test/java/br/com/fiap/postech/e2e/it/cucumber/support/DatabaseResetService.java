package br.com.fiap.postech.e2e.it.cucumber.support;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Restores the databases to the canonical baseline before each scenario.
 *
 * <p>The schema belongs to the services. This class only truncates the tables the scenarios own
 * and re-applies the same seed script Flyway applied at bootstrap.</p>
 */
public final class DatabaseResetService {

    private static final String SEED_RESOURCE = "db/e2e/migration/V1__canonical_seed.sql";

    private DatabaseResetService() {
    }

    public static void resetAll() {
        resetMNL();
        resetPedidos();
        resetOrcamentos();
        DynamoProbe.clear();
    }

    private static void resetMNL() {
        String sql = """
                TRUNCATE TABLE service_supplies, catalog_services, supplies, vehicles, owners, users
                RESTART IDENTITY CASCADE
                """;
        execute(mnlUrl(), mnlUser(), mnlPassword(), sql);
        executeScript(mnlUrl(), mnlUser(), mnlPassword(), SEED_RESOURCE);
    }

    private static void resetPedidos() {
        execute(pedidosUrl(), pedidosUser(), pedidosPassword(),
                "TRUNCATE TABLE service_lines, service_orders CASCADE");
    }

    private static void resetOrcamentos() {
        execute(orcamentosUrl(), orcamentosUser(), orcamentosPassword(),
                "TRUNCATE TABLE refunds, payments, budget_item_supplies, budget_items, budgets, "
                        + "budget_approval_tokens RESTART IDENTITY CASCADE");
    }

    private static void execute(String url, String user, String password, String sql) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot reset the E2E database: " + sql, exception);
        }
    }

    private static void executeScript(String url, String user, String password, String resource) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Seed resource not found: " + resource);
            }
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            execute(url, user, password, sql);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot apply the E2E seed: " + resource, exception);
        }
    }

    public static String mnlUrl() {
        return env("E2E_MNL_DB_URL", "jdbc:postgresql://localhost:5432/oficina_mnl");
    }

    public static String mnlUser() {
        return env("E2E_MNL_DB_USER", "mnl");
    }

    public static String mnlPassword() {
        return env("E2E_MNL_DB_PASSWORD", "mnl");
    }

    public static String pedidosUrl() {
        return env("E2E_PEDIDOS_DB_URL", "jdbc:postgresql://localhost:5432/oficina_ms_pedidos");
    }

    public static String pedidosUser() {
        return env("E2E_PEDIDOS_DB_USER", "ms_pedidos");
    }

    public static String pedidosPassword() {
        return env("E2E_PEDIDOS_DB_PASSWORD", "ms_pedidos");
    }

    public static String orcamentosUrl() {
        return env("E2E_ORCAMENTOS_DB_URL", "jdbc:postgresql://localhost:5432/oficina_ms_orcamentos");
    }

    public static String orcamentosUser() {
        return env("E2E_ORCAMENTOS_DB_USER", "ms_orcamentos");
    }

    public static String orcamentosPassword() {
        return env("E2E_ORCAMENTOS_DB_PASSWORD", "ms_orcamentos");
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
