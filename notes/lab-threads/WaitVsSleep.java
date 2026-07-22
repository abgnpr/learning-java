// Station 4 — the famous one. sleep() CLUTCHES the lock; wait() LETS GO.
// Same setup twice: a holder grabs the lock, a waiter tries to enter.
//   sleep scenario -> waiter gets in only after ~2000ms (lock held).
//   wait  scenario -> waiter gets in at ~100ms       (lock released).
// Run:  java WaitVsSleep.java
public class WaitVsSleep {
    static final Object lock = new Object();
    static long t0;

    static void log(String m) {
        System.out.printf("[%4dms] %-6s %s%n",
            System.currentTimeMillis() - t0, Thread.currentThread().getName(), m);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== sleep(2000): holder KEEPS the lock ===");
        scenario(true);
        Thread.sleep(500);
        System.out.println("\n=== wait(2000): holder RELEASES the lock ===");
        scenario(false);
    }

    static void scenario(boolean useSleep) throws InterruptedException {
        t0 = System.currentTimeMillis();
        Thread holder = new Thread(() -> {
            synchronized (lock) {
                try {
                    if (useSleep) { log("got lock, sleep(2000) — clutching it"); Thread.sleep(2000); }
                    else          { log("got lock, wait(2000) — letting go");     lock.wait(2000); }
                } catch (InterruptedException e) { }
                log("leaving synchronized block");
            }
        }, "holder");

        Thread waiter = new Thread(() -> {
            synchronized (lock) { log("*** I GOT IN ***"); }
        }, "waiter");

        holder.start();
        Thread.sleep(100);   // let holder grab the lock first
        waiter.start();
        holder.join();
        waiter.join();
    }
}
