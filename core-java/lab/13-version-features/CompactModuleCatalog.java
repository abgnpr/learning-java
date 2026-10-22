/*
 * Challenge 119: Compact Source and Module Import (Easy)
 * Java features: Java 25 module imports and compact source files (JEPs 511, 512).
 *
 * Task: Complete sqlTypes() in this compact source file. JDBCType is available
 * through one module import; List comes from the implicit java.base module
 * import of a compact source file.
 *
 * Complete: sqlTypes().
 * Run on JDK 25+: java CompactModuleCatalog.java
 */
import module java.sql;

List<String> sqlTypes() {
    // TODO: Return the names of INTEGER, VARCHAR, and TIMESTAMP in that order.
    throw new UnsupportedOperationException("TODO: build the SQL type catalog");
}

void main() {
    check("module import exposes java.sql.JDBCType", "three JDBC types",
            List.of("INTEGER", "VARCHAR", "TIMESTAMP"), sqlTypes());
    report("Challenge 119");
}

int passes;
int failures;

void check(String label, Object input, Object expected, Object actual) {
    boolean ok = Objects.deepEquals(expected, actual);
    if (ok) passes++; else failures++;
    IO.println((ok ? "PASS  " : "FAIL  ") + label);
    IO.println("      input:    " + input);
    IO.println("      expected: " + expected);
    IO.println("      actual:   " + actual);
}

void report(String challenge) {
    IO.println("----");
    IO.println(challenge + ": " + passes + " passed, " + failures + " failed.");
    if (failures > 0) throw new AssertionError(challenge + " failed");
    IO.println(challenge + " passed.");
}
