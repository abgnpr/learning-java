
import java.util.function.Predicate;

public class Scratchpad {

    private static final Predicate<String> p = (String s) -> s.equalsIgnoreCase("abhigyan");

    public static void main(String[] args) {
        System.out.println(p.test("s"));
    }
}