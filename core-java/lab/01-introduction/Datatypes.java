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

        //
        // Note what the comparisons cannot replace: the parse itself is still
        // the out-of-range test. Nothing above long can be held to compare in
        // the first place, so "9223372036854775808" has to arrive as a thrown
        // NumberFormatException either way. That is why widening the guards is
        // not an option -- BigInteger would be the next step, not a wider type.
        //
        // What it does buy is one throw/catch instead of up to four. Filling in
        // a stack trace dominates the cost of a parse, so exceptions-as-control-
        // flow is the fair criticism of the way below. It stays the way below
        // because four parseX calls say "smallest type that holds this" more
        // directly than eight boundary comparisons, and this is not a hot path.

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
        checkEquals("byte", smallestIntegralType("127"), "byte upper boundary");
        checkEquals("short", smallestIntegralType("128"), "first value above byte");
        checkEquals("int", smallestIntegralType("-40000"), "negative int value");
        checkEquals("long", smallestIntegralType("2147483648"), "first value above int");
        checkEquals("out-of-range", smallestIntegralType("9223372036854775808"), "above long");
        if (failures > 0) {
            throw new AssertionError("Challenge 08: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 08 passed.");
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
