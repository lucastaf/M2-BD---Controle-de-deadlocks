package databases.simulator.deadlock.entity;

import databases.simulator.deadlock.thread.TransactionThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class DataItem {

    /** Identificador lógico do recurso (ex.: "X" ou "Y"). */
    private final String itemId;

    /** Indica se o recurso está travado. */
    private boolean locked = false;

    /** Thread que possui o lock (null se livre). */
    private TransactionThread owner;

    /**
     * Fila de espera pela posse do lock. Usamos ConcurrentLinkedQueue
     * para evitar problemas de concorrência.
     */
    private final Queue<TransactionThread> queue = new ConcurrentLinkedQueue<>();
}
