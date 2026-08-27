/*
 * Challenge 55: Can You Access? (Medium)
 *
 * Task: Use reflection to construct a private nested helper and invoke its
 * private power-of-two test despite normal access checks.
 * Complete: callHiddenPowerCheck(int).
 * Run: java PrivateMemberAccess.java
 */
public class PrivateMemberAccess {
    static boolean callHiddenPowerCheck(int value) throws ReflectiveOperationException {
        // TODO: Find AccessVault$PowerJudge, make its members accessible, and invoke it.
        throw new UnsupportedOperationException("TODO: implement callHiddenPowerCheck");
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        check(true, callHiddenPowerCheck(1), "one is a power of two");
        check(false, callHiddenPowerCheck(3), "three is not a power of two");
        check(true, callHiddenPowerCheck(1024), "1024 is a power of two");
        check(false, callHiddenPowerCheck(0), "zero is not a power of two");
        System.out.println("Challenge 55 passed!");
    }

    private static void check(boolean expected, boolean actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}

final class AccessVault {
    private AccessVault() {
    }

    private static final class PowerJudge {
        private PowerJudge() {
        }

        private boolean isPowerOfTwo(int value) {
            return value > 0 && (value & (value - 1)) == 0;
        }
    }
}
