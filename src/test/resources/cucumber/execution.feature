# language: pt
@expedicao
Funcionalidade: Fila de execução
  Como um mecânico da oficina
  Quero receber as ordens de serviço aprovadas em uma fila
  Para organizar a execução por filial

  Cenário: Enfileirar ordem de serviço aprovada
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "APPROVED"
    Então a fila do branch 1 no status "QUEUED" deve conter a ordem de serviço

  Cenário: Iniciar execução de uma ordem de serviço
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "APPROVED"
    E que a ordem de serviço esteja na fila de execução
    Quando eu iniciar a execução da ordem de serviço
    Então o job da ordem de serviço deve estar com status "IN_PROGRESS"
    E o status da ordem de serviço deve ser "IN_PROGRESS"
    E um evento do tipo "EXECUTION_STARTED" deve ser publicado no tópico "execution-events" para a ordem de serviço atual

  Cenário: Concluir execução de uma ordem de serviço
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "APPROVED"
    E que a ordem de serviço esteja na fila de execução
    Quando eu iniciar a execução da ordem de serviço
    E eu concluir a execução da ordem de serviço
    Então o job da ordem de serviço deve estar com status "COMPLETED"
    E o status da ordem de serviço deve ser "COMPLETED"
    E um evento do tipo "EXECUTION_COMPLETED" deve ser publicado no tópico "execution-events" para a ordem de serviço atual

  Cenário: Cancelar job quando a ordem de serviço é cancelada
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço com um serviço
    E que a ordem de serviço esteja no status "APPROVED"
    E que a ordem de serviço esteja na fila de execução
    Quando eu cancelar a ordem de serviço
    Então o job da ordem de serviço deve estar com status "CANCELLED"
    E a fila do branch 1 no status "QUEUED" não deve conter a ordem de serviço

  Cenário: Início de execução inexistente
    Dado que eu esteja devidamente logado
    Quando eu iniciar uma execução inexistente
    Então devo receber uma resposta com status "404"
