
import java.util.concurrent.locks.ReentrantLock;

public class Scratchpad {

    static ReentrantLock lock1 = new ReentrantLock();
    static ReentrantLock lock2 = new ReentrantLock();

    static void work(ReentrantLock first, ReentrantLock second, String name) {
        while (true) {
            first.lock();
            System.out.println(name + " got lock: " + first.toString().substring(41, 49));

            second.lock();
            System.out.println(name + " got lock: " + second.toString().substring(41, 49));
            System.out.println(name + " got both locks");
            second.unlock();
            first.unlock();
            System.out.println(name + " released both locks");

            // return;
            // if (second.tryLock()) {
            // System.out.println(name + " got lock: " + second.toString().substring(41,
            // 49));
            // System.out.println(name + " got both locks");
            // second.unlock();
            // first.unlock();
            // System.out.println(name + " released both locks");
            // return;
            // } else {
            // System.out.println(name + " could not get lock: " +
            // second.toString().substring(41, 49));

            // }

            first.unlock();
            System.out.println(name + " released lock: " + first.toString().substring(41, 49));

            System.out.println(name + " retrying...");

            try {
                Thread.sleep(100); // "Be polite"
            } catch (InterruptedException ignored) {
            }
        }

    }

    public static void main(String[] args) {
        new Thread(() -> work(lock1, lock2, "T1")).start();
        new Thread(() -> work(lock2, lock1, "T2")).start();

        // 
        Object o = new Object();
        
    }
}