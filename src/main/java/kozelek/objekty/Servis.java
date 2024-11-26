package kozelek.objekty;

import kozelek.Helper;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Servis implements IRecord<Servis> {
    private Date datum;
    private double cena;
    private String[] popis;

    public Servis() {
    }

    public Servis(Date datum, double cena, String[] popis) {
        this.datum = datum;
        this.cena = cena;
        this.popis = popis;
    }

    public Servis(Date datum, double cena) {
        this.datum = datum;
        this.cena = cena;
        this.popis = new String[Helper.MAX_POPIS_POCET];
    }

    @Override
    public int getSize() {
        // datum + cena
        return Helper.VELKOST_LONG + Helper.VELKOST_LONG + Helper.MAX_POPIS_POCET * (1 + Helper.MAX_POPIS_DLZKA);
    }

    @Override
    public byte[] toByteArray() {
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);
            dataStream.writeLong(datum.getTime());
            dataStream.writeDouble(cena);
            for (int i = 0; i < Helper.MAX_POPIS_POCET; i++) {
                Helper.writeString(dataStream, popis[i], Helper.MAX_POPIS_DLZKA);
            }

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromByteArray(byte[] bytes) {
        if (this.getSize() != bytes.length) {
            throw new RuntimeException("Size mismatch");
        }

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)) {
            DataInputStream dataStream = new DataInputStream(byteStream);

            this.datum = new Date(dataStream.readLong());
            this.cena = dataStream.readDouble();
            this.popis = new String[Helper.MAX_POPIS_POCET];
            for (int i = 0; i < Helper.MAX_POPIS_POCET; i++) {
                this.popis[i] = Helper.readString(dataStream, Helper.MAX_POPIS_DLZKA);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public boolean equal(Servis other) {
        return this.datum.equals(other.datum);
    }

    @Override
    public Servis createInstance() {
        return new Servis();
    }
}
