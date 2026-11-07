package br.com.fiap.postech.e2e.it.cucumber.support;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Applies the canonical seed through Flyway. The services own the schema; this seeder only inserts
 * reference data.
 */
public final class FlywaySeeder {

    private FlywaySeeder() {
    }

    public static void migrate() {
        dropHistory();
        Flyway.configure()
                .dataSource(DatabaseResetService.mnlUrl(),
                        DatabaseResetService.mnlUser(),
                        DatabaseResetService.mnlPassword())
                .locations("classpath:db/e2e/migration")
                .table("e2e_flyway_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();
    }

    private static void dropHistory() {
        try (Connection connection = DriverManager.getConnection(
                DatabaseResetService.mnlUrl(), DatabaseResetService.mnlUser(),
                DatabaseResetService.mnlPassword());
             PreparedStatement statement = connection.prepareStatement(
                     "DROP TABLE IF EXISTS e2e_flyway_history")) {
            statement.execute();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot reset Flyway history for the E2E seed", exception);
        }
    }
}
