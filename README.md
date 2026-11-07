# e2e-oficina-mecanica

Local fleet and end-to-end suite for the workshop platform.

## Fleet

```bash
docker compose up -d --build
```

Services:

| Service | Port |
|---|---|
| `mnl-oficina-mecanica` | 8080 |
| `ms-orcamentos` | 8081 |
| `ms-pedidos` | 8082 |
| `ms-expedicao` | 8083 |
| Grafana (OTel-LGTM) | 3000 |
| Prometheus | 9090 |
| Tempo | 3200 |
| WireMock (Mercado Pago) | 8089 |
| Mailpit | 8025 |

## E2E

```bash
docker compose up -d --build
./mvnw verify
```

The Cucumber suite talks to the fleet over HTTP and Kafka. It covers:

1. Authentication and role authorization in `mnl`.
2. Owners, vehicles, users, catalog services and supplies CRUD.
3. Service order lifecycle, cascade creation and service lines in `ms-pedidos`.
4. Budget pricing/audit and `BUDGET_REQUESTED`/`BUDGET_CREATED`.
5. Checkout Pro, payment webhook, rejection, idempotent refund and `@slow` timeout.
6. Execution queue enqueue/start/complete and cancellation.
7. Reporting PDF/CSV through `mnl` reading `ms-pedidos`.
8. Correlation id, Prometheus metrics, Tempo traces and Grafana provisioning.

Tags: `@smoke` for the fast authentication path, `@slow` for the payment timeout
scenario (part of the full suite, excluded from smoke).

```bash
./mvnw verify -Dcucumber.filter.tags="@smoke"
```

Data isolation: the suite truncates the service tables and re-applies the
canonical seed before each scenario. For a deterministic run start from empty
volumes:

```bash
docker compose down -v
docker compose up -d --build
./mvnw verify
```

Flyway is a test-scope dependency and applies only the seed
(`src/test/resources/db/e2e/migration`); the services still own their schema.
No business stub: only Mercado Pago is simulated by WireMock, behind the real ACL.

## Resource limits

The compose file caps every container to keep the fleet runnable on a GitHub
Actions runner:

| Container | Limit |
|---|---|
| postgres | 384 MB |
| kafka | 768 MB (256 MB heap) |
| dynamodb | 256 MB |
| mailpit | 96 MB |
| mercadopago | 256 MB |
| dynamodb-init | 128 MB |
| observability (LGTM) | 1 GB |
| mnl | 768 MB (384 MB heap) |
| ms-orcamentos / ms-pedidos / ms-expedicao | 448 MB each (192 MB heap) |

## Grafana provisioning

- Dashboard `Oficina — Fleet Overview` (`uid=oficina-fleet`): request rate, failed requests,
  p95 latency, traces by service and a Tempo trace-search panel.
- Alert rules `oficina-failed-requests` and `oficina-no-traffic` in the `Oficina` folder.
- Provisioned from `observability/grafana/` and asserted by the Cucumber
  `observability.feature`, including a check that the cascade trace is queryable
  in Tempo and spans all four services.
