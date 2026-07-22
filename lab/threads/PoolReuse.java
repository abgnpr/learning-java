// Station 6 — a fixed pool is 3 workers + a shared in-tray (the queue).
// 6 tasks, 3 workers: watch only 3 distinct worker names get REUSED.
// Run:  java PoolReuse.java
import java.util.concurrent.*;

public class PoolReuse {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 6; i++) {
            int id = i;
            pool.submit(() -> {
                System.out.printf("task %d ran on %s%n", id, Thread.currentThread().getName());
                sleep(200);                       // hold the worker so queuing is visible
            });
        }
        pool.shutdown();                          // no new tasks; drain the queue
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("only 3 worker names appeared — t4/t5/t6 waited in the queue");
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { } }
}
