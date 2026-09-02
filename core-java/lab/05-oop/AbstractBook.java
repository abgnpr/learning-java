/*
 * Challenge 45: Java Abstract Class (Easy)
 *
 * Task: Implement a concrete book whose title can be assigned through the
 * abstract contract supplied by Book.
 * Complete: MyBook.setTitle(String).
 * Run: java AbstractBook.java
 */
public class AbstractBook {
    abstract static class Book {
        protected String title;

        public String getTitle() {
            return title;
        }

        public abstract void setTitle(String title);
    }

    static final class MyBook extends Book {
        @Override
        public void setTitle(String title) {
            // TODO: Store the supplied title in the inherited state.
            throw new UnsupportedOperationException("TODO: implement setTitle");
        }
    }

    public static void main(String[] args) {
        MyBook book = new MyBook();
        book.setTitle("A Tale of Two Cities");
        checkEquals("A Tale of Two Cities", book.getTitle(), "first title");
        book.setTitle("The Hobbit");
        checkEquals("The Hobbit", book.getTitle(), "replacement title");
        book.setTitle("");
        checkEquals("", book.getTitle(), "empty title");
        System.out.println("Challenge 45 passed!");
    }

    private static void checkEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
