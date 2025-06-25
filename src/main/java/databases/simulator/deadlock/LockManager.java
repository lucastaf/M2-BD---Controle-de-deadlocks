package databases.simulator.deadlock;

import databases.simulator.deadlock.entity.DataItem;
import databases.simulator.deadlock.thread.TransactionThread;

import java.util.List;
import java.util.Map;
import java.util.Queue;
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
            System.out.println(tx.getName() + "->>" + itemId + ":[ABORT FORÇADO] " + tx.getName() + " força "
                    + owner.getName() + " a abortar (wound-wait em " + owner.getName()  + ")");
            owner.abort();                  // aborta a thread mais nova
            grantLock(item, tx);
            return true;
        }

        // Caso contrário, espera
        System.out.println(tx.getName() + "-->" + tx.getName() + ":[WAIT] está esperando lock em "+ itemId);
        item.getQueue().add(tx);
        return false;
    }

    /** Libera lock e passa para a próxima thread da fila, se existir. */
    public synchronized void unlock(String itemId, TransactionThread tx) {
        internalUnlock(items.get(itemId), tx, false);
    }

    /**
     * Libera todos os locks mantidos por uma transação abortada
     * e remove-a de quaisquer filas de espera.
     */
    public synchronized void releaseAllLocks(TransactionThread tx) {
        items.values().forEach(item -> {
            // Se a transação é a dona, libera
            internalUnlock(item, tx, true);

            // Se estava apenas esperando neste item, retira da fila
            item.getQueue().remove(tx);
        });
    }

    // === utilitários internos ===
    private synchronized void grantLock(DataItem item, TransactionThread tx) {
        item.setLocked(true);
        item.setOwner(tx);
        System.out.println(tx.getName() + "->>+" + item.getItemId() + ":[GRANT-LOCK] " + tx.getName() + " Obteve lock em " + item.getItemId());
    }

    /**
     * Libera o lock de 'item' caso 'tx' seja o dono.
     * Se 'forced'==true, imprime anotação de abort.
     */
    private synchronized void internalUnlock(DataItem item, TransactionThread tx, boolean forced) {
        if (item == null || !tx.equals(item.getOwner())) return;

        item.setLocked(false);
        item.setOwner(null);

        String msg = item.getItemId() + "-->>-" +  tx.getName() + ":[UNLOCK] " + tx.getName() + " liberou lock de " + item.getItemId();
        if (forced) msg += " (abort)";
        System.out.println(msg);

        Queue<TransactionThread> queue = item.getQueue();
        TransactionThread next = queue.poll();
        if (next != null && !next.isAborted()) {
            grantLock(item, next);
            synchronized (next) { next.notify(); }
        }
    }
}
