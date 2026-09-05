/*
 * Challenge 08: Java Datatypes
 * Difficulty: Easy
 *
 * Task: Given a base-10 whole-number literal, name the smallest signed Java
 * primitive among byte, short, int, and long that can hold it, or return
 * "out-of-range".
 * Complete: static String smallestIntegralType(String literal)
 * Run: java Datatypes.java
 */
public final class Datatypes {
    private Datatypes() {
    }

    static String smallestIntegralType(String literal) {

        // parse-once way -- one parse, then comparisons
        /*
        try {
            long v = Long.parseLong(literal);
            if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
                return "byte";
            }
            if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) {
                return "short";
            }
            if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
                return "int";
            }
            return "long";
        } catch (NumberFormatException e) {
            return "out-of-range";
        } 
        */

        // A value beyond long still has to fail while parsing; only BigInteger
        // can represent it for comparison. Parsing each type is less efficient
        // than parsing once, but states the "smallest type that fits" rule plainly.

        // ask-each-type way
        try {
            Byte.parseByte(literal);
            return "byte";
        } catch (NumberFormatException e) {
        }
        try {
            Short.parseShort(literal);
            return "short";
        } catch (NumberFormatException e) {
        }
        try {
            Integer.parseInt(literal);
            return "int";
        } catch (NumberFormatException e) {
        }
        try {
            Long.parseLong(literal);
            return "long";
        } catch (NumberFormatException e) {
        }
        return "out-of-range";
    }

    public static void main(String[] args) {
        check("byte lower boundary", "-128", "byte", smallestIntegralType("-128"));
        check("byte upper boundary", "127", "byte", smallestIntegralType("127"));
        check("first value below byte", "-129", "short", smallestIntegralType("-129"));
        check("first value above byte", "128", "short", smallestIntegralType("128"));
        check("short lower boundary", "-32768", "short", smallestIntegralType("-32768"));
        check("negative int value", "-40000", "int", smallestIntegralType("-40000"));
        check("int lower boundary", "-2147483648", "int", smallestIntegralType("-2147483648"));
        check("first value above int", "2147483648", "long", smallestIntegralType("2147483648"));
        check("long lower boundary", "-9223372036854775808", "long", smallestIntegralType("-9223372036854775808"));
        check("below long", "-9223372036854775809", "out-of-range", smallestIntegralType("-9223372036854775809"));
        check("above long", "9223372036854775808", "out-of-range", smallestIntegralType("9223372036854775808"));
        report("Challenge 08");
    }



    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */

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
