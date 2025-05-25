import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeadlockSimulation {
    public static void main(String[] args) {
        List<String> items = Arrays.asList("X", "Y");
        LockManager lockManager = new LockManager(items);

        int N = 5; // Número de threads/transações
        List<TransactionThread> transactions = new ArrayList<>();

        for (int i = 1; i <= N; i++) {
            TransactionThread t = new TransactionThread("T" + i, lockManager);
            transactions.add(t);
        }

        for (TransactionThread t : transactions) {
            t.start();
        }

        for (TransactionThread t : transactions) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Todas as transações finalizaram.");
    }
}