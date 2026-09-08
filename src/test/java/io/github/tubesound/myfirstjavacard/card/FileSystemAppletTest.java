package io.github.tubesound.myfirstjavacard.card;

import com.licel.jcardsim.base.Simulator;
import com.licel.jcardsim.utils.AIDUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javacard.framework.AID;

/** COD, IEF, WEF and key-object tests executed without JUnit. */
public final class FileSystemAppletTest {

    private static final byte[] SELECT_WEF = {
        0x00, (byte) 0xA4, 0x02, 0x0C, 0x02, 0x10, 0x01
    };
    private static final byte[] SELECT_IEF = {
        0x00, (byte) 0xA4, 0x02, 0x0C, 0x02, 0x10, 0x02
    };

    private Simulator simulator;
    private AID appletAID;

    public static void main(String[] args) {
        runAll();
    }

    static void runAll() {
        FileSystemAppletTest suite = new FileSystemAppletTest();
        TestSupport.run("selected WEF can be read by offset",
                suite::selectedWefCanBeReadByOffset);
        TestSupport.run("READ requires a selected WEF",
                suite::readRequiresASelectedWef);
        TestSupport.run("unknown FID returns 6A82",
                suite::unknownFileIdReturns6A82);
        TestSupport.run("failed PIN verification decrements tries",
                suite::failedVerificationDecrementsTries);
        TestSupport.run("UPDATE requires PIN verification",
                suite::updateRequiresKeyVerification);
        TestSupport.run("WEF update survives deselect and reset",
                suite::verifiedUpdateSurvivesDeselectAndReset);
        TestSupport.run("COD areas have different lifetimes",
                suite::codDeselectAndResetAreasHaveDifferentLifetimes);
    }

    private void setUp() {
        simulator = new Simulator();
        appletAID = AIDUtil.create("F0545542450201");
        simulator.installApplet(appletAID, FileSystemApplet.class);
        TestSupport.assertTrue(simulator.selectApplet(appletAID),
                "Applet must accept SELECT");
    }

    private void selectedWefCanBeReadByOffset() {
        setUp();
        assertStatus(0x9000, SELECT_WEF);
        byte[] response = transmit(new byte[] {
            0x00, (byte) 0xB0, 0x00, 0x06, 0x03
        });

        TestSupport.assertEquals(0x9000, statusWord(response),
                "READ BINARY must succeed");
        TestSupport.assertArrayEquals("WEF".getBytes(StandardCharsets.US_ASCII),
                responseData(response), "Unexpected WEF data");
    }

    private void readRequiresASelectedWef() {
        setUp();
        assertStatus(0x6985,
                new byte[] {0x00, (byte) 0xB0, 0x00, 0x00, 0x08});
        assertStatus(0x9000, SELECT_IEF);
        assertStatus(0x6986,
                new byte[] {0x00, (byte) 0xB0, 0x00, 0x00, 0x08});
    }

    private void unknownFileIdReturns6A82() {
        setUp();
        assertStatus(0x6A82,
                new byte[] {0x00, (byte) 0xA4, 0x02, 0x0C,
                    0x02, 0x7F, 0x01});
    }

    private void failedVerificationDecrementsTries() {
        setUp();
        assertStatus(0x9000, SELECT_IEF);
        assertStatus(0x63C2,
                new byte[] {0x00, 0x20, 0x00, (byte) 0x80,
                    0x04, '0', '0', '0', '0'});
    }

    private void updateRequiresKeyVerification() {
        setUp();
        assertStatus(0x9000, SELECT_WEF);
        assertStatus(0x6982,
                new byte[] {0x00, (byte) 0xD6, 0x00, 0x00,
                    0x02, 'O', 'K'});
    }

    private void verifiedUpdateSurvivesDeselectAndReset() {
        setUp();
        verifyPin();
        assertStatus(0x9000, SELECT_WEF);
        assertStatus(0x9000, new byte[] {
            0x00, (byte) 0xD6, 0x00, 0x00, 0x08,
            'J', 'A', 'V', 'A', '-', 'E', 'F', '!'
        });

        AID otherAID = AIDUtil.create("F0545542450101");
        simulator.installApplet(otherAID, PingApplet.class);
        TestSupport.assertTrue(simulator.selectApplet(otherAID),
                "Other applet must be selected");
        TestSupport.assertTrue(simulator.selectApplet(appletAID),
                "File applet must be reselected");

        // CLEAR_ON_DESELECT forgot the selected FID.
        assertStatus(0x6985,
                new byte[] {0x00, (byte) 0xB0, 0x00, 0x00, 0x08});
        assertStatus(0x9000, SELECT_WEF);
        assertWefEquals("JAVA-EF!");

        // deselect() also cleared successful PIN validation.
        assertStatus(0x6982,
                new byte[] {0x00, (byte) 0xD6, 0x00, 0x00, 0x01, '!'});

        simulator.reset();
        TestSupport.assertTrue(simulator.selectApplet(appletAID),
                "File applet must be selected after reset");
        assertStatus(0x9000, SELECT_WEF);
        assertWefEquals("JAVA-EF!");
    }

    private void codDeselectAndResetAreasHaveDifferentLifetimes() {
        setUp();
        assertStatus(0x9000, SELECT_WEF);
        assertStatus(0x9000, new byte[] {(byte) 0x80, 0x31, 0x00, 0x00});
        assertCodState(0x1001, 1);

        AID otherAID = AIDUtil.create("F0545542450101");
        simulator.installApplet(otherAID, PingApplet.class);
        TestSupport.assertTrue(simulator.selectApplet(otherAID),
                "Other applet must be selected");
        TestSupport.assertTrue(simulator.selectApplet(appletAID),
                "File applet must be reselected");
        assertCodState(0x0000, 1);

        simulator.reset();
        TestSupport.assertTrue(simulator.selectApplet(appletAID),
                "File applet must be selected after reset");
        assertCodState(0x0000, 0);
    }

    private void verifyPin() {
        assertStatus(0x9000, SELECT_IEF);
        assertStatus(0x9000,
                new byte[] {0x00, 0x20, 0x00, (byte) 0x80,
                    0x04, '1', '2', '3', '4'});
    }

    private void assertWefEquals(String expected) {
        byte[] response = transmit(new byte[] {
            0x00, (byte) 0xB0, 0x00, 0x00, 0x08
        });
        TestSupport.assertArrayEquals(expected.getBytes(StandardCharsets.US_ASCII),
                responseData(response), "Unexpected persistent WEF data");
        TestSupport.assertEquals(0x9000, statusWord(response),
                "READ BINARY must succeed");
    }

    private void assertCodState(int selectedFileId, int resetCounter) {
        byte[] response = transmit(new byte[] {
            (byte) 0x80, 0x30, 0x00, 0x00, 0x03
        });
        TestSupport.assertEquals(0x9000, statusWord(response),
                "GET COD STATE must succeed");
        TestSupport.assertArrayEquals(new byte[] {
            (byte) (selectedFileId >>> 8),
            (byte) selectedFileId,
            (byte) resetCounter
        }, responseData(response), "Unexpected COD state");
    }

    private void assertStatus(int expected, byte[] command) {
        byte[] response = transmit(command);
        TestSupport.assertEquals(expected, statusWord(response),
                "Unexpected status word");
        TestSupport.assertEquals(0, responseData(response).length,
                "Status-only response must not contain data");
    }

    private byte[] transmit(byte[] command) {
        byte[] response = simulator.transmitCommand(command);
        TestSupport.assertTrue(response.length >= 2,
                "Response must contain SW1 and SW2");
        return response;
    }

    private int statusWord(byte[] response) {
        int offset = response.length - 2;
        return ((response[offset] & 0xFF) << 8)
                | (response[offset + 1] & 0xFF);
    }

    private byte[] responseData(byte[] response) {
        return Arrays.copyOf(response, response.length - 2);
    }
}
