Instruções de execução:
- Rode o main dentro de src/DeadLockSimulation
- A simulação executa 5 threads que tentam acessar ao banco simultaneamente
- Cada thread executa as seguintes operações: Lock em X, Lock em Y, Unlock em X e Unlock em Y, em sequencia.


Classes no projeto:
- DeadLockSimulation: O main do programa, instância as threads e roda-as em conjunto como o lockManager;
- databases.simulator.deadlock.thread.TransactionThread: A thread com a transação a ser rodada, possui a função run principal, além de funções para abortar a execução e dormir por um tempo aleatório.
- databases.simulator.deadlock.LockManager: Gerencia os items X e Y e seus respectivos locks, a função lock trava um item e entrega a trava para seu dono, e a função unlock destrava a thread caso quando o dono da lock a solicita.
- databases.simulator.deadlock.entity.DataItem: Um simples struct contendo os dados armazenados em uma lock: o id do item (X ou Y), um boolean que diz se está travada ou não, o dono da lock e uma fila contendo as próximas threads que irão ter acesso a lock.


O algoritmo implementa o wound-wait, ou seja, quando uma thread tenta acessar um item, e ele esteja bloqueado, caso a thread dona do lock desse item seja mais nova que a thread que tentou acessar, a thread mais nova é abortada e a thread mais antiga recebe o controle da lock.

A função run garante que uma thread ira rodar até concluir seu objetivo final, pois fica dentro de um while true, e com um try catch com um break no final do try, quando ocorre um abort, o bloco cai para o catch, e precisar recomeçar a transação do zero.