package br.com.fiap.postech.e2e.it.cucumber.support;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Reads and prepares payment/refund state owned by {@code ms-orcamentos}.
 */
public final class PaymentProbe {

    private PaymentProbe() {
    }

    public static long paymentId(long serviceOrderId) {
        return queryLong("select id from payments where service_order_id = ? order by id desc limit 1",
                serviceOrderId);
    }

    public static String paymentStatus(long serviceOrderId) {
        return queryString("select status from payments where service_order_id = ? order by id desc limit 1",
                serviceOrderId);
    }

    public static BigDecimal paymentAmount(long serviceOrderId) {
        return queryDecimal("select amount from payments where service_order_id = ? "
                + "order by id desc limit 1", serviceOrderId);
    }

    public static String preferenceId(long serviceOrderId) {
        return queryString("select preference_id from payments where service_order_id = ? "
                + "order by id desc limit 1", serviceOrderId);
    }

    public static BigDecimal refundAmount(long serviceOrderId) {
        return queryDecimal("select amount from refunds where service_order_id = ? "
                + "order by id desc limit 1", serviceOrderId);
    }

    public static int refundCount(long serviceOrderId) {
        return (int) queryLong("select count(*) from refunds where service_order_id = ?", serviceOrderId);
    }

    public static void expirePayment(long serviceOrderId) {
        execute("update payments set expires_at = CURRENT_TIMESTAMP - INTERVAL '1 minute' "
                + "where service_order_id = ?", serviceOrderId);
    }

    private static void execute(String sql, long parameter) {
        try (Connection connection = DriverManager.getConnection(
                DatabaseResetService.orcamentosUrl(),
                DatabaseResetService.orcamentosUser(),
                DatabaseResetService.orcamentosPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Payment update failed: " + sql, exception);
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
            throw new IllegalStateException("Payment query failed: " + sql, exception);
        }
    }

    private static String queryString(String sql, long parameter) {
        try (Connection connection = DriverManager.getConnection(
                DatabaseResetService.orcamentosUrl(),
                DatabaseResetService.orcamentosUser(),
                DatabaseResetService.orcamentosPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Payment query failed: " + sql, exception);
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
            throw new IllegalStateException("Payment query failed: " + sql, exception);
        }
    }
}
