# language: pt
@catalogServices
Funcionalidade: Gerenciamento de Serviços Catalogados
  Como um "admin" logado
  Quero gerenciar os serviços cadastrados na oficina
  Para manter o catálogo atualizado com as manutenções disponíveis

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenario: Listagem de serviços catalogados
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços catalogados
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de serviços catalogados por nome
    Dado que eu esteja devidamente logado
    E que o filtro name seja "Troca de Óleo"
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços catalogados
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 serviços catalogados
    E a resposta deve conter ao menos um serviço catalogado com nome "Troca de Óleo"

  Cenário: Paginação sem resultados deve retornar no content
    Dado que eu esteja devidamente logado
    E que o filtro id seja 99999
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços catalogados
    Então devo receber uma resposta com status "204"

  Cenário: Paginação de serviços catalogados com cursor
    Dado que eu esteja devidamente logado
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar os serviços catalogados
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de serviço catalogado por ID existente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 1
    Quando eu consultar o serviço catalogado por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "name"
    E a resposta deve conter o campo "description"
    E a resposta deve conter o campo "basePrice"
    E a resposta deve conter o campo "neededSupplies"

  Cenário: Detalhamento de serviço catalogado por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 99999
    Quando eu consultar o serviço catalogado por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "CATALOG_SERVICE_NOT_FOUND"

  # === CADASTRO ===

  Esquema do Cenário: Cadastro de serviço catalogado com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo do novo serviço catalogado seja:
      | nome   | descricao   | basePrice   | neededSupplies   |
      | <nome> | <descricao> | <basePrice> | <neededSupplies> |
    Quando eu criar o serviço catalogado
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    Exemplos:
      | nome                   | descricao                    | basePrice | neededSupplies |
      | Alinhamento de Rodas   | Alinhamento completo 4 rodas | 180.00    |                |
      | Troca de Óleo do Motor | Troca completa do óleo 5W-30 | 150.00    | SKU-001,,1     |

  Esquema do Cenário: Cadastro de serviço catalogado com dados inválidos
    Dado que eu esteja devidamente logado
    E que o corpo do novo serviço catalogado seja:
      | nome   | descricao   | basePrice   | neededSupplies   |
      | <nome> | <descricao> | <basePrice> | <neededSupplies> |
    Quando eu criar o serviço catalogado
    Então devo receber uma resposta com status "400"
    E com a mensagem de erro igual a "<reason>"
    Exemplos:
      | nome                 | descricao                    | basePrice | neededSupplies | reason                        |
      |                      | Alinhamento completo 4 rodas | 180.00    |                | INVALID_CATALOG_SERVICE_NAME  |
      | Alinhamento de Rodas | Alinhamento completo 4 rodas | -180.00   |                | INVALID_CATALOG_SERVICE_PRICE |

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de serviço catalogado existente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 1
    E que o corpo de atualização do serviço catalogado seja:
      | id | nome                 | descricao                    | basePrice | neededSupplies |
      | 1  | Alinhamento de Rodas | Alinhamento completo 4 rodas | 180.00    |                |
    Quando eu atualizar o serviço catalogado
    Então devo receber uma resposta com status "200"

  Esquema do Cenário: Atualização de serviço catalogado com dados inválidos
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 1
    E que o corpo de atualização do serviço catalogado seja:
      | id | nome   | descricao   | basePrice   | neededSupplies   |
      | 1  | <nome> | <descricao> | <basePrice> | <neededSupplies> |
    Quando eu atualizar o serviço catalogado
    Então devo receber uma resposta com status "400"
    E com a mensagem de erro igual a "<reason>"
    Exemplos:
      | nome                 | descricao                    | basePrice | neededSupplies | reason                        |
      |                      | Alinhamento completo 4 rodas | 180.00    |                | INVALID_CATALOG_SERVICE_NAME  |
      | Alinhamento de Rodas | Alinhamento completo 4 rodas | -180.00   |                | INVALID_CATALOG_SERVICE_PRICE |

  Cenário: Atualização de serviço catalogado com sku inexistente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 1
    E que o corpo de atualização do serviço catalogado seja:
      | id | nome                   | descricao                    | basePrice | neededSupplies |
      | 1  | Troca de Óleo do Motor | Troca completa do óleo 5W-30 | 150.00    | SKU-9999,,1    |
    Quando eu atualizar o serviço catalogado
    Então devo receber uma resposta com status "404"
    E com a mensagem de erro igual a "CATALOG_SERVICE_NOT_FOUND"

  Cenário: Atualização de serviço catalogado com quantidade de insumos inválida
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 1
    E que o corpo de atualização do serviço catalogado seja:
      | id | nome                   | descricao                    | basePrice | neededSupplies |
      | 1  | Troca de Óleo do Motor | Troca completa do óleo 5W-30 | 150.00    | SKU-002,,-1    |
    Quando eu atualizar o serviço catalogado
    Então devo receber uma resposta com status "400"
    E com a mensagem de erro igual a "INVALID_SUPPLY_QUANTITY"

  Cenário: Atualização de serviço catalogado inexistente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 99999
    E que o corpo de atualização do serviço catalogado seja:
      | id    | nome           | descricao      | basePrice | neededSupplies |
      | 99999 | Nao encontrado | Nao encontrado | 10.00     |                |
    Quando eu atualizar o serviço catalogado
    Então devo receber uma resposta com status "404"
    E com a mensagem de erro igual a "CATALOG_SERVICE_NOT_FOUND"

  # === EXCLUSÃO ===

  Cenário: Exclusão de serviço catalogado existente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 2
    Quando eu remover o serviço catalogado
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de serviço catalogado inexistente
    Dado que eu esteja devidamente logado
    E que o id do serviço catalogado seja 99999
    Quando eu remover o serviço catalogado
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "CATALOG_SERVICE_NOT_FOUND"
