import java.util.LinkedList;
import java.util.Queue;

class DataItem {
    String itemId;
    boolean isLocked = false;
    TransactionThread owner = null;
    Queue<TransactionThread> queue = new LinkedList<>();

    public DataItem(String itemId) {
        this.itemId = itemId;
    }
}