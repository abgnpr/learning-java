
/*
 * Challenge 68: Flatten Nested Data
 * Difficulty: Easy
 *
 * Task: Split every nonblank line on one or more whitespace characters,
 * flatten all words into one stream, lowercase with Locale.ROOT, remove
 * duplicates, sort, and return a list.
 * Complete: static List<String> uniqueWords(List<String> lines)
 * Required focus: flatMap.
 * Run: java FlatMapWords.java
 */
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class FlatMapWords {
    private FlatMapWords() {
    }

    // Compiled once as a constant. String.split(regex) recompiles the pattern on
    // every call, which inside a flatMap means once per line.
    private static final Pattern SPLITTER = Pattern.compile("\\s+");

    /**
     * flatMap, not map: the mapper returns a Stream per line and flatMap
     * concatenates them into one stream of words. map would give a
     * Stream<Stream<String>> that distinct and sorted cannot see into.
     */
    static List<String> uniqueWords(List<String> lines) {
        return lines.stream()
                // splitAsStream over split: no String[] is materialized per line.
                // SPLITTER::splitAsStream is a bound reference to the constant.
                .flatMap(SPLITTER::splitAsStream)

                // Not defensive. Splitting drops trailing empty fields but never
                // leading ones, so a line with leading whitespace -- and an empty
                // line -- each yield an "" token that would sort to the front:
                //     " a b " -> ["", "a", "b"]      ""   -> [""]
                //     "  "    -> []                  "a b" -> ["a", "b"]
                // Without this filter, List.of("") returns [""], not [].
                .filter(word -> !word.isBlank())

                // ROOT, not the default locale: under tr-TR "TITLE".toLowerCase()
                // is "tItle" with a dotless i, so TITLE and title stop deduping.
                // Folding must depend on the data, not on the host.
                .map(word -> word.toLowerCase(Locale.ROOT))

                // distinct before sorted: both are stateful and buffer, so
                // deduplicating first leaves sorted less to order. Reversing them
                // is correct but does strictly more work.
                .distinct()

                // sorted AFTER the lowercasing, never before. Natural String order
                // is UTF-16 code-unit order, where every uppercase letter precedes
                // every lowercase one -- ["A", "B", "a", "b"] -- so sorting the raw
                // words would order "Zebra" before "apple" and the fold could not
                // repair it.
                .sorted()

                // toList() returns an unmodifiable list (Java 16+), and unlike
                // Collectors.toUnmodifiableList it tolerates nulls.
                .toList();
    }

    public static void main(String[] args) {
        check("flatten, normalize, and deduplicate", "List.of(\" Java stream \", \"STREAM   API\", \"\", \"java\")",
                List.of("api", "java", "stream"), uniqueWords(List.of(" Java stream ", "STREAM   API", "", "java")));
        check("single word", "List.of(\"one\")", List.of("one"), uniqueWords(List.of("one")));
        check("blank lines", "List.of(\"  \", \"\\t\")", List.of(), uniqueWords(List.of("  ", "\t")));

        // ---- empty and degenerate inputs ----
        check("no lines at all", "List.of()", List.of(), uniqueWords(List.of()));
        check("one empty line", "List.of(\"\")", List.of(), uniqueWords(List.of("")));
        check("empty line beside a real one", "List.of(\"\", \"word\")",
                List.of("word"), uniqueWords(List.of("", "word")));

        // ---- separator placement: leading empties survive split, trailing ones do not ----
        check("leading separator yields no empty word", "List.of(\" pad\")",
                List.of("pad"), uniqueWords(List.of(" pad")));
        check("trailing separator yields no empty word", "List.of(\"pad \")",
                List.of("pad"), uniqueWords(List.of("pad ")));
        check("surrounded by whitespace", "List.of(\"   pad   \")",
                List.of("pad"), uniqueWords(List.of("   pad   ")));

        // ---- \s+ collapses every whitespace kind, not just the space ----
        check("runs of mixed whitespace are one separator", "List.of(\"a \\t\\n b\")",
                List.of("a", "b"), uniqueWords(List.of("a \t\n b")));
        check("tab, newline, form feed, carriage return", "List.of(\"a\\tb\\ncd\\fe\\rf\")",
                List.of("a", "b", "cd", "e", "f"), uniqueWords(List.of("a\tb\ncd\fe\rf")));

        // ---- deduplication ----
        check("duplicates within a single line", "List.of(\"go go GO Go\")",
                List.of("go"), uniqueWords(List.of("go go GO Go")));
        check("duplicates across lines", "List.of(\"same\", \"SAME\", \"Same\")",
                List.of("same"), uniqueWords(List.of("same", "SAME", "Same")));

        // ---- sorting happens after lowercasing, not before ----
        // Natural String order is UTF-16 code-unit order, where every uppercase
        // letter precedes every lowercase one ([A, B, a, b]). The uppercase word
        // must sort AFTER the lowercase one once folded, or transposing map and
        // sorted still passes: "Zebra apple" catches it, "Beta ALPHA" does not.
        check("sort follows case folding", "List.of(\"Zebra apple\")",
                List.of("apple", "zebra"), uniqueWords(List.of("Zebra apple")));
        check("case folding reorders across lines", "List.of(\"Echo\", \"delta\", \"foxtrot\")",
                List.of("delta", "echo", "foxtrot"), uniqueWords(List.of("Echo", "delta", "foxtrot")));
        check("digits and symbols sort before letters", "List.of(\"zeta 10 2 _under\")",
                List.of("10", "2", "_under", "zeta"), uniqueWords(List.of("zeta 10 2 _under")));

        // ---- only whitespace splits; punctuation stays attached ----
        check("punctuation is part of the word", "List.of(\"end. mid, word\")",
                List.of("end.", "mid,", "word"), uniqueWords(List.of("end. mid, word")));

        // ---- Locale.ROOT keeps folding independent of the default locale ----
        check("non-ascii folds and dedupes", "List.of(\"Caf\\u00e9 CAF\\u00c9\")",
                List.of("caf\u00e9"), uniqueWords(List.of("Caf\u00e9 CAF\u00c9")));

        report("Challenge 68");
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

    /** Records one case. Prints input, expected and actual so a failure is diagnosable. */
    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    /** Records a case whose contract is a condition rather than a value. */
    private static void checkThat(String label, Object input, boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + "condition holds");
        System.out.println("      actual:   " + (condition ? "holds" : "does not hold"));
    }

    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */
    private static String show(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Object[] array) {
            return java.util.Arrays.deepToString(array);
        }
        if (value.getClass().isArray()) {
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
        String[] lines = s.split("\n", -1);
        var sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\\n");
            }
            sb.append('<').append(lines[i].replace("\r", "\\r")).append('>');
        }
        return sb.toString();
    }

    /** Prints the tally and fails the run if any case failed. */
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
