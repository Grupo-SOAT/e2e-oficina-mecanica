# language: pt
@vehicles
Funcionalidade: Gerenciamento de Veiculos
  Como um administrador da oficina
  Quero gerenciar veiculos
  Para manter o cadastro atualizado

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de veiculos
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar os veiculos
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de veiculos por placa
    Dado que eu esteja devidamente logado
    E que o filtro licensePlate seja "ABC-1234"
    E que o tamanho da pagina seja 10
    Quando eu listar os veiculos
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 veiculos
    E a resposta deve conter ao menos um veiculo com licensePlate "ABC-1234"

  Cenário: Paginação de veiculos com cursor
    Dado que eu esteja devidamente logado
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar os veiculos
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de veiculo por ID existente
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 1
    Quando eu consultar o veiculo por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "licensePlate"
    E a resposta deve conter o campo "brand"
    E a resposta deve conter o campo "model"
    E a resposta deve conter o campo "year"
    E a resposta deve conter o campo "color"

  Cenário: Detalhamento de veiculo por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 99999
    Quando eu consultar o veiculo por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "VEHICLE_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de veiculo com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo do novo veiculo seja:
      | licensePlate | brand  | model   | year | color | ownerId |
      | JKL-9087     | Toyota | Corolla | 2022 | PRATA | 1       |
    Quando eu criar o veiculo
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve refletir o payload enviado

  Esquema do Cenário: Cadastro de veiculo com placa inválida
    Dado que eu esteja devidamente logado
    E que o corpo do novo veiculo seja:
      | licensePlate | brand  | model   | year | color | ownerId |
      | <placa>      | Toyota | Corolla | 2022 | PRATA | 1       |
    Quando eu criar o veiculo
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "INVALID_LICENSE_PLATE"
    Exemplos:
      | placa    |
      | 000-AAAA |
      | 0-1      |
      | XPTO     |

  Esquema do Cenário: Cadastro de veiculo com ano inválido
    Dado que eu esteja devidamente logado
    E que o corpo do novo veiculo seja:
      | licensePlate | brand  | model   | year   | color | ownerId |
      | <placa>      | Toyota | Corolla | <year> | PRATA | 1       |
    Quando eu criar o veiculo
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "INVALID_VEHICLE_YEAR"
    Exemplos:
      | placa    | year  |
      | XYZ-1111 | -2012 |
      | XYZ-2222 | 12    |
      | XYZ-3333 | 3072  |

  Cenário: Cadastro de veiculo com placa duplicada
    Dado que eu esteja devidamente logado
    E que o corpo do novo veiculo seja:
      | licensePlate | brand | model | year | color | ownerId |
      | ABC-1234     | Ford  | Ka    | 2022 | AZUL  | 1       |
    Quando eu criar o veiculo
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE"

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de veiculo existente
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 1
    E que o corpo de atualização do veiculo seja:
      | id | licensePlate | brand | model | year | color | ownerId |
      | 1  | ABC-1234     | Ford  | Ka    | 2022 | AZUL  | 1       |
    Quando eu atualizar o veiculo
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado

  Cenário: Atualização de veiculo inexistente
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 99999
    E que o corpo de atualização do veiculo seja:
      | id    | licensePlate | brand  | model | year | color | ownerId |
      | 99999 | NAO-0000     | Toyota | Yaris | 2024 | PRETO | 1       |
    Quando eu atualizar o veiculo
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "VEHICLE_NOT_FOUND"

  Esquema do Cenário: Atualização de veiculo com placa inválida
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 1
    E que o corpo de atualização do veiculo seja:
      | id | licensePlate | brand  | model   | year | color | ownerId |
      | 1  | <placa>      | Toyota | Corolla | 2022 | PRATA | 1       |
    Quando eu atualizar o veiculo
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "INVALID_LICENSE_PLATE"
    Exemplos:
      | placa    |
      | 000-AAAA |
      | 0-1      |
      | XPTO     |

  Esquema do Cenário: Atualização de veiculo com ano inválido
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 1
    E que o corpo de atualização do veiculo seja:
      | id | licensePlate | brand  | model   | year   | color | ownerId |
      | 1  | <placa>      | Toyota | Corolla | <year> | PRATA | 1       |
    Quando eu atualizar o veiculo
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "INVALID_VEHICLE_YEAR"
    Exemplos:
      | placa    | year  |
      | XYZ-1111 | -2012 |
      | XYZ-2222 | 12    |
      | XYZ-3333 | 3072  |

  Cenário: Atualização de veiculo com placa duplicada
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 1
    E que o corpo de atualização do veiculo seja:
      | id | licensePlate | brand | model | year | color | ownerId |
      | 1  | DEF-5678     | Ford  | Ka    | 2022 | AZUL  | 1       |
    Quando eu atualizar o veiculo
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE"

  # === EXCLUSÃO ===

  Cenário: Exclusão de veiculo existente
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 2
    Quando eu remover o veiculo
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de veiculo inexistente
    Dado que eu esteja devidamente logado
    E que o id do veiculo seja 99999
    Quando eu remover o veiculo
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "VEHICLE_NOT_FOUND"
