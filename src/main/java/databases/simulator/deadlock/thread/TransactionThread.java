package databases.simulator.deadlock.thread;

import databases.simulator.deadlock.LockManager;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simula uma transação que concorre pelos itens X e Y.
 */
public class TransactionThread extends Thread {

    private static final AtomicLong CLOCK = new AtomicLong();

    private final long timestamp = CLOCK.incrementAndGet();
    private final LockManager lockManager;
    private volatile boolean aborted = false;
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

    public TransactionThread(String name, LockManager manager) {
        super(name);
        this.lockManager = manager;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isAborted() {
        return aborted;
    }

    /** Marca a transação como abortada. */
    public synchronized void abort() {
        aborted = true;
        interrupt();
    }

    /** Espera aleatoriamente entre 0,5 s e 1,5 s. */
    private void randomDelay() {
        try {
            Thread.sleep(rnd.nextLong(500, 1500));
        } catch (InterruptedException ignored) {
            // ignorado: interrupção tratada no run()
        }
    }

    @Override
    public void run() {
        // permite reiniciar caso seja abortada
        while (true) {
            aborted = false;
            System.out.println(getName() + " entrou em execução.");

            String firstLock  = rnd.nextBoolean() ? "X" : "Y";
            String secondLock = firstLock.equals("X") ? "Y" : "X";

            try {
                randomDelay();

                // Tenta obter o primeiro lock
                if (!lockManager.lock(firstLock, this)) {
                    synchronized (this) {
                        wait();
                    }
                }

                randomDelay();

                // Tenta obter o segundo lock
                if (!lockManager.lock(secondLock, this)) {
                    synchronized (this) {
                        wait();
                    }
                }

                randomDelay();

                // Libera locks (pode ser na mesma ordem de aquisição)
                lockManager.unlock(firstLock, this);
                randomDelay();
                lockManager.unlock(secondLock, this);
                randomDelay();

                System.out.println(getName() + " fez commit e finalizou.");
                break; // sucesso ⇒ encerra loop
            } catch (InterruptedException e) {
                if (aborted) {
                    System.out.println(getName()
                            + " foi abortada por deadlock. Reiniciando...");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
}
