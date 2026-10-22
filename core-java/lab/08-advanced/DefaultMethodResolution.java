/*
 * Challenge 112: Default-Method Resolution (Medium)
 *
 * Task: Resolve two unrelated interface defaults explicitly. Preserve both
 * behaviors in the result, and use the interface-qualified super syntax.
 *
 * Complete: ResolvedLabel.label().
 * Java baseline: Java 8.
 * Run: javac DefaultMethodResolution.java && java DefaultMethodResolution
 */
public final class DefaultMethodResolution {
    interface Named {
        default String label() {
            return "named";
        }

        static String normalize(String value) {
            return value.trim().toLowerCase(java.util.Locale.ROOT);
        }
    }

    interface Audited {
        default String label() {
            return "audited";
        }
    }

    static class ParentLabel {
        public String label() {
            return "class";
        }
    }

    static final class ClassWins extends ParentLabel implements Named {
        // The inherited class method wins over Named.label().
    }

    static final class ResolvedLabel implements Named, Audited {
        @Override
        public String label() {
            // TODO: Combine Named's label and Audited's label as "named+audited".
            throw new UnsupportedOperationException("TODO: resolve the default conflict");
        }
    }

    private DefaultMethodResolution() {
    }

    public static void main(String[] args) {
        check("class beats interface default", "new ClassWins().label()",
                "class", new ClassWins().label());

        check("unrelated defaults are resolved explicitly",
                "new ResolvedLabel().label()",
                "named+audited", new ResolvedLabel().label());

        check("static interface method is called through the interface",
                "Named.normalize with padded JAVA",
                "java", Named.normalize("  JAVA  "));

        report("Challenge 112");
    }

    private static int passes;
    private static int failures;

    private static void check(
            String label,
            Object input,
            Object expected,
            Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);

        if (ok) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, "
                + failures + " failed.");

        if (failures > 0) {
            throw new AssertionError(
                    challenge + ": " + failures + " check(s) failed.");
        }

        System.out.println(challenge + " passed.");
    }
}
