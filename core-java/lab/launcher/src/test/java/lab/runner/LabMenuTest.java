package lab.runner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LabMenuTest {
    @TempDir Path root;

    @Test
    void searchFindsProgramAndEnterShowsItsOutput() throws Exception {
        Path section = Files.createDirectory(root.resolve("01-introduction"));
        Files.writeString(section.resolve("Hello.java"), """
                /* Challenge 01: Hello */
                public class Hello {
                    public static void main(String[] args) { System.out.println("hello lab"); }
                }
                """);
        Files.writeString(section.resolve("Other.java"), """
                /* Challenge 10: Other */
                public class Other { public static void main(String[] args) {} }
                """);
        DefaultVirtualTerminal terminal = new DefaultVirtualTerminal(new TerminalSize(100, 30));
        try (TerminalScreen screen = new TerminalScreen(terminal)) {
            screen.startScreen();
            LabMenu menu = new LabMenu(root, screen);
            menu.draw();
            assertTrue(visible(terminal).contains("01  Hello"));

            menu.handle(new KeyStroke('/', false, false));
            menu.handle(new KeyStroke('1', false, false));
            menu.handle(new KeyStroke(KeyType.Enter));
            menu.draw();
            assertTrue(visible(terminal).contains("1 match"));
            menu.handle(new KeyStroke(KeyType.Escape));

            menu.handle(new KeyStroke('/', false, false));
            for (char letter : "Hello".toCharArray()) menu.handle(new KeyStroke(letter, false, false));
            menu.handle(new KeyStroke(KeyType.Enter));
            menu.draw();
            assertTrue(visible(terminal).contains("1 match"));
            menu.handle(new KeyStroke(KeyType.Enter));

            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            String output = "";
            while (System.nanoTime() < deadline) {
                menu.draw();
                output = visible(terminal);
                if (output.contains("hello lab")) break;
                Thread.sleep(20);
            }
            assertTrue(output.contains("hello lab"));
        }
    }

    private static String visible(DefaultVirtualTerminal terminal) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < 30; row++) {
            for (int column = 0; column < 100; column++) {
                text.append(terminal.getCharacter(column, row).getCharacter());
            }
            text.append('\n');
        }
        return text.toString();
    }
}
