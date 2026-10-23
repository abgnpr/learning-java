/*
 * Challenge 123 — Functional Composition Contracts (Medium)
 *
 * Task: Build reusable behavior, rather than evaluating it while constructing
 * it. Use Function.andThen/compose and Predicate.and/or/negate. Return method references
 * for the four reference factories. Do not replace supplied callbacks.
 * Complete: the ten marked methods. Baseline: Java 21 (Java 8 APIs).
 * Run: java 08-advanced/FunctionalCompositionContracts.java
 * Explain: target typing; the receiver of String::compareTo; effectively-final
 * capture versus mutable captured objects; lambda this versus anonymous this.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public final class FunctionalCompositionContracts {
    static Function<String, Integer> pipeline(
            Function<String, String> clean, Function<String, Integer> measure) {
        throw new UnsupportedOperationException("TODO: compose clean then measure");
    }

    static Function<Integer, Integer> surround(Function<Integer, Integer> core,
            Function<Integer, Integer> before, Function<Integer, Integer> after) {
        throw new UnsupportedOperationException("TODO: run before, core, after");
    }

    static Predicate<String> guarded(Predicate<String> guard, Predicate<String> rule) {
        throw new UnsupportedOperationException("TODO: short-circuit the supplied predicates");
    }

    static Predicate<String> either(Predicate<String> first, Predicate<String> second) {
        throw new UnsupportedOperationException("TODO: short-circuit alternatives");
    }

    static Predicate<String> opposite(Predicate<String> rule) {
        throw new UnsupportedOperationException("TODO: negate the supplied behavior");
    }

    static Function<String, Integer> parser() {
        throw new UnsupportedOperationException("TODO: static method reference");
    }

    static Consumer<String> appender(List<String> target) {
        throw new UnsupportedOperationException("TODO: bound instance method reference");
    }

    static BiFunction<String, String, Integer> comparison() {
        throw new UnsupportedOperationException("TODO: unbound instance method reference");
    }

    static Supplier<List<String>> listFactory() {
        throw new UnsupportedOperationException("TODO: constructor reference");
    }

    static IntUnaryOperator offsetBy(int offset) {
        throw new UnsupportedOperationException("TODO: capture the supplied offset");
    }

    public static void main(String[] args) {
        List<String> trace = new ArrayList<>();
        Function<String, Integer> operation = pipeline(s -> {
            trace.add("clean"); return s.trim();
        }, s -> { trace.add("measure"); return s.length(); });
        check("constructing behavior is lazy", List.of(), trace);
        check("clean before measuring", 4, operation.apply("  Java  "));
        check("callback order", List.of("clean", "measure"), trace);
        check("function is reusable", 1, operation.apply(" x "));
        check("compose and andThen have distinct positions", 70,
                surround(n -> n * 2, n -> n + 3, n -> n * 5).apply(4));

        AtomicInteger calls = new AtomicInteger();
        Predicate<String> usable = guarded(s -> s != null, s -> {
            calls.incrementAndGet(); return !s.isBlank();
        });
        check("null is rejected without dereference", false, usable.test(null));
        check("guard suppresses the second callback", 0, calls.get());
        check("blank fails the second callback", false, usable.test(" "));
        check("ordinary value passes", true, usable.test("Java"));
        check("second callback ran only when allowed", 2, calls.get());
        AtomicInteger alternatives = new AtomicInteger();
        Predicate<String> allowed = either("admin"::equals, value -> {
            alternatives.incrementAndGet(); return "reader".equals(value);
        });
        check("first alternative succeeds", true, allowed.test("admin"));
        check("or skips second callback after success", 0, alternatives.get());
        check("second alternative may succeed", true, allowed.test("reader"));
        check("neither alternative succeeds", false, allowed.test("guest"));
        check("second alternative runs after first fails", 2, alternatives.get());
        check("negation reverses a match", false, opposite("reader"::equals).test("reader"));
        check("negation reverses a miss", true, opposite("reader"::equals).test("guest"));

        check("static reference", -42, parser().apply("-42"));
        expect(NumberFormatException.class, () -> parser().apply("forty"));
        List<String> target = new ArrayList<>();
        Consumer<String> append = appender(target);
        target.add("before"); append.accept("after");
        check("bound receiver is the original mutable object", List.of("before", "after"), target);
        check("first SAM argument is the receiver", "a".compareTo("z"), comparison().apply("a", "z"));
        Supplier<List<String>> factory = listFactory();
        List<String> first = factory.get();
        List<String> second = factory.get();
        first.add("one");
        check("constructor creates independent mutable lists", List.of(), second);
        check("captured scalar", 12, offsetBy(7).applyAsInt(5));
        check("different captures remain independent", 3, offsetBy(-2).applyAsInt(5));
        report("Challenge 123");
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
