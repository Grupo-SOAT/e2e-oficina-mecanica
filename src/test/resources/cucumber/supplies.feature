# language: pt
@supplies
Funcionalidade: Gerenciamento de Insumos
  Como um "admin" logado
  Quero gerenciar os insumos e o estoque da oficina
  Para manter a disponibilidade de peças e materiais para os serviços

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de insumos
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar os insumos
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de insumos por SKU
    Dado que eu esteja devidamente logado
    E que o filtro sku seja "SKU-001"
    E que o tamanho da pagina seja 10
    Quando eu listar os insumos
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 insumos
    E a resposta deve conter ao menos um insumo com sku "SKU-001"

  Cenário: Paginação sem resultados deve retornar no content
    Dado que eu esteja devidamente logado
    E que o filtro sku seja "SKU-NAO-EXISTE"
    E que o tamanho da pagina seja 10
    Quando eu listar os insumos
    Então devo receber uma resposta com status "204"

  Cenário: Paginação de insumos com cursor
    Dado que eu esteja devidamente logado
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar os insumos
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de insumo por ID existente
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 1
    Quando eu consultar o insumo por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "reservedQuantity"
    E a resposta deve conter o campo "availableQuantity"

  Cenário: Detalhamento de insumo por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 99999
    Quando eu consultar o insumo por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SUPPLY_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de insumo com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo do novo insumo seja:
      | sku         | name        | description        | unitPrice | suppliedBy |
      | NEW-SKU-001 | Novo Insumo | Descricao de teste | 29.90     | 10         |
    Quando eu criar o insumo
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve refletir o payload enviado

  Cenário: Cadastro de insumo com sku duplicado
    Dado que eu esteja devidamente logado
    E que o corpo do novo insumo seja:
      | sku     | name            | description     | unitPrice | suppliedBy |
      | SKU-001 | Insumo repetido | Conflito de sku | 29.90     | 10         |
    Quando eu criar o insumo
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "SUPPLY_CONFLICT_DUPLICATED_SKU"

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de insumo existente
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 1
    E que o corpo de atualização do insumo seja:
      | id | sku             | name                      | description          | unitPrice | suppliedBy | reservedQuantity | availableQuantity |
      | 1  | SKU-001-UPDATED | Oleo sintetico atualizado | Descricao atualizada | 55.00     | 10         | 2                | 8                 |
    Quando eu atualizar o insumo
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado

  Cenário: Atualização de insumo com sku duplicado
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 1
    E que o corpo de atualização do insumo seja:
      | id | sku     | name            | description          | unitPrice | suppliedBy | reservedQuantity | availableQuantity |
      | 1  | SKU-002 | Insumo conflito | Atualizacao invalida | 55.00     | 10         | 2                | 8                 |
    Quando eu atualizar o insumo
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "SUPPLY_CONFLICT_DUPLICATED_SKU"

  Cenário: Atualização de insumo inexistente
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 99999
    E que o corpo de atualização do insumo seja:
      | id    | sku   | name           | description    | unitPrice | suppliedBy | reservedQuantity | availableQuantity |
      | 99999 | SKU-X | Nao encontrado | Nao encontrado | 10.00     | 10         | 0                | 0                 |
    Quando eu atualizar o insumo
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SUPPLY_NOT_FOUND"

  # === EXCLUSÃO ===

  Cenário: Exclusão de insumo existente
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 3
    Quando eu remover o insumo
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de insumo inexistente
    Dado que eu esteja devidamente logado
    E que o id do insumo seja 99999
    Quando eu remover o insumo
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SUPPLY_NOT_FOUND"
