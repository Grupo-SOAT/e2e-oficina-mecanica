# language: pt
@owners
Funcionalidade: Gerenciamento de Clientes
  Como um administrador da oficina
  Quero gerenciar clientes
  Para manter os cadastros atualizados

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de clientes
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar os clientes
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de clientes por documento
    Dado que eu esteja devidamente logado
    E que o filtro document seja "39544511075"
    E que o tamanho da pagina seja 10
    Quando eu listar os clientes
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 clientes
    E a resposta deve conter ao menos um cliente com document "39544511075"

  Cenário: Paginação de clientes com cursor
    Dado que eu esteja devidamente logado
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar os clientes
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de cliente por ID existente
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 1
    Quando eu consultar o cliente por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "name"
    E a resposta deve conter o campo "document"
    E a resposta deve conter o campo "documentType"
    E a resposta deve conter o campo "phone"
    E a resposta deve conter o campo "email"

  Cenário: Detalhamento de cliente por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 99999
    Quando eu consultar o cliente por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "OWNER_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de cliente com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo do novo cliente seja:
      | name          | document    | documentType | phone           | email                     |
      | João Ferreira | 11284956350 | CPF          | (11) 97777-1111 | joao.ferreira@cliente.com |
    Quando eu criar o cliente
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve refletir o payload enviado

#  Esquema do Cenário: Cadastro de cliente com documento válido em formatos alternativos
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo cliente seja:
#      | name   | document   | documentType   | phone   | email   |
#      | <name> | <document> | <documentType> | <phone> | <email> |
#    Quando eu criar o cliente
#    Então devo receber uma resposta com status "201"
#    Exemplos:
#      | name                     | document           | documentType | phone           | email                           |
#      | João Sem Mascara         | 32165498700        | CPF          | (11) 97777-1111 | joao.sem.mascara@cliente.com    |
#      | Empresa Cliente          | 45.987.654/0001-10 | CNPJ         | (11) 96666-5555 | empresa@cliente.com             |
#      | Empresa Sem Mascara LTDA | 45987654000110     | CNPJ         | (11) 95555-4444 | empresa.sem.mascara@cliente.com |

  Esquema do Cenário: Cadastro de cliente com CPF inválido
    Dado que eu esteja devidamente logado
    E que o corpo do novo cliente seja:
      | name                 | document   | documentType | phone           | email                    |
      | Cliente CPF Inválido | <document> | CPF          | (11) 98888-7777 | cpf.inválido@cliente.com |
    Quando eu criar o cliente
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "INVALID_DOCUMENT"
    Exemplos:
      | document       |
      | 123.456.789-00 |
      | 12345678900    |
      | abc            |

  Cenário: Cadastro de cliente com documento duplicado
    Dado que eu esteja devidamente logado
    E que o corpo do novo cliente seja:
      | name        | document    | documentType | phone           | email         |
      | Cliente Dup | 39544511075 | CPF          | (11) 98888-7777 | dup@email.com |
    Quando eu criar o cliente
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "OWNER_ALREADY_EXISTS"

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de cliente existente
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 1
    E que o corpo de atualização do cliente seja:
      | id | name            | document       | documentType | phone           | email          |
      | 1  | Joao Atualizado | 123.456.789-09 | CPF          | (11) 99999-9999 | novo@email.com |
    Quando eu atualizar o cliente
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado

  Esquema do Cenário: Atualização de cliente com documento duplicado
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 1
    E que o corpo de atualização do cliente seja:
      | id | name           | document    | documentType | phone           | email            |
      | 1  | Cliente Ajuste | <document>  | CPF          | (11) 99999-9999 | ajuste@email.com |
    Quando eu atualizar o cliente
    Então devo receber uma resposta com status "<status>"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | document    | status | reason               |
      | 39544511075 | 409    | OWNER_ALREADY_EXISTS |

  Cenário: Atualização de cliente inexistente
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 99999
    E que o corpo de atualização do cliente seja:
      | id    | name       | document    | documentType | phone           | email        |
      | 99999 | Nao Existe | 15146861048 | CPF          | (11) 95555-1111 | ne@email.com |
    Quando eu atualizar o cliente
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "OWNER_NOT_FOUND"

  # === EXCLUSÃO ===

  Cenário: Exclusão de cliente existente
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 2
    Quando eu remover o cliente
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de cliente inexistente
    Dado que eu esteja devidamente logado
    E que o id do cliente seja 99999
    Quando eu remover o cliente
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "OWNER_NOT_FOUND"
