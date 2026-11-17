package lab.runner;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/** Search, run, and inspect the standalone Java exercises. */
public final class LabMenu {
    private static final TextColor BACKGROUND = new TextColor.RGB(15, 21, 29);
    private static final TextColor PANEL = new TextColor.RGB(24, 33, 43);
    private static final TextColor TEXT = new TextColor.RGB(226, 234, 239);
    private static final TextColor MUTED = new TextColor.RGB(145, 164, 176);
    private static final TextColor ACCENT = new TextColor.RGB(96, 205, 214);
    private static final TextColor ERROR = new TextColor.RGB(244, 136, 125);

    private final Path root;
    private final Screen screen;
    private final LabCatalog catalog;
    private List<LabCatalog.Lab> all = List.of();
    private List<LabCatalog.Lab> matches = List.of();
    private String query = "";
    private String message = "";
    private int selected;
    private int firstVisible;
    private boolean searching;
    private boolean showingOutput;
    private LabCatalog.Lab runningLab;
    private LabJob job;
    private int outputScroll;
    private final AtomicLong revision = new AtomicLong();

    LabMenu(Path root, Screen screen) throws IOException {
        this.root = root;
        this.screen = screen;
        this.catalog = new LabCatalog(root);
        refresh();
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length == 0 ? "." : args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(root.resolve("settings.gradle"))) {
            System.err.println("Run this menu with ./lab from the lab directory.");
            System.exit(1);
        }
        try (Screen screen = createScreen()) {
            screen.startScreen();
            screen.setCursorPosition(null);
            new LabMenu(root, screen).loop();
        } catch (IOException error) {
            System.err.println("Could not open the lab menu: " + error.getMessage());
            System.err.println("Run ./lab in a terminal at least 60 columns by 18 rows.");
            System.exit(1);
        }
    }

    private static Screen createScreen() throws IOException {
        if (Files.exists(Path.of("/dev/tty"))) {
            try {
                return new TerminalScreen(new SizedUnixTerminal());
            } catch (IOException ignored) {
                // The default factory can still serve terminals without a Unix TTY.
            }
        }
        return new DefaultTerminalFactory().setForceTextTerminal(true).createScreen();
    }

    void loop() throws IOException, InterruptedException {
        try {
            boolean open = true;
            long drawnRevision = -1;
            while (open) {
                TerminalSize resized = screen.doResizeIfNecessary();
                long currentRevision = revision.get();
                if (resized != null || currentRevision != drawnRevision) {
                    draw();
                    drawnRevision = currentRevision;
                }
                KeyStroke key;
                while ((key = screen.pollInput()) != null) {
                    if (!handle(key)) {
                        open = false;
                        break;
                    }
                }
                Thread.sleep(80);
            }
        } finally {
            if (job != null) job.close();
        }
    }

    boolean handle(KeyStroke key) {
        revision.incrementAndGet();
        if (key.getKeyType() == KeyType.EOF || (key.isCtrlDown() && key.getCharacter() != null
                && Character.toLowerCase(key.getCharacter()) == 'c')) {
            return false;
        }
        if (searching) {
            switch (key.getKeyType()) {
                case Enter -> searching = false;
                case Escape -> {
                    searching = false;
                    query = "";
                    filter();
                }
                case Backspace -> {
                    if (!query.isEmpty()) {
                        query = query.substring(0, query.offsetByCodePoints(query.length(), -1));
                        filter();
                    }
                }
                default -> {
                    if (key.getCharacter() != null && !Character.isISOControl(key.getCharacter())) {
                        query += key.getCharacter();
                        filter();
                    }
                }
            }
            return true;
        }

        char command = key.getCharacter() == null ? '\0' : Character.toLowerCase(key.getCharacter());
        if (command == 'q') return false;
        if (showingOutput) {
            int page = Math.max(1, screen.getTerminalSize().getRows() - 10);
            switch (key.getKeyType()) {
                case Escape -> showingOutput = false;
                case ArrowUp -> outputScroll++;
                case ArrowDown -> outputScroll = Math.max(0, outputScroll - 1);
                case PageUp -> outputScroll += page;
                case PageDown -> outputScroll = Math.max(0, outputScroll - page);
                case Home -> outputScroll = Integer.MAX_VALUE;
                case End -> outputScroll = 0;
                default -> {
                    if (command == 'r') run(runningLab);
                    if (command == 'x' && job != null && !job.finished()) job.close();
                }
            }
            return true;
        }

        int page = Math.max(1, screen.getTerminalSize().getRows() - 9);
        switch (key.getKeyType()) {
            case ArrowUp -> move(-1);
            case ArrowDown -> move(1);
            case PageUp -> move(-page);
            case PageDown -> move(page);
            case Home -> move(-matches.size());
            case End -> move(matches.size());
            case Enter -> {
                if (!matches.isEmpty()) run(matches.get(selected));
            }
            case Escape -> {
                query = "";
                filter();
            }
            case F5 -> refresh();
            default -> {
                switch (command) {
                    case '/' -> searching = true;
                    case 'j' -> move(1);
                    case 'k' -> move(-1);
                    case 'r' -> refresh();
                    default -> { }
                }
            }
        }
        return true;
    }

    private void refresh() {
        Path previous = matches.isEmpty() ? null : matches.get(selected).source();
        try {
            all = catalog.discover().values().stream().flatMap(List::stream).toList();
            filter();
            if (previous != null) {
                for (int index = 0; index < matches.size(); index++) {
                    if (matches.get(index).source().equals(previous)) selected = index;
                }
            }
            message = all.size() + " programs found";
        } catch (IOException error) {
            message = "Could not scan labs: " + error.getMessage();
        }
    }

    private void filter() {
        String needle = query.toLowerCase(Locale.ROOT);
        if (needle.matches("[0-9]{1,3}")) {
            String challenge = "^0*" + Integer.parseInt(needle) + "\\s+.*";
            matches = all.stream().filter(lab -> lab.title().matches(challenge)).toList();
        } else {
            matches = all.stream().filter(lab -> (lab.title() + " " + lab.source() + " " + lab.task())
                    .toLowerCase(Locale.ROOT).contains(needle)).toList();
        }
        selected = 0;
        firstVisible = 0;
    }

    private void move(int delta) {
        if (!matches.isEmpty()) {
            selected = Math.max(0, Math.min(matches.size() - 1, selected + delta));
        }
    }

    private void run(LabCatalog.Lab lab) {
        if (lab == null) return;
        if (job != null) job.close();
        runningLab = lab;
        showingOutput = true;
        outputScroll = 0;
        message = "";
        try {
            job = new LabJob(root, lab.source(), ignored -> revision.incrementAndGet());
        } catch (IOException error) {
            job = null;
            message = "Could not start Java: " + error.getMessage();
        }
    }

    void draw() throws IOException {
        TerminalSize size = screen.getTerminalSize();
        int width = size.getColumns();
        int height = size.getRows();
        TextGraphics g = screen.newTextGraphics();
        g.setBackgroundColor(BACKGROUND).setForegroundColor(TEXT).fill(' ');
        if (width < 60 || height < 18) {
            put(g, 1, 1, width - 2, "Resize terminal to at least 60 x 18.  q: quit", ERROR, true);
            screen.refresh();
            return;
        }
        if (showingOutput) drawOutput(g, width, height);
        else drawBrowser(g, width, height);
        screen.refresh();
    }

    private void drawBrowser(TextGraphics g, int width, int height) {
        put(g, 2, 0, width - 4, "JAVA LABS  /  " + all.size() + " programs", ACCENT, true);
        put(g, 2, 1, width - 4, "Search by challenge, name, or section.  Enter runs the selected lab.", MUTED, false);
        int leftWidth = Math.max(32, Math.min(46, width * 45 / 100));
        int rightX = leftWidth + 2;
        int rightWidth = width - rightX - 1;
        box(g, 1, 3, leftWidth, height - 6, " PROGRAMS ");
        box(g, rightX, 3, rightWidth, height - 6, " TASK ");
        put(g, 3, 5, leftWidth - 4, "Find: " + query + (searching ? "_" : ""), ACCENT, searching);
        put(g, 3, 6, leftWidth - 4, matches.size() + " match" + (matches.size() == 1 ? "" : "es"), MUTED, false);
        int rows = height - 12;
        if (selected < firstVisible) firstVisible = selected;
        if (selected >= firstVisible + rows) firstVisible = selected - rows + 1;
        for (int row = 0; row < rows && firstVisible + row < matches.size(); row++) {
            int index = firstVisible + row;
            boolean active = index == selected;
            g.setBackgroundColor(active ? ACCENT : PANEL).setForegroundColor(active ? BACKGROUND : TEXT);
            g.fillRectangle(new TerminalPosition(2, 8 + row), new TerminalSize(leftWidth - 2, 1), ' ');
            put(g, 3, 8 + row, leftWidth - 4, matches.get(index).title(),
                    active ? BACKGROUND : TEXT, active);
        }
        if (matches.isEmpty()) put(g, 3, 8, leftWidth - 4, "No matches. Edit / to search.", MUTED, false);
        g.setBackgroundColor(PANEL);
        if (!matches.isEmpty()) {
            LabCatalog.Lab lab = matches.get(selected);
            put(g, rightX + 2, 5, rightWidth - 4, lab.title(), ACCENT, true);
            put(g, rightX + 2, 7, rightWidth - 4, lab.source().toString(), MUTED, false);
            put(g, rightX + 2, 9, rightWidth - 4, "JDK " + lab.requiredJdk() + "+", MUTED, false);
            List<String> task = wrap(lab.task(), rightWidth - 4);
            for (int row = 0; row < task.size() && row < height - 15; row++) {
                put(g, rightX + 2, 11 + row, rightWidth - 4, task.get(row), TEXT, false);
            }
        }
        g.setBackgroundColor(BACKGROUND);
        put(g, 2, height - 2, width - 4, "[Up/Down] Select   [/] Search   [Enter] Run   [F5] Refresh   [q] Quit",
                TEXT, false);
        put(g, 2, height - 1, width - 4, message, MUTED, false);
    }

    private void drawOutput(TextGraphics g, int width, int height) {
        put(g, 2, 0, width - 4, runningLab.title(), ACCENT, true);
        put(g, 2, 1, width - 4, runningLab.source().toString(), MUTED, false);
        box(g, 1, 3, width - 2, height - 6, " OUTPUT ");
        String status = job == null ? message : !job.finished() ? "Running..." : job.stopped()
                ? "Stopped" : "Exit " + job.exitCode() + (job.exitCode() == 0 ? "  OK" : "  Failed");
        put(g, 3, 5, width - 6, status, job != null && job.finished() && job.exitCode() != 0 ? ERROR : ACCENT, true);
        List<String> lines = wrap(job == null ? "" : job.output(), width - 6);
        int rows = height - 12;
        int maxScroll = Math.max(0, lines.size() - rows);
        outputScroll = Math.min(outputScroll, maxScroll);
        int first = Math.max(0, lines.size() - rows - outputScroll);
        for (int row = 0; row < rows && first + row < lines.size(); row++) {
            put(g, 3, 7 + row, width - 6, lines.get(first + row), TEXT, false);
        }
        g.setBackgroundColor(BACKGROUND);
        put(g, 2, height - 2, width - 4,
                "[Esc] Programs   [r] Run again   [PgUp/PgDn] Scroll   [x] Stop   [q] Quit", TEXT, false);
        put(g, 2, height - 1, width - 4,
                outputScroll > 0 ? "Earlier output  /  End returns to latest" : "Latest output", MUTED, false);
    }

    private static void box(TextGraphics g, int x, int y, int width, int height, String title) {
        g.setBackgroundColor(PANEL).setForegroundColor(MUTED);
        g.fillRectangle(new TerminalPosition(x, y), new TerminalSize(width, height), ' ');
        g.putString(x, y, "┌" + "─".repeat(width - 2) + "┐");
        g.putString(x, y + height - 1, "└" + "─".repeat(width - 2) + "┘");
        for (int row = y + 1; row < y + height - 1; row++) {
            g.putString(x, row, "│");
            g.putString(x + width - 1, row, "│");
        }
        put(g, x + 2, y, width - 4, title, ACCENT, true);
    }

    private static void put(TextGraphics g, int x, int y, int width, String value, TextColor color, boolean bold) {
        if (width <= 0 || y < 0 || y >= g.getSize().getRows()) return;
        g.setForegroundColor(color);
        if (bold) g.enableModifiers(SGR.BOLD);
        else g.disableModifiers(SGR.BOLD);
        g.putString(x, y, value.length() > width ? value.substring(0, Math.max(0, width - 1)) + "…" : value);
        g.disableModifiers(SGR.BOLD);
    }

    private static List<String> wrap(String value, int width) {
        List<String> result = new ArrayList<>();
        for (String line : value.replace("\r", "").replace("\t", "    ").split("\n", -1)) {
            if (line.isEmpty()) result.add("");
            else for (int offset = 0; offset < line.length(); offset += width) {
                result.add(line.substring(offset, Math.min(line.length(), offset + width)));
            }
        }
        return result;
    }
}
