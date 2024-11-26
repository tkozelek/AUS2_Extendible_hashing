package kozelek;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

public class Helper {
    // maximalne velkosti
    // servis
    public static final int MAX_POPIS_POCET = 10;
    public static final int MAX_POPIS_DLZKA = 20;

    // car
    public static final int MAX_MENO = 15;
    public static final int MAX_PRIEZVISKO = 20;
    public static final int MAX_ECV = 10;
    public static final int MAX_POCET_SERVISOV = 5;

    // velkosti
    public static final int VELKOST_LONG = Long.BYTES;
    public static final int VELKOST_INTEGER = Integer.BYTES;
    public static final int VELKOST_BYTE = Byte.BYTES;


    /**
     * Zapíše byte (dlžku) a string do suboru
     *
     * @param dataStream - stream suboru
     * @param text - text na vlozenie
     * @param maxLengthOfString - maximalna dlzka vkladaneho stringu
     */
    public static void writeString(DataOutputStream dataStream, String text, int maxLengthOfString) {
        try {
            if (text != null) {
                byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                // dlzka stringu (1 byte)
                dataStream.writeByte(bytes.length);
                // string
                dataStream.write(bytes);
                // zbytok naplnime
                for (int i = 0; i < (maxLengthOfString - bytes.length); i++) {
                    dataStream.writeByte(0);
                }
            } else {
                dataStream.writeByte(0);
                for (int i = 0; i < maxLengthOfString; i++) {
                    dataStream.writeByte(0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * načíta a automaticky sa oreže podla načitaného bytu
     *
     * @param dataStream - stream dat
     * @param maxLengthOfString - maximalna dlzka nacitavaneho streamu
     * @return
     */
    public static String readString(DataInputStream dataStream, int maxLengthOfString) {
        try {
            int length = dataStream.readByte(); // dlzka stringu
            if (length > 0) {
                byte[] bytes = new byte[length];
                dataStream.readFully(bytes);
                dataStream.skipBytes(maxLengthOfString - length); // preskocime nepotrebne byty (0)
                return new String(bytes, StandardCharsets.UTF_8);
            } else {
                dataStream.skipBytes(maxLengthOfString); // ak je dlzka stringu 0, preskocime celych x bytov
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
