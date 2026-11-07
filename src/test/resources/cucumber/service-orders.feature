# language: pt
@serviceOrders
Funcionalidade: Gerenciamento de Ordens de Serviço
  Como um administrador da oficina
  Quero gerenciar ordens de serviço e seus fluxos
  Para controlar o atendimento do veículo e o orçamento do cliente

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de ordens de serviço
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de ordens de serviço por status
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o filtro status seja "PENDING"
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 ordens de serviço
    E a resposta deve conter ao menos uma ordem de serviço com status "PENDING"
    E a resposta deve conter ao menos uma ordem de serviço com statusLabel "Recebida"

  Cenário: Paginação sem resultados deve retornar no content
    Dado que eu esteja devidamente logado
    E que o filtro status seja "DELIVERED"
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "204"

  Cenário: Paginação de ordens de serviço com cursor
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que exista uma ordem de serviço com um serviço
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  Cenário: Listagem padrão deve excluir ordens de serviço concluídas
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "COMPLETED"
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "204"

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de ordem de serviço por ID existente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    Quando eu consultar a ordem de serviço por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "clientId"
    E a resposta deve conter o campo "vehicleId"
    E a resposta deve conter o campo "description"
    E a resposta deve conter o campo "status"
    E a resposta deve conter o campo "statusLabel"
    E a resposta deve conter o campo "estimatedAmount"

  Cenário: Detalhamento de ordem de serviço por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    Quando eu consultar a ordem de serviço por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de ordem de serviço com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | clientId | vehicleId | description            |
      | 1        | 1         | Troca de óleo e filtro |
    Quando eu criar a ordem de serviço
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "status"
    E o campo "statusLabel" deve ser "Recebida"
    E a resposta deve refletir o payload enviado

  Esquema do Cenário: Cadastro de ordem de serviço com ids inexistentes
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | clientId   | vehicleId   | description   |
      | <clientId> | <vehicleId> | <description> |
    Quando eu criar a ordem de serviço
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | clientId | vehicleId | description            | reason                          |
      | 99999    | 1         | Troca de óleo e filtro | SERVICE_ORDER_CLIENT_NOT_FOUND  |
      | 1        | 99999     | Troca de óleo e filtro | SERVICE_ORDER_VEHICLE_NOT_FOUND |

  # === CADASTRO EM CASCATA ===

  Cenário: Cadastro de ordem de serviço em cascata com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | ownerId | vehicleId | description            | catalogServiceIds |
      | 1       | 1         | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "order.id"
    E a resposta deve conter o campo "order.status"
    E a resposta deve conter o campo "order.description"
    E o campo "order.statusLabel" deve ser "Recebida"

  Esquema do Cenário: Cadastro de ordem de serviço em cascata com ids inexistentes
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | ownerId   | vehicleId   | description   | catalogServiceIds   |
      | <ownerId> | <vehicleId> | <description> | <catalogServiceIds> |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "<code>"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | ownerId | vehicleId | description            | catalogServiceIds | reason                          | code |
      | 99999   | 1         | Troca de óleo e filtro | [1]               | SERVICE_ORDER_CLIENT_NOT_FOUND  | 400  |
      | 1       | 99999     | Troca de óleo e filtro | [1]               | SERVICE_ORDER_VEHICLE_NOT_FOUND | 400  |
      | 1       | 1         | Troca de óleo e filtro | [99999]           | CATALOG_SERVICE_NOT_FOUND       | 404  |

  Esquema do Cenário: Cadastro de ordem de serviço em cascata com dados duplicados
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | owner                                                                                                                                                          | vehicle                                                                                     | description            | catalogServiceIds |
      | {"name": "Creuza", "document": "09642137020", "documentType": "CPF", "phone": "1199", "email": "creuza@email.com"} | {"licensePlate": "<licensePlate>", "brand": "Geep", "model": "Renegade", "year": 2021, "color": "Prata"} | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | licensePlate | reason                                    |
      | ABC-1234     | VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE |

  Cenário: Cadastro de ordem de serviço em cascata sem dados de veículo
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | ownerId | description            | catalogServiceIds |
      | 1       | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_VEHICLE_DATA_ABSENT"

  Cenário: Cadastro de ordem de serviço em cascata sem dados de cliente
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | vehicle                                                                                            | description            | catalogServiceIds |
      | {"licensePlate": "KDP-9887", "brand": "Geep", "model": "Renegade", "year": 2021, "color": "Prata"} | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "VEHICLE_OWNER_DATA_ABSENT"

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de ordem de serviço existente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "AWAITING_APPROVAL"
    E que o corpo de atualização da ordem de serviço seja:
      | clientId | vehicleId | description                     | status   |
      | 1        | 1         | Troca de óleo, filtro e revisão | APPROVED |
    Quando eu atualizar a ordem de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado
    E o campo "statusLabel" deve ser "Aprovada"

  Esquema do Cenário: Atualização de ordem de serviço com payload inválido
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "AWAITING_APPROVAL"
    E que o corpo de atualização da ordem de serviço seja:
      | clientId   | vehicleId   | description   | status   |
      | <clientId> | <vehicleId> | <description> | APPROVED |
    Quando eu atualizar a ordem de serviço
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | clientId | vehicleId | description            | reason                          |
      | 99999    | 1         | Troca de óleo e filtro | SERVICE_ORDER_CLIENT_NOT_FOUND  |
      | 1        | 99999     | Troca de óleo e filtro | SERVICE_ORDER_VEHICLE_NOT_FOUND |

  Cenário: Atualização de ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o corpo de atualização da ordem de serviço seja:
      | clientId | vehicleId | description    | status   |
      | 1        | 1         | Nao encontrada | APPROVED |
    Quando eu atualizar a ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === EXCLUSÃO ===

  Cenário: Exclusão de ordem de serviço existente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    Quando eu remover a ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    Quando eu remover a ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === PROGRESSO DA ORDEM DE SERVIÇO ===

  Cenário: Registrar ação START_INSPECTION
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action           | additionalInfo       | relatedServiceId |
      | START_INSPECTION | Recebida na recepcao |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E o status da ordem de serviço deve ser "IN_INSPECTION"

  Cenário: Registrar ação COMPLETE_INSPECTION
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "IN_INSPECTION"
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action              | additionalInfo      | relatedServiceId |
      | COMPLETE_INSPECTION | Inspecao finalizada |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E o status da ordem de serviço deve ser "AWAITING_APPROVAL"

  Cenário: Registrar ação START_SERVICE
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "APPROVED"
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action        | additionalInfo          | relatedServiceId |
      | START_SERVICE | Inicio da troca de oleo |                  |
    E que o serviço relacionado seja o serviço da ordem de serviço
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E o status do serviço da ordem de serviço deve ser "IN_PROGRESS"

  Cenário: Registrar ação COMPLETE_SERVICE
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "IN_PROGRESS"
    E que o status do serviço da ordem de serviço seja "IN_PROGRESS"
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action           | additionalInfo    | relatedServiceId |
      | COMPLETE_SERVICE | Serviço concluido |                  |
    E que o serviço relacionado seja o serviço da ordem de serviço
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E o status do serviço da ordem de serviço deve ser "COMPLETED"

  Cenário: Registrar ação CANCEL_SERVICE
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action         | additionalInfo    | relatedServiceId |
      | CANCEL_SERVICE | Serviço cancelado |                  |
    E que o serviço relacionado seja o serviço da ordem de serviço
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E o status do serviço da ordem de serviço deve ser "CANCELLED"

  Cenário: Registrar ação DELIVER_VEHICLE
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "COMPLETED"
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action          | additionalInfo   | relatedServiceId |
      | DELIVER_VEHICLE | Veiculo entregue |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E o status da ordem de serviço deve ser "DELIVERED"

  Cenário: Progresso em ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action           | additionalInfo       | relatedServiceId |
      | START_INSPECTION | Recebida na recepcao |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === ORÇAMENTO ASSÍNCRONO ===

  @asyncBudget
  Cenário: Publicar evento de orçamento ao completar inspeção
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "IN_INSPECTION"
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action              | additionalInfo      | relatedServiceId |
      | COMPLETE_INSPECTION | Inspecao finalizada |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E um evento do tipo "BUDGET_REQUESTED" deve ser publicado no tópico "service-order-events" para a ordem de serviço
    E a ordem de serviço tenha o orçamento calculado
    E o campo decimal "estimatedAmount" da ordem de serviço não deve ser vazio
