package br.com.fiap.postech.e2e.it.cucumber.hooks;

import br.com.fiap.postech.e2e.it.cucumber.context.CucumberTestContext;
import br.com.fiap.postech.e2e.it.cucumber.support.DatabaseResetService;
import br.com.fiap.postech.e2e.it.cucumber.support.SuiteBootstrap;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.util.UUID;

/**
 * Prepares each scenario: waits for the fleet once, restores the canonical baseline and starts a
 * clean context with a fresh correlation id.
 */
public class CucumberHooks {

    @Before(order = 0)
    public void setUp(Scenario scenario) {
        SuiteBootstrap.ensureReady();
        DatabaseResetService.resetAll();
        CucumberTestContext context = CucumberTestContext.getInstance();
        context.reset();
        context.setCorrelationId("e2e-" + UUID.randomUUID());
        context.put("scenarioName", scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            CucumberTestContext context = CucumberTestContext.getInstance();
            System.out.println("Scenario failed: " + scenario.getName());
            System.out.println("Last status: " + context.getLastStatusCode());
            System.out.println("Last body: " + context.getLastResponseBodyAsString());
        }
    }
}
