# language: pt
@reporting
Funcionalidade: Exportação de Relatórios
  Como um usuário da oficina
  Quero consultar relatórios do sistema
  Para acompanhar indicadores e documentos operacionais

  # === RELATÓRIO DE TEMPO MÉDIO DE SERVIÇO ===

  Cenário: Consulta de tempo médio de execução por serviço
    Dado que eu esteja devidamente logado
    E que exista um serviço executado no catálogo
    Quando acesso o endpoint "GET" "/reports/catalog/services/:id/average-time"
    Então devo receber uma resposta com status "200"
    E o content-type da resposta deve ser "application/pdf"
    E o body da resposta não deve ser vazio
    E o nome do arquivo deve indicar exportação recente

  Cenário: Consulta de tempo médio de execução de todos os serviços em CSV
    Dado que eu esteja devidamente logado
    E que exista um serviço executado no catálogo
    Quando acesso o endpoint "GET" "/reports/catalog/services/average-time"
    Então devo receber uma resposta com status "200"
    E o content-type da resposta deve ser "text/csv"
    E o body da resposta não deve ser vazio
    E o nome do arquivo deve indicar exportação recente

  # === CASOS DE ERRO: SEM RESULTADO (204) ===

  Cenário: Consulta de relatório sem nenhum serviço cadastrado
    Dado que eu esteja devidamente logado
    E que nenhum serviço esteja cadastrado
    Quando acesso o endpoint "GET" "/reports/catalog/services/average-time"
    Então devo receber uma resposta com status "204"

  # === CASOS DE ERRO: NÃO ENCONTRADO (404) ===

  Cenário: Consulta de tempo médio de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id do serviço seja 99999
    Quando acesso o endpoint "GET" "/reports/catalog/services/:id/average-time"
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_NOT_FOUND"
