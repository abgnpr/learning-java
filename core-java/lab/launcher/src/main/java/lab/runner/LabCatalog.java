package lab.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds standalone source files without compiling or changing any of the exercises. */
final class LabCatalog {
    private static final Pattern SECTION = Pattern.compile("\\d{2}-.+");
    private static final Pattern TITLE = Pattern.compile("Challenge (\\d+): (.+)");
    private final Path root;

    LabCatalog(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    Map<String, List<Lab>> discover() throws IOException {
        Map<String, List<Lab>> sections = new LinkedHashMap<>();
        try (var entries = Files.list(root)) {
            for (Path directory : entries.filter(Files::isDirectory)
                    .filter(path -> SECTION.matcher(path.getFileName().toString()).matches())
                    .sorted().toList()) {
                List<Lab> labs = new ArrayList<>();
                try (var sources = Files.list(directory)) {
                    for (Path source : sources.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".java"))
                            .sorted().toList()) {
                        labs.add(describe(source));
                    }
                }
                labs.sort(Comparator.comparingInt(lab -> challengeNumber(lab.title())));
                if (!labs.isEmpty()) {
                    sections.put(directory.getFileName().toString(), List.copyOf(labs));
                }
            }
        }
        Path scratchpad = root.resolve("Scratchpad.java");
        if (Files.isRegularFile(scratchpad)) {
            sections.put("Scratchpad", List.of(describe(scratchpad)));
        }
        return sections;
    }

    private Lab describe(Path source) throws IOException {
        String name = source.getFileName().toString();
        String title = name.substring(0, name.length() - ".java".length());
        int requiredJdk = 21;
        StringBuilder task = new StringBuilder();
        boolean readingTask = false;
        // The metadata lives in the short opening comment of each challenge.
        try (var lines = Files.lines(source)) {
            for (String line : lines.limit(30).toList()) {
                Matcher match = TITLE.matcher(line);
                if (match.find()) {
                    title = match.group(1) + "  " + match.group(2).trim();
                }
                String comment = line.strip().replaceFirst("^\\* ?", "");
                if (comment.startsWith("Task:")) {
                    readingTask = true;
                    task.append(comment.substring(5).strip());
                } else if (readingTask && (comment.startsWith("Complete:") || comment.startsWith("Run:")
                        || comment.startsWith("Run on") || comment.startsWith("*/"))) {
                    readingTask = false;
                } else if (readingTask && !comment.isBlank()) {
                    task.append(' ').append(comment);
                }
                if (line.contains("Run on JDK 25+") || line.contains("Java 25")) {
                    requiredJdk = 25;
                } else if (line.contains("Run on JDK 17+")) {
                    requiredJdk = 17;
                }
            }
        }
        return new Lab(root.relativize(source), title, requiredJdk,
                task.isEmpty() ? "Open the source file to read its task." : task.toString());
    }

    private static int challengeNumber(String title) {
        Matcher match = Pattern.compile("^(\\d+)").matcher(title);
        return match.find() ? Integer.parseInt(match.group(1)) : Integer.MAX_VALUE;
    }

    record Lab(Path source, String title, int requiredJdk, String task) {
        @Override
        public String toString() {
            return title;
        }
    }
}
