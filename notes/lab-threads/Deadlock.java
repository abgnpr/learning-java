// Station 5 — deadlock by inconsistent lock ordering. This HANGS.
// t1 grabs A then wants B; t2 grabs B then wants A. Neither yields.
// Run:  java Deadlock.java     (it will hang — find it, then fix it)
//   Find it live:  jps        # get the pid
//                  jstack <pid> | grep -A3 -i deadlock
//   Fix: make BOTH threads lock in the SAME order (A then B). See the note below.
public class Deadlock {
    static final Object A = new Object();
    static final Object B = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            synchronized (A) {
                sleep(100);
                System.out.println("t1 holds A, wants B");
                synchronized (B) { System.out.println("t1 got BOTH"); }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (B) {                 // FIX: change B -> A ...
                sleep(100);
                System.out.println("t2 holds B, wants A");
                synchronized (A) { System.out.println("t2 got BOTH"); }  // ... and A -> B
            }
        }, "t2");

        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println("both finished — no deadlock");
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { } }
}
