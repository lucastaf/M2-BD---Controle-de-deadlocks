import java.util.Random;

class TransactionThread extends Thread {
    static long globalTimestamp = 0;
    long timestamp;
    LockManager lockManager;
    boolean isAborted = false;
    Random rand = new Random();

    public TransactionThread(String name, LockManager lm) {
        super(name);
        this.lockManager = lm;
        this.timestamp = ++globalTimestamp;
    }

    public synchronized void abort() {
        isAborted = true;
        this.interrupt();
    }

    void randomSleep() {
        try {
            Thread.sleep(rand.nextInt(1000) + 500);
        } catch (InterruptedException e) {
            // Ignorado
        }
    }

    @Override
    public void run() {
        while (true) {
            isAborted = false;
            System.out.println(getName() + " entrou em execução.");

            try {
                randomSleep();

                //Solicita a lock de X, caso seja negada, espera o notify
                if (!lockManager.lock("X", this)) {
                    synchronized (this) {
                        this.wait();
                    }
                }

                randomSleep();

                //Solicita a lock de Y, caso seja negada, espera o notify
                if (!lockManager.lock("Y", this)) {
                    synchronized (this) {
                        this.wait();
                    }
                }

                randomSleep();

                //Libera X
                lockManager.unlock("X", this);
                randomSleep();

                //Libera Y
                lockManager.unlock("Y", this);
                randomSleep();

                System.out.println(getName() + " fez commit e finalizou.");
                break;

            } catch (InterruptedException e) {
                if (isAborted) {
                    System.out.println(getName() + " foi abortada devido a deadlock. Reiniciando...");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
}