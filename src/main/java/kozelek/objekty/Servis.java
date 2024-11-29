package kozelek.objekty;

import kozelek.Helper;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Servis implements IRecord<Servis> {
    private Date datum;
    private double cena;
    private ArrayList<String> popis;

    public Servis() {
    }

    public Servis(Date datum, double cena) {
        this.datum = datum;
        this.cena = cena;
        this.popis = new ArrayList<>();
    }

    public void addPopis(String popis) {
        this.popis.add(popis);
    }

    public void setPopis(int index, String popis) {
        this.popis.set(index, popis);
    }

    public String getPopis(int index) {
        return this.popis.get(index);
    }

    @Override
    public int getSize() {
        // datum + cena + POCETPOPISOV * (BYTE + DLZKAPOPISOV)
        return Helper.VELKOST_LONG + Helper.VELKOST_DOUBLE + (Helper.MAX_POPIS_POCET * (Helper.VELKOST_BYTE + Helper.MAX_POPIS_DLZKA));
    }

    @Override
    public byte[] toByteArray() {
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);
            dataStream.writeLong(datum.getTime());
            dataStream.writeDouble(cena);

            for (int i = 0; i < Helper.MAX_POPIS_POCET; i++) {
                if (i < popis.size()) {
                    Helper.writeString(dataStream, popis.get(i), Helper.MAX_POPIS_DLZKA);
                } else {
                    Helper.writeString(dataStream, null, Helper.MAX_POPIS_DLZKA);

                }
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
            this.popis = new ArrayList<>();
            for (int i = 0; i < Helper.MAX_POPIS_POCET; i++) {
                this.popis.add(Helper.readString(dataStream, Helper.MAX_POPIS_DLZKA));
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


    @Override
    public String toString() {
        return "Servis{" +
                "datum=" + datum +
                ", cena=" + cena +
                ", popis=" + popis +
                '}';
    }
}
