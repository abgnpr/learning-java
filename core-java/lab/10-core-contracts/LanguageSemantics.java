/*
 * Challenge 120 — Language Semantics Predictions (Medium)
 *
 * Task: Read each scenario, then fill predictions() with its exact returned
 * String, keyed by the scenario name. Predict BEFORE running. The scenario
 * code is fixed; implement only the prediction map. Explain each governing
 * rule aloud. Revisit with changed values, without opening the old predictions.
 * Run: java 10-core-contracts/LanguageSemantics.java
 * No assumptions are made about Integer identity outside the required cache.
 */
import java.util.*;
import java.util.function.Supplier;

public final class LanguageSemantics {
    static Map<String, String> predictions() {
        throw new UnsupportedOperationException("TODO: predict every scenario without running it");
    }

    @SuppressWarnings("static") // Calling through a reference is intentional in the hiding drill.
    static Map<String, Supplier<String>> scenarios() {
        Map<String, Supplier<String>> cases = new LinkedHashMap<>();
        cases.put("reference-copy", () -> {
            List<String> caller = new ArrayList<>(List.of("first"));
            mutateThenRebind(caller);
            return caller.toString();
        });
        cases.put("primitive-copy", () -> {
            int value = 7;
            increment(value);
            return Integer.toString(value);
        });
        cases.put("string-identity", () -> {
            String literal = "java";
            String separate = new String("java");
            return (literal == separate) + "," + literal.equals(separate)
                    + "," + (literal == separate.intern());
        });
        cases.put("concatenation", () -> {
            String prefix = new String("ja");
            return ("java" == "ja" + "va") + "," + ("java" == prefix + "va");
        });
        cases.put("string-immutability", () -> {
            String value = "a";
            value.concat("b");
            return value;
        });
        cases.put("boxing", () -> {
            Integer left = 127, right = 127;
            Integer larger = 1000;
            return (left == right) + "," + (larger == 1000) + "," + larger.equals(1000);
        });
        cases.put("null-unboxing", () -> {
            Integer value = null;
            try { int unboxed = value; return Integer.toString(unboxed); }
            catch (NullPointerException failure) { return failure.getClass().getSimpleName(); }
        });
        cases.put("numeric-promotion", () -> ('a' + 1) + "," + (1 + 2 + "3") + "," + ("1" + 2 + 3));
        cases.put("post-increment", () -> { int value = 3; value = value++; return "" + value; });
        cases.put("overflow", () -> Integer.toString(Integer.MAX_VALUE + 1));
        cases.put("floating-point", () -> (0.1 + 0.2 == 0.3) + "," + (5.0 / 0));
        cases.put("finally-return", () -> Integer.toString(finallyReturn()));
        cases.put("finally-exception", LanguageSemantics::finallyException);
        cases.put("initialization", () -> {
            new InitChild(); new InitChild();
            return INIT_EVENTS.toString();
        });
        cases.put("dispatch-and-hiding", () -> {
            Parent reference = new Child();
            Object argument = "text";
            return reference.field + "," + reference.kind() + "," + reference.call()
                    + "," + reference.overload(argument);
        });
        cases.put("lambda-this", () -> new Scope().compareThis());
        return cases;
    }

    private static void mutateThenRebind(List<String> copy) {
        copy.add("shared"); copy = new ArrayList<>(); copy.add("private");
    }
    private static void increment(int copy) { copy++; }
    @SuppressWarnings("finally")
    private static int finallyReturn() { try { return 1; } finally { return 2; } }
    @SuppressWarnings("finally")
    private static String finallyException() {
        try { throw new IllegalStateException("body"); } finally { return "cleanup"; }
    }

    private static final List<String> INIT_EVENTS = new ArrayList<>();
    static class InitParent {
        static { INIT_EVENTS.add("parent-static"); }
        { INIT_EVENTS.add("parent-instance"); }
        InitParent() { INIT_EVENTS.add("parent-constructor"); }
    }
    static final class InitChild extends InitParent {
        static { INIT_EVENTS.add("child-static"); }
        { INIT_EVENTS.add("child-instance"); }
        InitChild() { INIT_EVENTS.add("child-constructor"); }
    }
    static class Parent {
        String field = "parent-field";
        static String kind() { return "parent-static"; }
        String call() { return "parent-method"; }
        String overload(Object value) { return "object-overload"; }
        String overload(String value) { return "string-overload"; }
    }
    static final class Child extends Parent {
        String field = "child-field";
        static String kind() { return "child-static"; }
        @Override String call() { return "child-method"; }
    }
    static final class Scope {
        String compareThis() {
            Object owner = this;
            Supplier<Boolean> lambda = () -> this == owner;
            Supplier<Boolean> anonymous = new Supplier<>() {
                @Override public Boolean get() { return this == owner; }
            };
            return lambda.get() + "," + anonymous.get();
        }
    }

    public static void main(String[] args) {
        Map<String, String> guesses = predictions();
        Map<String, Supplier<String>> cases = scenarios();
        check("every scenario has exactly one prediction", cases.keySet(), guesses.keySet());
        cases.forEach((name, scenario) -> check(name, guesses.get(name), scenario.get()));
        report("Challenge 120");
    }

    // ---- test harness (not part of the exercise) ----
    private static int passes;
    private static int failures;

    private static void check(String label, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    private static String show(Object value) {
        if (value == null) return "null";
        if (value instanceof String text) return "<" + text.replace("\n", "\\n") + ">";
        if (value.getClass().isArray()) return java.util.Arrays.deepToString(new Object[]{value});
        return value + " (" + value.getClass().getSimpleName() + ")";
    }

    @FunctionalInterface
    interface CheckedAction { void run() throws Exception; }

    private static <E extends Throwable> E expect(Class<E> type, CheckedAction action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (!type.isInstance(actual)) throw new AssertionError("expected " + type.getName()
                    + ", got " + actual.getClass().getName(), actual);
            passes++;
            System.out.println("PASS  throws " + type.getSimpleName());
            return type.cast(actual);
        }
        throw new AssertionError("expected " + type.getName() + ", nothing was thrown");
    }

    private static void report(String name) {
        System.out.println(name + ": " + passes + " passed, " + failures + " failed.");
        if (failures != 0) throw new AssertionError(name + " failed");
        System.out.println(name + " passed.");
    }
}
