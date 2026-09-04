
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
        checkEquals(true, areAnagrams("Dormitory", "Dirty room"), "spaces and case ignored");
        checkEquals(true, areAnagrams("Java", "avaJ"), "same multiplicities");
        checkEquals(true, areAnagrams("abc!", "cab!"), "matching punctuation");
        checkEquals(false, areAnagrams("abc!", "cab"), "punctuation is significant");
        checkEquals(false, areAnagrams("abc!", "caba"), "different punctuation");
        checkEquals(true, areAnagrams("a1b2", "2B1A"), "digits are significant");
        checkEquals(true, areAnagrams("a\tb\nc", "Cba"), "all whitespace is ignored");
        checkEquals(false, areAnagrams("aab", "abb"), "different multiplicities");
        checkEquals(false, areAnagrams("abc", "abd"), "different characters");
        checkEquals(true, areAnagrams("", ""), "empty strings");
        checkEquals(false, areAnagrams("", "a"), "different lengths");
        checkEquals(false, areAnagrams(null, "abc"), "null first string");
        checkEquals(false, areAnagrams("abc", null), "null second string");
        if (failures > 0) {
            throw new AssertionError("Challenge 18: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 18 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected == actual) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + ":\n  expected: <" + expected + ">"
                + "\n    actual: <" + actual + ">");
    }
}
