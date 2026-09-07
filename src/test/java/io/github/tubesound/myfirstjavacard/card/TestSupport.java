package io.github.tubesound.myfirstjavacard.card;

import java.util.Arrays;

final class TestSupport {

    interface TestCase {
        void run();
    }

    private TestSupport() {
    }

    static void run(String name, TestCase test) {
        try {
            test.run();
            System.out.println("[PASS] " + name);
        } catch (RuntimeException | AssertionError error) {
            System.err.println("[FAIL] " + name + ": " + error.getMessage());
            throw error;
        }
    }

    static void assertTrue(boolean actual, String message) {
        if (!actual) {
            throw new AssertionError(message);
        }
    }

    static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " expected=" + toHex(expected)
                    + " actual=" + toHex(actual));
        }
    }

    static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(message + " expected=" + Arrays.toString(expected)
                    + " actual=" + Arrays.toString(actual));
        }
    }

    private static String toHex(int value) {
        return String.format("0x%04X", value);
    }
}
