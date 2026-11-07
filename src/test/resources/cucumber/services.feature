# language: pt
@services
Funcionalidade: Gerenciamento de Serviços da Ordem de Serviço
  Como um administrador da oficina
  Quero gerenciar os serviços que compõem uma ordem de serviço
  Para gerar métricas de produtividade e acompanhar o andamento

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de serviços da ordem de serviço
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com dois serviços
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços da ordem de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de serviços da ordem de serviço por id
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com dois serviços
    E que o filtro serviceId seja o serviço da ordem de serviço
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços da ordem de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 serviços da ordem de serviço

  Cenário: Paginação sem resultados de serviços da ordem de serviço deve retornar no content
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o filtro serviceId seja 99999
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços da ordem de serviço
    Então devo receber uma resposta com status "204"

  Cenário: Paginação de serviços da ordem de serviço com cursor
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com dois serviços
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços da ordem de serviço
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de serviço da ordem de serviço por ID existente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    Quando eu consultar o serviço da ordem de serviço por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "serviceOrderId"
    E a resposta deve conter o campo "catalogServiceId"
    E a resposta deve conter o campo "price"
    E a resposta deve conter o campo "status"
    E a resposta deve conter o campo "statusLabel"

  Cenário: Detalhamento de serviço da ordem de serviço por ID inexistente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o id do serviço da ordem de serviço seja 99999
    Quando eu consultar o serviço da ordem de serviço por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_NOT_FOUND"

  Cenário: Detalhamento de serviço da ordem de serviço com OS inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o id do serviço da ordem de serviço seja 1
    Quando eu consultar o serviço da ordem de serviço por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de serviço na ordem de serviço com dados válidos
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo do novo serviço da ordem de serviço seja:
      | catalogServiceId | price  |
      | 2                | 150.00 |
    Quando eu incluir o serviço na ordem de serviço
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve refletir o payload enviado
    E o campo "statusLabel" deve ser "Aguardando Aprovação"

  Cenário: Cadastro de serviço em ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o corpo do novo serviço da ordem de serviço seja:
      | catalogServiceId | price |
      | 1                | 10.00 |
    Quando eu incluir o serviço na ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  Cenário: Cadastro de serviço com catálogo inexistente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo do novo serviço da ordem de serviço seja:
      | catalogServiceId | price |
      | 99999            | 10.00 |
    Quando eu incluir o serviço na ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "CATALOG_SERVICE_NOT_FOUND"

  Cenário: Cadastro de serviço com preço inválido
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo do novo serviço da ordem de serviço seja:
      | catalogServiceId | price |
      | 1                | -1.00 |
    Quando eu incluir o serviço na ordem de serviço
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "INVALID_PRICE"

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de serviço da ordem de serviço existente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo de atualização do serviço da ordem de serviço seja:
      | catalogServiceId | price  | status            |
      | 1                | 160.00 | AWAITING_APPROVAL |
    Quando eu atualizar o serviço da ordem de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado
    E o campo "statusLabel" deve ser "Aguardando Aprovação"

  Cenário: Atualização de serviço da ordem de serviço com OS inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o id do serviço da ordem de serviço seja 1
    E que o corpo de atualização do serviço da ordem de serviço seja:
      | catalogServiceId | price  | status   |
      | 1                | 160.00 | APPROVED |
    Quando eu atualizar o serviço da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  Cenário: Atualização de serviço com id inexistente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o id do serviço da ordem de serviço seja 99999
    E que o corpo de atualização do serviço da ordem de serviço seja:
      | catalogServiceId | price  | status   |
      | 1                | 160.00 | APPROVED |
    Quando eu atualizar o serviço da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_NOT_FOUND"

  Esquema do Cenário: Atualização de serviço na ordem de serviço com payload inválido
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o corpo de atualização do serviço da ordem de serviço seja:
      | catalogServiceId   | price   | status            |
      | <catalogServiceId> | <price> | AWAITING_APPROVAL |
    Quando eu atualizar o serviço da ordem de serviço
    Então devo receber uma resposta com status "<code>"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | catalogServiceId | price | reason                    | code |
      | 99999            | 10.00 | CATALOG_SERVICE_NOT_FOUND | 404  |
      | 1                | -1.00 | INVALID_PRICE             | 400  |

  # === EXCLUSÃO ===

  Cenário: Exclusão de serviço da ordem de serviço existente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    Quando eu remover o serviço da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de serviço da ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o id do serviço da ordem de serviço seja 1
    Quando eu remover o serviço da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  Cenário: Exclusão de serviço com id inexistente
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que o id do serviço da ordem de serviço seja 99999
    Quando eu remover o serviço da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_NOT_FOUND"
