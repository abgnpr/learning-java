/*
 * Challenge 29 — Java 2D Array (Easy)
 *
 * Task: Find the greatest hourglass sum in a rectangular integer grid with at
 * least three rows and three columns. An hourglass uses three cells, then the
 * center cell, then three cells below.
 * Complete: maxHourglassSum(int[][] grid)
 * Run: java HourglassSum.java
 */
public class HourglassSum {
    private static int _sum(int[][] grid, int i, int j) {
        return grid[i][j] + grid[i][j + 1] + grid[i][j + 2]
                + grid[i + 1][j + 1]
                + grid[i + 2][j] + grid[i + 2][j + 1] + grid[i + 2][j + 2];
    }

    /*
     * i and j are the hourglass's TOP-LEFT corner, so both must start at 0 —
     * they are corners to try, not "inner" cells to walk. Starting at 1:
     *
     *     for (int i = 1; i < grid.length - 2; i++)
     *         for (int j = 1; j < grid[i].length - 2; j++)
     *
     * silently drops the whole top row and left column of corners, and on a
     * 3-row grid the condition is 1 < 1 — the body never runs at all and the
     * seed leaks out as the answer. A plausible number, never an exception.
     *
     * Seeding from a real hourglass rather than 0 is the point: sums go
     * negative, so `largestSum = 0` would return 0 for an all-negative grid —
     * a value no hourglass has. Integer.MIN_VALUE works too and fails louder
     * when nothing is scanned; 0 is the only wrong answer here.
     */
    static int maxHourglassSum(int[][] grid) {
        int largestSum = _sum(grid, 0, 0);
        // - 2 leaves room for the two rows/columns below and right that _sum
        // reaches; i + 2 < grid.length is the same bound written to match it.
        for (int i = 0; i < grid.length - 2; i++) {
            for (int j = 0; j < grid[i].length - 2; j++) {
                int sum = _sum(grid, i, j);
                if (sum > largestSum) {
                    largestSum = sum;
                }
            }
        }
        return largestSum;
    }

    public static void main(String[] args) {
        check("single hourglass", "new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } }", 35,
                maxHourglassSum(new int[][] {
                        { 1, 2, 3 },
                        { 4, 5, 6 },
                        { 7, 8, 9 }
                }));

        check("all-negative grid", "new int[][] { { -1, -1, -1 }, { -1, -1, -1 }, { -1, -1, -1 } }", -7,
                maxHourglassSum(new int[][] {
                        { -1, -1, -1 },
                        { -1, -1, -1 },
                        { -1, -1, -1 }
                }));

        check("multiple candidates",
                "new int[][] { { 1, 1, 1, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0 }, { 0, 0, 2, 4, 4, 0 }, { 0, 0, 0, 2, 0, 0 }, { 0, 0, 1, 2, 4, 0 } }",
                19, maxHourglassSum(new int[][] {
                        { 1, 1, 1, 0, 0, 0 },
                        { 0, 1, 0, 0, 0, 0 },
                        { 1, 1, 1, 0, 0, 0 },
                        { 0, 0, 2, 4, 4, 0 },
                        { 0, 0, 0, 2, 0, 0 },
                        { 0, 0, 1, 2, 4, 0 }
                }));

        check("max in top row, not at column 0",
                "new int[][] { { 0, 9, 9, 9 }, { 0, 0, 9, 0 }, { 0, 9, 9, 9 }, { 0, 0, 0, 0 } }",
                63, maxHourglassSum(new int[][] {
                        { 0, 9, 9, 9 },
                        { 0, 0, 9, 0 },
                        { 0, 9, 9, 9 },
                        { 0, 0, 0, 0 }
                }));

        check("max in left column, not at row 0",
                "new int[][] { { 0, 0, 0, 0 }, { 9, 9, 9, 0 }, { 0, 9, 0, 0 }, { 9, 9, 9, 0 } }",
                63, maxHourglassSum(new int[][] {
                        { 0, 0, 0, 0 },
                        { 9, 9, 9, 0 },
                        { 0, 9, 0, 0 },
                        { 9, 9, 9, 0 }
                }));

        check("max at the bottom-right corner",
                "new int[][] { { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 9, 9, 9 }, { 0, 0, 9, 0 }, { 0, 9, 9, 9 } }",
                63, maxHourglassSum(new int[][] {
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 },
                        { 0, 9, 9, 9 },
                        { 0, 0, 9, 0 },
                        { 0, 9, 9, 9 }
                }));

        check("non-square grid, wide", "new int[][] { { 1, 1, 1, 0, 5 }, { 0, 1, 0, 5, 0 }, { 1, 1, 1, 5, 5 } }",
                22, maxHourglassSum(new int[][] {
                        { 1, 1, 1, 0, 5 },
                        { 0, 1, 0, 5, 0 },
                        { 1, 1, 1, 5, 5 }
                }));

        check("all-negative, best is the least bad",
                "new int[][] { { -9, -9, -9, -1, -1, -1 }, { -9, -9, -9, -9, -1, -9 }, { -9, -9, -9, -1, -1, -1 } }",
                -7, maxHourglassSum(new int[][] {
                        { -9, -9, -9, -1, -1, -1 },
                        { -9, -9, -9, -9, -1, -9 },
                        { -9, -9, -9, -1, -1, -1 }
                }));

        report("Challenge 29");
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
