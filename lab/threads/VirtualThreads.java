// Station 8 (bonus, Java 21) — a MILLION virtual threads, each blocking on I/O.
// Platform threads (1 OS thread each, MB stacks) can't do this — you'd OOM.
// Virtual threads are KB-scale; the JVM unmounts them from a carrier while blocked.
// Run:  java VirtualThreads.java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreads {
    public static void main(String[] args) {
        AtomicInteger done = new AtomicInteger();
        long t0 = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1_000_000; i++) {
                executor.submit(() -> {
                    try { Thread.sleep(100); } catch (InterruptedException e) { }  // simulated I/O
                    done.incrementAndGet();
                });
            }
        } // executor.close() waits for all one-million tasks to finish

        System.out.printf("1,000,000 virtual threads finished in %dms%n",
            System.currentTimeMillis() - t0);
        System.out.println("done = " + done.get());
        System.out.println("try newFixedThreadPool(1_000_000) for contrast — you can't afford the OS threads");
    }
}
