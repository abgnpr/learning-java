// Station 3 — visibility. Without `volatile`, this data race gives the worker
// no guarantee of observing the write; an optimizer may reuse the old value.
// Run:  java Visibility.java
//   -> if it hangs, that's the bug. Ctrl-C, add `volatile` to `stop`, rerun.
//   (JIT-dependent: it may or may not hang — volatile establishes visibility.)
public class Visibility {
    static boolean stop = false;          // <-- add `volatile` here to fix

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            long spins = 0;
            while (!stop) { spins++; }     // wants to see stop==true
            System.out.println("worker saw stop=true after " + spins + " spins");
        });
        worker.start();
        Thread.sleep(1000);
        stop = true;                       // main flips the flag
        System.out.println("main set stop=true — waiting for worker...");
        worker.join();                     // hangs here if worker never sees the flip
        System.out.println("done");
    }
}
