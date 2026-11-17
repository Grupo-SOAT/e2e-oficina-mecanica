# e2e-oficina-mecanica

Frota local e suíte end-to-end da plataforma da oficina mecânica.

## Frota

```bash
docker compose up -d --build
```

Serviços:

| Serviço | Porta |
|---|---|
| `mnl-oficina-mecanica` | 8080 |
| `ms-orcamentos` | 8081 |
| `ms-pedidos` | 8082 |
| `ms-expedicao` | 8083 |
| `postgres-mnl` | 5432 |
| `postgres-pedidos` | 5434 |
| `postgres-orcamentos` | 5435 |
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

A suíte Cucumber conversa com a frota por HTTP e Kafka. Ela cobre:

1. Autenticação e autorização por papel no `mnl`.
2. CRUD de clientes, veículos, usuários, serviços de catálogo e insumos.
3. Ciclo de vida da ordem de serviço, criação em cascata e linhas de serviço no `ms-pedidos`.
4. Precificação/auditoria do orçamento e `BUDGET_REQUESTED`/`BUDGET_CREATED`.
5. Checkout Pro, webhook de pagamento, rejeição, reembolso idempotente e timeout `@slow`.
6. Enfileiramento, início, conclusão e cancelamento na fila de execução.
7. Relatórios PDF/CSV via `mnl` lendo do `ms-pedidos`.
8. Correlation id, métricas Prometheus, traces Tempo e provisionamento Grafana.

Tags: `@smoke` para o caminho rápido de autenticação, `@slow` para o cenário de
timeout de pagamento (faz parte da suíte completa, excluído do smoke).

```bash
./mvnw verify -Dcucumber.filter.tags="@smoke"
```

Isolamento de dados: cada serviço tem seu próprio container PostgreSQL
(`postgres-mnl`, `postgres-pedidos`, `postgres-orcamentos`), então nenhum serviço
recebe credenciais do banco de outro serviço. A suíte trunca as tabelas de cada
serviço e reaplica o seed canônico antes de cada cenário. Para uma execução
determinística, comece com volumes vazios:

```bash
docker compose down -v
docker compose up -d --build
./mvnw verify
```

Flyway é uma dependência de escopo de teste e aplica apenas o seed
(`src/test/resources/db/e2e/migration`); os serviços continuam donos do próprio
schema. Sem stub de negócio: apenas o Mercado Pago é simulado pelo WireMock, atrás
da ACL real.

## Limites de recursos

O compose limita cada container para manter a frota executável em um runner do
GitHub Actions:

| Container | Limite |
|---|---|
| postgres-mnl / postgres-pedidos / postgres-orcamentos | 256 MB cada |
| kafka | 768 MB (256 MB de heap) |
| dynamodb | 256 MB |
| mailpit | 96 MB |
| mercadopago | 256 MB |
| dynamodb-init | 128 MB |
| observability (LGTM) | 2 GB |
| mnl | 768 MB (384 MB de heap) |
| ms-orcamentos / ms-pedidos / ms-expedicao | 448 MB cada (192 MB de heap) |

O `observability` usa 2 GB para absorver a ingestão de traces da suíte completa
sem estourar a memória e derrubar Grafana/Tempo.

## Provisionamento do Grafana

- Dashboard `Oficina — Fleet Overview` (`uid=oficina-fleet`): taxa de requisições,
  requisições com falha, latência p95, traces por serviço e um painel de busca de
  traces do Tempo.
- Regras de alerta `oficina-failed-requests` e `oficina-no-traffic` na pasta `Oficina`.
- Provisionados a partir de `observability/grafana/` e verificados pela feature
  Cucumber `observability.feature`, incluindo a checagem de que o trace da cascata
  é consultável no Tempo e abrange os quatro serviços.
