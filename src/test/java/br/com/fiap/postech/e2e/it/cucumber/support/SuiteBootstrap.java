package br.com.fiap.postech.e2e.it.cucumber.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Boots the suite once: waits for the fleet, applies the Flyway seed and clears the databases.
 */
public final class SuiteBootstrap {

    private static final AtomicBoolean READY = new AtomicBoolean(false);

    private SuiteBootstrap() {
    }

    public static synchronized void ensureReady() {
        if (READY.get()) {
            return;
        }
        waitForFleet();
        FlywaySeeder.migrate();
        DatabaseResetService.resetAll();
        READY.set(true);
    }

    private static void waitForFleet() {
        List<ServiceUrls.Service> services = List.of(
                ServiceUrls.Service.MNL,
                ServiceUrls.Service.ORCAMENTOS,
                ServiceUrls.Service.PEDIDOS,
                ServiceUrls.Service.EXPEDICAO);
        for (ServiceUrls.Service service : services) {
            waitForHealth(service);
        }
    }

    private static void waitForHealth(ServiceUrls.Service service) {
        String url = ServiceUrls.baseUrl(service) + "/actuator/health";
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        long deadline = System.nanoTime() + Duration.ofSeconds(180).toNanos();
        int lastStatus = 0;
        while (System.nanoTime() < deadline) {
            try {
                HttpResponse<Void> response = client.send(
                        HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
                        HttpResponse.BodyHandlers.discarding());
                lastStatus = response.statusCode();
                if (lastStatus == 200) {
                    return;
                }
            } catch (Exception ignored) {
                lastStatus = 0;
            }
            sleep();
        }
        throw new IllegalStateException("Fleet is not ready: " + url + " last status " + lastStatus
                + ". Run 'docker compose up -d --build' before the E2E suite.");
    }

    private static void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
