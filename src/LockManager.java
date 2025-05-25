import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LockManager {
    Map<String, DataItem> dataItems = new HashMap<>();

    public LockManager(List<String> items) {
        for (String item : items) {
            dataItems.put(item, new DataItem(item));
        }
    }

    public synchronized boolean lock(String itemId, TransactionThread transaction) {
        DataItem item = dataItems.get(itemId);

        if (!item.isLocked) {
            item.isLocked = true;
            item.owner = transaction;
            System.out.println(transaction.getName() + " obteve lock em " + itemId);
            return true;
        } else {
            TransactionThread owner = item.owner;
            if (transaction.timestamp < owner.timestamp) {
                // Wound-wait: transação mais nova espera, mais velha força o abort da mais nova
                System.out.println(transaction.getName() + " força " + owner.getName() + " a abortar (wound-wait em " + itemId + ")");
                owner.abort();
                item.isLocked = true;
                item.owner = transaction;
                System.out.println(transaction.getName() + " obteve lock em " + itemId);
                return true;
            } else {
                // Espera
                System.out.println(transaction.getName() + " está esperando lock em " + itemId);
                item.queue.add(transaction);
                return false;
            }
        }
    }

    public synchronized void unlock(String itemId, TransactionThread transaction) {
        DataItem item = dataItems.get(itemId);

        if (item.owner == transaction) {
            item.isLocked = false;
            item.owner = null;
            System.out.println(transaction.getName() + " liberou lock em " + itemId);

            if (!item.queue.isEmpty()) {
                TransactionThread next = item.queue.poll();
                if (!next.isAborted) {
                    item.isLocked = true;
                    item.owner = next;
                    System.out.println(next.getName() + " obteve lock em " + itemId + " da fila");
                    synchronized (next) {
                        next.notify();
                    }
                }
            }
        }
    }
}