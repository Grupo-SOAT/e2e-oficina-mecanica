# language: pt
@payments
Funcionalidade: Checkout, pagamentos e reembolsos
  Como um atendente da oficina
  Quero cobrar o cliente e estornar quando necessário
  Para manter o financeiro consistente

  # === CHECKOUT PRO ===

  Cenário: Criação da preferência de checkout do orçamento
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    Quando eu criar a preferência de checkout do orçamento
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "preferenceId"

  Cenário: Reutilização da preferência ativa
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    Então a preferência de checkout deve ser reutilizada

  # === WEBHOOK DO MERCADO PAGO ===

  Cenário: Pagamento aprovado
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    Quando o pagamento for aprovado no Mercado Pago
    Então o pagamento deve estar com status "APPROVED"
    E um evento do tipo "PAYMENT_APPROVED" deve ser publicado no tópico "payment-events" para a ordem de serviço atual

  Cenário: Pagamento recusado
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    Quando o pagamento for recusado no Mercado Pago
    Então o pagamento deve estar com status "REJECTED"
    E a ordem de serviço deve estar cancelada

  Cenário: Pagamento pendente não publica evento
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    Quando o pagamento for enviado com status "pending"
    Então o pagamento deve estar com status "PENDING"

  # === REEMBOLSO ===

  Cenário: Reembolso total idempotente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    E que o pagamento seja aprovado no Mercado Pago
    Quando eu solicitar o reembolso 2 vezes
    Então os reembolsos devem ser idempotentes
    E o valor do reembolso deve ser igual ao valor do pagamento
    E o pagamento deve estar com status "REFUNDED"

  Cenário: Reembolso exige pagamento aprovado
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    Quando eu solicitar o reembolso 1 vezes
    Então devo receber uma resposta com status "409"

  # === TIMEOUT ===

  @slow
  Cenário: Timeout do pagamento compensa a ordem de serviço
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a inspeção da ordem de serviço seja concluída
    E que eu crie a preferência de checkout do orçamento
    E que o pagamento esteja expirado
    Então o pagamento deve ser expirado
    E a ordem de serviço deve estar cancelada
    E um evento do tipo "PAYMENT_TIMEOUT" deve ser publicado no tópico "payment-events" para a ordem de serviço atual
