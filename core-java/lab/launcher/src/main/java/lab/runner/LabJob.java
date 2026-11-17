package lab.runner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Runs one source file and captures its output without blocking the menu. */
final class LabJob implements AutoCloseable {
    private static final int OUTPUT_LIMIT = 100_000;
    private final Process process;
    private final Consumer<LabJob> changed;
    private final Thread reader;
    private final StringBuilder output = new StringBuilder();
    private boolean truncated;
    private volatile boolean finished;
    private volatile boolean stopped;
    private volatile int exitCode = -1;

    LabJob(Path root, Path source, Consumer<LabJob> changed) throws IOException {
        this.changed = changed;
        process = new ProcessBuilder(System.getenv().getOrDefault("LAB_JAVA", "java"), source.toString())
                .directory(root.toFile())
                .redirectErrorStream(true)
                .start();
        try {
            process.getOutputStream().close(); // This version runs the labs' embedded checks.
        } catch (IOException ignored) {
            // A very short program may have already closed its input.
        }
        reader = Thread.ofVirtual().start(this::readOutput);
        Thread.ofVirtual().start(this::awaitExit);
    }

    private void readOutput() {
        try (var stream = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[2048];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                synchronized (output) {
                    output.append(buffer, 0, count);
                    if (output.length() > OUTPUT_LIMIT) {
                        output.delete(0, output.length() - OUTPUT_LIMIT);
                        truncated = true;
                    }
                }
                changed.accept(this);
            }
        } catch (IOException error) {
            if (!stopped) append("\n[Could not read output: " + error.getMessage() + "]\n");
        }
    }

    private void awaitExit() {
        try {
            exitCode = process.waitFor();
            reader.join();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        } finally {
            finished = true;
            changed.accept(this);
        }
    }

    private void append(String text) {
        synchronized (output) {
            output.append(text);
        }
        changed.accept(this);
    }

    String output() {
        synchronized (output) {
            return (truncated ? "[Earlier output omitted]\n" : "") + output;
        }
    }

    boolean finished() { return finished; }
    boolean stopped() { return stopped; }
    int exitCode() { return exitCode; }

    @Override
    public void close() {
        if (finished) return;
        stopped = true;
        process.destroy();
        Thread.ofVirtual().start(() -> {
            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) process.destroyForcibly();
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        });
    }
}
