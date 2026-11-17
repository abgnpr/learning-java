package lab.runner;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Reads the actual TTY size when cursor-position probing gives an unusable size. */
final class SizedUnixTerminal extends UnixTerminal {
    private TerminalSize lastSize;
    private long lastCheck;

    SizedUnixTerminal() throws IOException {
        super();
    }

    @Override
    protected TerminalSize findTerminalSize() throws IOException {
        long now = System.nanoTime();
        if (lastSize != null && now - lastCheck < 300_000_000L) return lastSize;
        try {
            Process probe = new ProcessBuilder("stty", "size")
                    .redirectInput(new File("/dev/tty"))
                    .start();
            String[] dimensions = new String(probe.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .strip().split("\\s+");
            if (probe.waitFor() == 0 && dimensions.length == 2) {
                int rows = Integer.parseInt(dimensions[0]);
                int columns = Integer.parseInt(dimensions[1]);
                if (columns >= 20 && rows >= 10) {
                    lastSize = new TerminalSize(columns, rows);
                    lastCheck = now;
                    return lastSize;
                }
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        } catch (IOException | NumberFormatException ignored) {
            // Use Lanterna's own terminal query when stty is unavailable.
        }
        return super.findTerminalSize();
    }
}
