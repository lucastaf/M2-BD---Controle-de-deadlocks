package databases.simulator.deadlock;

import databases.simulator.deadlock.entity.DataItem;
import databases.simulator.deadlock.thread.TransactionThread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concede e libera locks, aplicando a política wound-wait.
 */
public class LockManager {

    private final Map<String, DataItem> items = new ConcurrentHashMap<>();

    public LockManager(List<String> ids) {
        ids.forEach(id -> items.put(id, new DataItem(id)));
    }

    /** Tenta obter o lock; retorna true se conseguiu imediatamente. */
    public synchronized boolean lock(String itemId, TransactionThread tx) {
        DataItem item = items.get(itemId);

        // Recurso livre
        if (!item.isLocked()) {
            grantLock(item, tx);
            return true;
        }

        TransactionThread owner = item.getOwner();

        // Wound-wait: thread mais velha “fere” a mais nova
        if (tx.getTimestamp() < owner.getTimestamp()) {
            System.out.println(tx.getName() + " força " + owner.getName()
                    + " a abortar (wound-wait em " + itemId + ")");
            owner.abort();
            grantLock(item, tx);
            return true;
        }

        // Caso contrário, espera
        System.out.println(tx.getName() + " está esperando lock em " + itemId);
        item.getQueue().add(tx);
        return false;
    }

    /** Libera lock e passa para a próxima thread da fila, se existir. */
    public synchronized void unlock(String itemId, TransactionThread tx) {
        DataItem item = items.get(itemId);

        if (tx.equals(item.getOwner())) {
            item.setLocked(false);
            item.setOwner(null);
            System.out.println(tx.getName() + " liberou lock em " + itemId);

            TransactionThread next = item.getQueue().poll();
            if (next != null && !next.isAborted()) {
                grantLock(item, next);
                synchronized (next) {
                    next.notify();
                }
            }
        }
    }

    // === utilitário interno ===
    private void grantLock(DataItem item, TransactionThread tx) {
        item.setLocked(true);
        item.setOwner(tx);
        System.out.println(tx.getName() + " obteve lock em " + item.getItemId());
    }
}
