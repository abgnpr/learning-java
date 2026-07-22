
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        var a = Arrays.asList(1, 2, 3);
        a.add(4);
        System.out.println(a.getClass());
        System.out.println(a);
    }
}