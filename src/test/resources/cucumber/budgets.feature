# language: pt
@orcamentos
Funcionalidade: Orçamentos e pagamentos
  Como um atendente da oficina
  Quero precificar a ordem de serviço e cobrar o cliente
  Para liberar a execução do serviço

  # === PRECIFICAÇÃO ===

  Cenário: Precificação do orçamento a partir das referências da OS
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com dois serviços
    E que a inspeção da ordem de serviço seja concluída
    Então o orçamento da ordem de serviço deve ter sido persistido
    E o orçamento da ordem de serviço deve conter 2 itens
    E o total do orçamento da ordem de serviço deve ser maior que zero
    E a auditoria de preço do orçamento deve estar preenchida
    E o valor unitário do item de catálogo 1 deve ser "120.00"
    E um evento do tipo "BUDGET_CREATED" deve ser publicado no tópico "budget-events" para a ordem de serviço

  Cenário: Orçamento aplicado à ordem de serviço
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    Quando eu consultar a ordem de serviço por id
    Então devo receber uma resposta com status "200"
    E o campo decimal "estimatedAmount" da ordem de serviço não deve ser vazio
    E o campo "statusLabel" deve ser "Aguardando Aprovação"
