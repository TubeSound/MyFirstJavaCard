package io.github.tubesound.myfirstjavacard.card;

public final class TestRunner {

    private TestRunner() {
    }

    public static void main(String[] args) {
        System.out.println("MyFirstJavaCard - jCardSim tests");
        PingAppletTest.runAll();
        System.out.println("All 7 tests passed.");
    }
}
