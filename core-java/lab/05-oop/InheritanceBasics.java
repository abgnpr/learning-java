/*
 * Challenge 43: Java Inheritance I (Easy)
 *
 * Task: Model a bird that inherits the walking behavior of an animal while
 * adding its own flying and singing behaviors.
 * Complete: Change the Bird type so it has the required inheritance relation.
 * Run: java InheritanceBasics.java
 */
public class InheritanceBasics {
    static class Animal {
        public String walk() {
            return "I am walking";
        }
    }

    // TODO: Make Bird inherit from Animal.
    static class Bird {
        public String fly() {
            return "I am flying";
        }

        public String sing() {
            return "I am singing";
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        checkThat("Bird must be a subtype of Animal", "Animal.class.isAssignableFrom(Bird.class)", Animal.class.isAssignableFrom(Bird.class));
        Object bird = Bird.class.getDeclaredConstructor().newInstance();
        check("Bird must inherit walk()", "Bird.class.getMethod(\"walk\").invoke(bird)", "I am walking", Bird.class.getMethod("walk").invoke(bird));
        check("Bird must retain fly()", "Bird.class.getMethod(\"fly\").invoke(bird)", "I am flying", Bird.class.getMethod("fly").invoke(bird));
        check("Bird must retain sing()", "Bird.class.getMethod(\"sing\").invoke(bird)", "I am singing", Bird.class.getMethod("sing").invoke(bird));
        report("Challenge 43");
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
