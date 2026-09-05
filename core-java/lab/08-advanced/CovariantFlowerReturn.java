/*
 * Challenge 61: Covariant Return Types (Easy)
 *
 * Task: Specialize a region's flower method so its override returns the more
 * precise Jasmine subtype while remaining usable through a Region reference.
 * Complete: WestBengal.nationalFlower().
 * Run: java CovariantFlowerReturn.java
 */
public class CovariantFlowerReturn {
    static class Flower {
        public String name() {
            return "Flower";
        }
    }

    static final class Jasmine extends Flower {
        @Override
        public String name() {
            return "Jasmine";
        }
    }

    static class Region {
        public Flower nationalFlower() {
            return new Flower();
        }
    }

    static final class WestBengal extends Region {
        @Override
        public Jasmine nationalFlower() {
            // TODO: Return the region's flower using the covariant subtype.
            throw new UnsupportedOperationException("TODO: implement nationalFlower");
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        check("override return type", "WestBengal.class.getDeclaredMethod(\"nationalFlower\").getReturnType()", Jasmine.class, WestBengal.class.getDeclaredMethod("nationalFlower").getReturnType());
        Flower direct = new WestBengal().nationalFlower();
        checkThat("direct call must return Jasmine", "direct instanceof Jasmine", direct instanceof Jasmine);
        Region polymorphic = new WestBengal();
        check("covariant result through base reference", "polymorphic.nationalFlower().name()", "Jasmine", polymorphic.nationalFlower().name());
        report("Challenge 61");
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
