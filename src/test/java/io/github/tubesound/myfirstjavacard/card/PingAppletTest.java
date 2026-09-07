package io.github.tubesound.myfirstjavacard.card;

import com.licel.jcardsim.base.Simulator;
import com.licel.jcardsim.utils.AIDUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javacard.framework.AID;

/** Tests executed directly by Java, without Maven or JUnit. */
public final class PingAppletTest {

    private static final String APPLET_AID = "F0545542450101";

    private Simulator simulator;
    private AID appletAID;

    public static void main(String[] args) {
        runAll();
    }

    static void runAll() {
        PingAppletTest suite = new PingAppletTest();
        TestSupport.run("PING returns PING + 9000", suite::pingReturnsFourBytesAndSuccess);
        TestSupport.run("Le=00 means 256", suite::pingAcceptsLe256);
        TestSupport.run("unsupported CLA returns 6E00", suite::unsupportedClaReturns6E00);
        TestSupport.run("unsupported INS returns 6D00", suite::unsupportedInsReturns6D00);
        TestSupport.run("non-zero P1/P2 returns 6A86", suite::nonzeroP1P2Returns6A86);
        TestSupport.run("short Le returns 6C04", suite::shortLeReturns6C04);
        TestSupport.run("PING works after an error", suite::pingStillWorksAfterAnError);
    }

    private void setUp() {
        simulator = new Simulator();
        appletAID = AIDUtil.create(APPLET_AID);
        simulator.installApplet(appletAID, PingApplet.class);
        TestSupport.assertTrue(simulator.selectApplet(appletAID), "Applet must accept SELECT");
    }

    private void pingReturnsFourBytesAndSuccess() {
        setUp();
        assertPing(new byte[] {0x00, 0x10, 0x00, 0x00, 0x04});
    }

    private void pingAcceptsLe256() {
        setUp();
        assertPing(new byte[] {0x00, 0x10, 0x00, 0x00, 0x00});
    }

    private void unsupportedClaReturns6E00() {
        setUp();
        assertStatus(0x6E00, new byte[] {(byte) 0x80, 0x10, 0x00, 0x00, 0x04});
    }

    private void unsupportedInsReturns6D00() {
        setUp();
        assertStatus(0x6D00, new byte[] {0x00, 0x7F, 0x00, 0x00, 0x04});
    }

    private void nonzeroP1P2Returns6A86() {
        setUp();
        assertStatus(0x6A86, new byte[] {0x00, 0x10, 0x01, 0x00, 0x04});
        assertStatus(0x6A86, new byte[] {0x00, 0x10, 0x00, 0x01, 0x04});
    }

    private void shortLeReturns6C04() {
        setUp();
        assertStatus(0x6C04, new byte[] {0x00, 0x10, 0x00, 0x00, 0x03});
    }

    private void pingStillWorksAfterAnError() {
        setUp();
        assertStatus(0x6D00, new byte[] {0x00, 0x7F, 0x00, 0x00, 0x04});
        assertPing(new byte[] {0x00, 0x10, 0x00, 0x00, 0x04});
    }

    private void assertPing(byte[] command) {
        byte[] response = transmit(command);
        TestSupport.assertEquals(0x9000, statusWord(response), "Unexpected status word");
        TestSupport.assertArrayEquals("PING".getBytes(StandardCharsets.US_ASCII),
                responseData(response), "Unexpected response data");
    }

    private void assertStatus(int expected, byte[] command) {
        byte[] response = transmit(command);
        TestSupport.assertEquals(expected, statusWord(response), "Unexpected status word");
        TestSupport.assertEquals(0, responseData(response).length,
                "Error response must not contain stale data");
    }

    private byte[] transmit(byte[] command) {
        byte[] response = simulator.transmitCommand(command);
        TestSupport.assertTrue(response.length >= 2, "Response must contain SW1 and SW2");
        return response;
    }

    private int statusWord(byte[] response) {
        int offset = response.length - 2;
        return ((response[offset] & 0xFF) << 8) | (response[offset + 1] & 0xFF);
    }

    private byte[] responseData(byte[] response) {
        return Arrays.copyOf(response, response.length - 2);
    }
}
