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
        checkEquals(10, tree.accept(new SumVisitor()), "sum visitor");
        checkEquals(3, tree.accept(new ValueCountVisitor()), "count visitor");
        checkEquals("root[2,right[3,5]]", tree.accept(new RenderVisitor()), "render visitor");
        System.out.println("Challenge 59 passed!");
    }

    private static void checkEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
