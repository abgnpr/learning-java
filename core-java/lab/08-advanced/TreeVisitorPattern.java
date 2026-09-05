/*
 * Challenge 59: Java Visitor Pattern (Medium)
 *
 * Task: Complete double dispatch for value and branch nodes so independent
 * visitors can sum values, count values, or render the same tree.
 * Complete: ValueNode.accept(TreeVisitor) and BranchNode.accept(TreeVisitor).
 * Run: java TreeVisitorPattern.java
 */
import java.util.List;

public class TreeVisitorPattern {
    interface TreeVisitor<R> {
        R visitValue(ValueNode node);

        R visitBranch(BranchNode node);
    }

    abstract static class TreeNode {
        public abstract <R> R accept(TreeVisitor<R> visitor);
    }

    static final class ValueNode extends TreeNode {
        private final int value;

        ValueNode(int value) {
            this.value = value;
        }

        int value() {
            return value;
        }

        @Override
        public <R> R accept(TreeVisitor<R> visitor) {
            // TODO: Dispatch this concrete node to the matching visitor method.
            throw new UnsupportedOperationException("TODO: implement ValueNode.accept");
        }
    }

    static final class BranchNode extends TreeNode {
        private final String label;
        private final List<TreeNode> children;

        BranchNode(String label, List<TreeNode> children) {
            this.label = label;
            this.children = List.copyOf(children);
        }

        String label() {
            return label;
        }

        List<TreeNode> children() {
            return children;
        }

        @Override
        public <R> R accept(TreeVisitor<R> visitor) {
            // TODO: Dispatch this concrete node to the matching visitor method.
            throw new UnsupportedOperationException("TODO: implement BranchNode.accept");
        }
    }

    static final class SumVisitor implements TreeVisitor<Integer> {
        @Override
        public Integer visitValue(ValueNode node) {
            return node.value();
        }

        @Override
        public Integer visitBranch(BranchNode node) {
            int total = 0;
            for (TreeNode child : node.children()) {
                total += child.accept(this);
            }
            return total;
        }
    }

    static final class ValueCountVisitor implements TreeVisitor<Integer> {
        @Override
        public Integer visitValue(ValueNode node) {
            return 1;
        }

        @Override
        public Integer visitBranch(BranchNode node) {
            int count = 0;
            for (TreeNode child : node.children()) {
                count += child.accept(this);
            }
            return count;
        }
    }

    static final class RenderVisitor implements TreeVisitor<String> {
        @Override
        public String visitValue(ValueNode node) {
            return Integer.toString(node.value());
        }

        @Override
        public String visitBranch(BranchNode node) {
            StringBuilder result = new StringBuilder(node.label()).append('[');
            for (int index = 0; index < node.children().size(); index++) {
                if (index > 0) {
                    result.append(',');
                }
                result.append(node.children().get(index).accept(this));
            }
            return result.append(']').toString();
        }
    }

    public static void main(String[] args) {
        TreeNode tree = new BranchNode("root", List.of(
                new ValueNode(2),
                new BranchNode("right", List.of(new ValueNode(3), new ValueNode(5)))));
        check("sum visitor", "tree.accept(new SumVisitor())", 10, tree.accept(new SumVisitor()));
        check("count visitor", "tree.accept(new ValueCountVisitor())", 3, tree.accept(new ValueCountVisitor()));
        check("render visitor", "tree.accept(new RenderVisitor())", "root[2,right[3,5]]", tree.accept(new RenderVisitor()));
        report("Challenge 59");
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
