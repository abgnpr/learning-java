// Station 1 — start() spawns a thread; run() is just a method call.
// Run:  java StartVsRun.java
public class StartVsRun {
    static class Worker extends Thread {
        public void run() {
            System.out.println("run() executed on thread: " + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Worker().run();        // direct call — runs on THIS thread
        Worker w = new Worker();
        w.start();                 // spawns a NEW thread, which calls run()
        w.join();
        System.out.println("main is running on thread: " + Thread.currentThread().getName());
    }
}
