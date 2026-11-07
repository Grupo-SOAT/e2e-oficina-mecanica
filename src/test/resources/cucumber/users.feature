# language: pt
@users
Funcionalidade: Gerenciamento de Usuarios
  Como um administrador da oficina
  Quero gerenciar usuarios
  Para controlar acessos ao sistema

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de usuarios
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar os usuarios
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de usuarios por username
    Dado que eu esteja devidamente logado
    E que o filtro username seja "admin"
    E que o tamanho da pagina seja 10
    Quando eu listar os usuarios
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 usuarios
    E a resposta deve conter ao menos um usuario com username "admin"

  Cenário: Paginação de usuarios com cursor
    Dado que eu esteja devidamente logado
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar os usuarios
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de usuario por ID existente
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 1
    Quando eu consultar o usuario por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "username"
    E a resposta deve conter o campo "roles"

  Cenário: Detalhamento de usuario por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 99999
    Quando eu consultar o usuario por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "USER_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de usuario com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo do novo usuario seja:
      | username          | roles         |
      | consultor.oficina | ["ATTENDANT"] |
    Quando eu criar o usuario
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve refletir o payload enviado

  Esquema do Cenário: Cadastro de usuario com username inválido ou duplicado
    Dado que eu esteja devidamente logado
    E que o corpo do novo usuario seja:
      | username   | roles        |
      | <username> | ["MECHANIC"] |
    Quando eu criar o usuario
    Então devo receber uma resposta com status "<status>"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | username | status | reason                            |
      | mecanico | 409    | USER_CONFLICT_DUPLICATED_USERNAME |

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de usuario existente
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 2
    E que o corpo de atualização do usuario seja:
      | id | username  | roles        |
      | 2  | mecanico2 | ["MECHANIC"] |
    Quando eu atualizar o usuario
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado

  Esquema do Cenário: Atualização de usuario com username inválido ou duplicado
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 1
    E que o corpo de atualização do usuario seja:
      | id | username   | roles         |
      | 1  | <username> | ["ATTENDANT"] |
    Quando eu atualizar o usuario
    Então devo receber uma resposta com status "<status>"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | username | status | reason                            |
      | mecanico | 409    | USER_CONFLICT_DUPLICATED_USERNAME |

  Cenário: Atualização de usuario inexistente
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 99999
    E que o corpo de atualização do usuario seja:
      | id    | username  | roles        |
      | 99999 | naoexiste | ["MECHANIC"] |
    Quando eu atualizar o usuario
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "USER_NOT_FOUND"

  # === EXCLUSÃO ===

  Cenário: Exclusão de usuario existente
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 2
    Quando eu remover o usuario
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de usuario inexistente
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 99999
    Quando eu remover o usuario
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "USER_NOT_FOUND"

  # === RESET DE SENHA ===

  Cenário: Resetar senha de usuario existente e fazer login com senha padrão
    Dado que eu esteja devidamente logado
    E que o id do usuario seja 2
    Quando eu resetar a senha do usuario
    Então devo receber uma resposta com status "200"
    E quando eu fizer logout
    E eu fazer login com username "mecanico" e senha padrão
    Então devo receber uma resposta com status "200"
    E a resposta deve conter um token JWT válido
