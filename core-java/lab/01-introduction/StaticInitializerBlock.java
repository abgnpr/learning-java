/*
 * Challenge 10: Java Static Initializer Block
 * Difficulty: Easy
 *
 * Task: Complete the nested Defaults type's static initialization so its
 * width, height, and area fields hold 7, 5, and 35 respectively.
 * Complete: static void Defaults.initialize()
 * Run: java StaticInitializerBlock.java
 */
public final class StaticInitializerBlock {
    static final class Defaults {
        static int width;
        static int height;
        static int area;

        static {
            initialize();
        }

        private static void initialize() {
            width = 7;
            height = 5;
            area = width * height;
        }
    }

    private StaticInitializerBlock() {
    }

    public static void main(String[] args) {
        checkEquals(7, Defaults.width, "default width");
        checkEquals(5, Defaults.height, "default height");
        checkEquals(35, Defaults.area, "computed area");
        if (failures > 0) {
            throw new AssertionError("Challenge 10: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 10 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + show(actual));
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + "\n  expected: " + show(expected)
                + "\n    actual: " + show(actual));
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
}
