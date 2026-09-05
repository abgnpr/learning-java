
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * Challenge 18: Java Anagrams
 * Difficulty: Easy
 *
 * Task: Decide whether two strings contain the same non-whitespace characters
 * with the same multiplicities, ignoring character case. Punctuation counts.
 * Complete: static boolean areAnagrams(String first, String second)
 * Run: java Anagrams.java
 */
public final class Anagrams {
    private Anagrams() {
    }

    static boolean areAnagrams(String first, String second) {
        if (first == null || second == null) {
            return false;
        }

        // Normalize before comparing lengths: otherwise the ignored space in "Dirty room"
        // would reject an anagram. Locale.ROOT prevents the machine's default locale changing keys.
        first = first.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        second = second.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        if (first.length() != second.length()) {
            return false;
        }

        // merge supplies the missing-key case, so no containsKey branch is needed. Map.equals()
        // then checks both the characters and their counts; 1L keeps both maps' values as Long.
        Map<Character, Long> firstCounter = new HashMap<>();
        for (int i = 0; i < first.length(); i++) {
            firstCounter.merge(first.charAt(i), 1L, Long::sum);
        }
        Map<Character, Long> secondCounter = new HashMap<>();
        for (int i = 0; i < second.length(); i++) {
            secondCounter.merge(second.charAt(i), 1L, Long::sum);
        }
        // counting() returns Long, which is why this stream-built map compares with the loop-built one.
        // alternative counter using streams
        /*
        secondCounter = second.chars().mapToObj(c -> (char) c).collect(
                Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return firstCounter.equals(secondCounter);
        */
        return firstCounter.equals(secondCounter);

        // Sorting keeps duplicate characters, unlike a Set, so equal sorted arrays prove equal multiplicities.
        // approach 2: sort and compare
        /* 
        var firstCharArray = first.toCharArray();
        var secondCharArray = second.toCharArray();
        Arrays.sort(secondCharArray);
        Arrays.sort(firstCharArray);
        return Arrays.equals(firstCharArray, secondCharArray); 
        */

        // A fixed table is fastest only when the character range is truly bounded; 256 excludes an em dash.
        // apprach 3: if characters are constrained to a-z and A-Z
        /*
        int[] counts = new int[256];
        for (int i = 0; i < first.length(); i++) {
            counts[first.charAt(i)] += 1;
            counts[second.charAt(i)] -= 1;
        }
        return Arrays.stream(counts).allMatch(count -> count == 0);
        */

        // Code points avoid splitting supplementary characters such as emoji, at the cost of a large table.
        // approach 4: count Unicode code points, including characters such as emoji
        /*
        int[] counts = new int[Character.MAX_CODE_POINT + 1];
        first.codePoints().forEach(codePoint -> counts[codePoint]++);
        second.codePoints().forEach(codePoint -> counts[codePoint]--);
        return Arrays.stream(counts).allMatch(count -> count == 0);
        */
    }

    public static void main(String[] args) {
        check("spaces and case ignored", "\"Dormitory\", \"Dirty room\"", true, areAnagrams("Dormitory", "Dirty room"));
        check("same multiplicities", "\"Java\", \"avaJ\"", true, areAnagrams("Java", "avaJ"));
        check("matching punctuation", "\"abc!\", \"cab!\"", true, areAnagrams("abc!", "cab!"));
        check("punctuation is significant", "\"abc!\", \"cab\"", false, areAnagrams("abc!", "cab"));
        check("different punctuation", "\"abc!\", \"caba\"", false, areAnagrams("abc!", "caba"));
        check("digits are significant", "\"a1b2\", \"2B1A\"", true, areAnagrams("a1b2", "2B1A"));
        check("all whitespace is ignored", "\"a\\tb\\nc\", \"Cba\"", true, areAnagrams("a\tb\nc", "Cba"));
        check("different multiplicities", "\"aab\", \"abb\"", false, areAnagrams("aab", "abb"));
        check("different characters", "\"abc\", \"abd\"", false, areAnagrams("abc", "abd"));
        check("empty strings", "\"\", \"\"", true, areAnagrams("", ""));
        check("different lengths", "\"\", \"a\"", false, areAnagrams("", "a"));
        check("null first string", "null, \"abc\"", false, areAnagrams(null, "abc"));
        check("null second string", "\"abc\", null", false, areAnagrams("abc", null));
        report("Challenge 18");
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
