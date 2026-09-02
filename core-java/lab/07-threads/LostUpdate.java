// Station 2 — the race condition. `i++` is read-modify-write, NOT atomic.
// 8 threads each add 100k. Plain int can lose updates; AtomicInteger doesn't.
// Run:  java LostUpdate.java   (rerun a few times — any loss usually varies)
import java.util.concurrent.atomic.AtomicInteger;

public class LostUpdate {
    static int unsafe = 0;                          // plain int — racy
    static final AtomicInteger safe = new AtomicInteger();  // CAS, lock-free
    static final int THREADS = 8, PER = 100_000;

    public static void main(String[] args) throws InterruptedException {
        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < PER; j++) {
                    unsafe++;                       // three steps: read, +1, write
                    safe.incrementAndGet();         // one atomic step
                }
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();

        int expected = THREADS * PER;
        System.out.println("expected : " + expected);
        System.out.println("unsafe   : " + unsafe + "   (lost " + (expected - unsafe) + ")");
        System.out.println("safe     : " + safe.get());
    }
}
