package databases.simulator.deadlock;

import databases.simulator.deadlock.thread.TransactionThread;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Classe principal da simulação.
 */
public class DeadlockSimulation {

    public static void main(String[] args) {

        LockManager manager =
                new LockManager(Arrays.asList("X", "Y"));

        int totalThreads = 5;

        List<TransactionThread> txList = IntStream.rangeClosed(1, totalThreads)
                .mapToObj(i -> new TransactionThread("T" + i, manager))
                .toList();

        txList.forEach(Thread::start);

        txList.forEach(t -> {
            try { t.join(); } catch (InterruptedException ignored) {}
        });

        System.out.println("Todas as transações finalizaram.");
    }
}
