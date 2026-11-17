package lab.runner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LabJobTest {
    @TempDir Path root;

    @Test
    void capturesStdoutAndStderr() throws Exception {
        Files.writeString(root.resolve("Example.java"), """
                public class Example {
                    public static void main(String[] args) {
                        System.out.println("hello lab");
                        System.err.println("diagnostic");
                    }
                }
                """);

        try (LabJob job = new LabJob(root, Path.of("Example.java"), ignored -> {})) {
            waitForExit(job);
            assertTrue(job.output().contains("hello lab"));
            assertTrue(job.output().contains("diagnostic"));
            assertTrue(job.exitCode() == 0);
        }
    }

    @Test
    void canStopAProgramThatDoesNotFinish() throws Exception {
        Files.writeString(root.resolve("Loop.java"), """
                public class Loop {
                    public static void main(String[] args) throws Exception {
                        while (true) Thread.sleep(1000);
                    }
                }
                """);

        try (LabJob job = new LabJob(root, Path.of("Loop.java"), ignored -> {})) {
            job.close();
            waitForExit(job);
            assertTrue(job.stopped());
        }
    }

    private static void waitForExit(LabJob job) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (!job.finished() && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(job.finished(), "child process did not finish in time");
    }
}
