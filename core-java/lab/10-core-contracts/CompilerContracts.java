/*
 * Challenge 121 — Compiler Contracts Predictions (Medium)
 *
 * Task: Fill predictions() with true/false for whether each isolated snippet
 * compiles. Inspect the source first and explain a rule for every rejection.
 * Do not edit snippets or the compiler harness. Full JDK 21+ is required.
 * Compilation targets Java 21, uses no external libraries, and writes only to
 * an automatically removed temporary directory. Diagnostics are shown after
 * predictions, never treated as stable strings to memorize.
 * Run: java 10-core-contracts/CompilerContracts.java
 */
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;

public final class CompilerContracts {
    static Map<String, Boolean> predictions() {
        throw new UnsupportedOperationException("TODO: predict which snippets compile");
    }

    record Snippet(String name, Map<String, String> sources) { }
    static Snippet snippet(String name, String source) {
        return new Snippet(name, Map.of("Probe", source));
    }

    static List<Snippet> snippets() {
        return List.of(
            snippet("functional-object-method", """
                @FunctionalInterface interface Action {
                    boolean equals(Object other);
                    void run();
                    default void log() { }
                }
                class Probe { Action action = () -> {}; }
                """),
            snippet("two-abstract-methods", """
                @FunctionalInterface interface Action { void run(); void stop(); }
                class Probe { }
                """),
            snippet("lambda-target-object", """
                class Probe { Object action = () -> 1; }
                """),
            snippet("reassigned-capture", """
                class Probe { void test() {
                    int offset = 1;
                    java.util.function.IntUnaryOperator f = n -> n + offset;
                    offset = 2;
                } }
                """),
            snippet("mutable-captured-object", """
                class Probe { void test() {
                    var values = new java.util.ArrayList<String>();
                    Runnable r = () -> values.add("x");
                    values.add("y");
                } }
                """),
            snippet("checked-lambda", """
                class Probe { Runnable action = () -> { throw new java.io.IOException(); }; }
                """),
            snippet("ambiguous-target", """
                class Probe {
                    static void use(java.util.function.Predicate<String> f) { }
                    static void use(java.util.function.Function<String, Boolean> f) { }
                    void test() { use(s -> s.isEmpty()); }
                }
                """),
            snippet("explicit-target", """
                class Probe {
                    static void use(java.util.function.Predicate<String> f) { }
                    static void use(java.util.function.Function<String, Boolean> f) { }
                    void test() { use((java.util.function.Predicate<String>) s -> s.isEmpty()); }
                }
                """),
            snippet("generic-invariance", """
                class Probe { java.util.List<Number> values = new java.util.ArrayList<Integer>(); }
                """),
            snippet("extends-write", """
                class Probe { void add(java.util.List<? extends Number> values) { values.add(1); } }
                """),
            snippet("super-write", """
                class Probe { void add(java.util.List<? super Integer> values) { values.add(1); } }
                """),
            snippet("non-reifiable-instanceof", """
                class Probe { boolean test(Object value) { return value instanceof java.util.List<String>; } }
                """),
            snippet("covariant-return", """
                class Parent { Number value() { return 1; } }
                class Probe extends Parent { @Override Integer value() { return 2; } }
                """),
            snippet("wider-checked-override", """
                class Parent { void read() throws java.io.IOException { } }
                class Probe extends Parent { @Override void read() throws Exception { } }
                """),
            snippet("dropped-checked-override", """
                class Parent { void read() throws java.io.IOException { } }
                class Probe extends Parent { @Override void read() { } }
                """),
            snippet("caller-reference-contract", """
                class Parent { void read() throws java.io.IOException { } }
                class Probe extends Parent {
                    @Override void read() { }
                    void test() { Parent p = new Probe(); p.read(); }
                }
                """),
            snippet("narrowed-visibility", """
                class Parent { public void run() { } }
                class Probe extends Parent { @Override protected void run() { } }
                """),
            snippet("unrelated-defaults", """
                interface A { default int value() { return 1; } }
                interface B { default int value() { return 2; } }
                class Probe implements A, B { }
                """),
            snippet("definite-assignment", """
                class Probe { int value(boolean flag) { int n; if (flag) n = 1; return n; } }
                """),
            new Snippet("protected-own-receiver", Map.of(
                "p.Base", "package p; public class Base { protected int value = 1; }",
                "q.Probe", "package q; public class Probe extends p.Base { int read(Probe p) { return p.value; } }")),
            new Snippet("protected-parent-receiver", Map.of(
                "p.Base", "package p; public class Base { protected int value = 1; }",
                "q.Probe", "package q; public class Probe extends p.Base { int read(p.Base p) { return p.value; } }"))
        );
    }

    // ---- compiler harness (not part of the exercise) ----
    static boolean compiles(Snippet snippet, Path output) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Run with a full JDK, not a JRE");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<JavaFileObject> sources = snippet.sources().entrySet().stream()
                .map(entry -> (JavaFileObject) new SimpleJavaFileObject(
                        URI.create("string:///" + entry.getKey().replace('.', '/') + ".java"),
                        JavaFileObject.Kind.SOURCE) {
                    @Override public CharSequence getCharContent(boolean ignore) { return entry.getValue(); }
                }).toList();
        Files.createDirectories(output);
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            boolean success = compiler.getTask(null, manager, diagnostics,
                    List.of("--release", "21", "-proc:none", "-d", output.toString(),
                            "-classpath", output.toString()), null, sources).call();
            if (!success) diagnostics.getDiagnostics().stream()
                    .filter(d -> d.getKind() == Diagnostic.Kind.ERROR).findFirst()
                    .ifPresent(d -> System.out.println("      compiler: " + d.getMessage(Locale.ROOT)));
            return success;
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Boolean> guesses = predictions();
        List<Snippet> cases = snippets();
        check("every snippet has exactly one prediction",
                new HashSet<>(cases.stream().map(Snippet::name).toList()), guesses.keySet());
        Path build = Files.createTempDirectory("compiler-contracts-");
        try {
            for (Snippet snippet : cases) {
                check(snippet.name(), guesses.get(snippet.name()), compiles(snippet, build.resolve(snippet.name())));
            }
        } finally {
            try (var files = Files.walk(build)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
            }
        }
        report("Challenge 121");
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
