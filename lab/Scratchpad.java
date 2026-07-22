
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Scratchpad {
    public static void main(String[] args) throws Exception {

        // 3. Callable + Future (returns a value and can throw checked exceptions)
        Callable<Integer> job = () -> {
            Thread.sleep(5000);
            return 42;
        };

        FutureTask<Integer> future = new FutureTask<>(job);
        var jobThread = new Thread(future);
        jobThread.start();

        System.out.println("tasks before getting the value...");

        var interruptorThread = new Thread(() -> {
            try {
                Thread.sleep(2000);
                var threads = Thread.getAllStackTraces();
                for (var thread : threads.keySet()) {
                    thread.interrupt();
                }
            } catch (InterruptedException ignored) {
            }
        });
        interruptorThread.start();

        try {
            int result = future.get(); // waits for completion
            System.out.println("value: " + result);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
            throw e;
        }

        System.out.println("tasks after getting the value...");
    }
}