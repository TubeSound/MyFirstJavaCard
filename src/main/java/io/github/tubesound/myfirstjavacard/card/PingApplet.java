package io.github.tubesound.myfirstjavacard.card;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

/** The first applet: 00 10 00 00 04 returns ASCII "PING" and 9000. */
public final class PingApplet extends Applet {

    private static final byte INS_PING = (byte) 0x10;
    private static final short PING_LENGTH = (short) 4;

    private PingApplet() {
    }

    public static void install(byte[] installData, short offset, byte length) {
        new PingApplet().register();
    }

    @Override
    public void process(APDU apdu) {
        // The JCRE also invokes process() for SELECT APDU.
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();
        if (buffer[ISO7816.OFFSET_CLA] != (byte) 0x00) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        if (buffer[ISO7816.OFFSET_INS] != INS_PING) {
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
        if (buffer[ISO7816.OFFSET_P1] != (byte) 0x00
                || buffer[ISO7816.OFFSET_P2] != (byte) 0x00) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        short le = apdu.setOutgoing();
        if (le < PING_LENGTH) {
            ISOException.throwIt((short) (ISO7816.SW_CORRECT_LENGTH_00 | PING_LENGTH));
        }

        apdu.setOutgoingLength(PING_LENGTH);
        buffer[0] = (byte) 'P';
        buffer[1] = (byte) 'I';
        buffer[2] = (byte) 'N';
        buffer[3] = (byte) 'G';
        apdu.sendBytes((short) 0, PING_LENGTH);
    }
}
