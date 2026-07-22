// Station 7 — producer/consumer over a BlockingQueue (capacity 5).
// Consumer is slower than producer, so the queue FILLS and put() blocks:
// backpressure, for free. No hand-rolled wait/notify.
// This IS the IMPS Kafka shape: producers append, consumer polls at its pace,
// the partition log is the shared buffer. (See core-java.md §5 / §8.)
// Run:  java ProducerConsumer.java
import java.util.concurrent.*;

public class ProducerConsumer {
    static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
    static final String POISON = "__END__";

    public static void main(String[] args) throws InterruptedException {
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 12; i++) {
                    queue.put("txn-" + i);                    // BLOCKS if full
                    System.out.println("produced txn-" + i + "  (queue size " + queue.size() + ")");
                    Thread.sleep(40);
                }
                queue.put(POISON);
            } catch (InterruptedException e) { }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String txn = queue.take();                // BLOCKS if empty
                    if (txn.equals(POISON)) break;
                    System.out.println("        consumed " + txn);
                    Thread.sleep(120);                        // slower -> queue backs up
                }
            } catch (InterruptedException e) { }
        }, "consumer");

        producer.start(); consumer.start();
        producer.join();  consumer.join();
        System.out.println("done — the queue was the shared buffer (the backpressure valve)");
    }
}
