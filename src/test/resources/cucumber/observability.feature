# language: pt
@observability
Funcionalidade: Observabilidade da frota
  Como um operador da oficina
  Quero correlacionar requisições, métricas e traces
  Para diagnosticar a saga entre os serviços

  Cenário: Propagação do correlation id
    Dado que eu esteja devidamente logado
    Quando eu envie uma requisição com o correlation id "e2e-correlation"
    Então a resposta deve devolver o correlation id "e2e-correlation"

  Cenário: Métricas Prometheus nos quatro serviços
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    Então as métricas Prometheus devem estar disponíveis nos quatro serviços

  Cenário: Dashboard da frota provisionado
    Então o dashboard "oficina-fleet" deve estar provisionado

  Cenário: Regras de alerta provisionadas
    Então as regras de alerta "oficina-failed-requests" e "oficina-no-traffic" devem estar provisionadas

  Cenário: Trace da saga com os quatro serviços
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    E que o pagamento seja aprovado no Mercado Pago
    E que a ordem de serviço esteja no status "APPROVED"
    E que a ordem de serviço esteja na fila de execução
    Quando eu iniciar a execução da ordem de serviço
    E eu concluir a execução da ordem de serviço
    Então a trace da saga deve conter os quatro serviços
