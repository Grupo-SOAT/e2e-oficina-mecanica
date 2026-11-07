package br.com.fiap.postech.e2e.it.cucumber.support;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Reads the budget audit tables owned by {@code ms-orcamentos}.
 */
public final class BudgetProbe {

    private BudgetProbe() {
    }

    public static long budgetId(long serviceOrderId) {
        return queryLong("select id from budgets where service_order_id = ? order by id desc limit 1",
                serviceOrderId);
    }

    public static BigDecimal totalAmount(long serviceOrderId) {
        return queryDecimal("select total_amount from budgets where service_order_id = ? "
                + "order by id desc limit 1", serviceOrderId);
    }

    public static int itemCount(long serviceOrderId) {
        return (int) queryLong("select count(*) from budget_items where budget_id = "
                + "(select id from budgets where service_order_id = ? order by id desc limit 1)",
                serviceOrderId);
    }

    public static boolean itemsHavePriceFetchedAt(long serviceOrderId) {
        long missing = queryLong("select count(*) from budget_items where budget_id = "
                + "(select id from budgets where service_order_id = ? order by id desc limit 1) "
                + "and price_fetched_at is null", serviceOrderId);
        return missing == 0;
    }

    public static boolean suppliesHavePriceFetchedAt(long serviceOrderId) {
        long missing = queryLong("select count(*) from budget_item_supplies where budget_item_id in "
                + "(select id from budget_items where budget_id = "
                + "(select id from budgets where service_order_id = ? order by id desc limit 1)) "
                + "and price_fetched_at is null", serviceOrderId);
        return missing == 0;
    }

    public static int forServiceOrder(long serviceOrderId) {
        return (int) queryLong("select count(*) from budgets where service_order_id = ?", serviceOrderId);
    }

    public static BigDecimal unitPrice(long serviceOrderId, long catalogServiceId) {
        try (Connection connection = DriverManager.getConnection(
                DatabaseResetService.orcamentosUrl(),
                DatabaseResetService.orcamentosUser(),
                DatabaseResetService.orcamentosPassword());
             PreparedStatement statement = connection.prepareStatement(
                     "select unit_price from budget_items where catalog_service_id = ? and budget_id = "
                             + "(select id from budgets where service_order_id = ? order by id desc limit 1)")) {
            statement.setLong(1, catalogServiceId);
            statement.setLong(2, serviceOrderId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getBigDecimal(1) : null;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Budget item query failed", exception);
        }
    }

    private static long queryLong(String sql, long parameter) {
        try (Connection connection = DriverManager.getConnection(
                DatabaseResetService.orcamentosUrl(),
                DatabaseResetService.orcamentosUser(),
                DatabaseResetService.orcamentosPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0L;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Budget query failed: " + sql, exception);
        }
    }

    private static BigDecimal queryDecimal(String sql, long parameter) {
        try (Connection connection = DriverManager.getConnection(
                DatabaseResetService.orcamentosUrl(),
                DatabaseResetService.orcamentosUser(),
                DatabaseResetService.orcamentosPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getBigDecimal(1) : null;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Budget query failed: " + sql, exception);
        }
    }
}
