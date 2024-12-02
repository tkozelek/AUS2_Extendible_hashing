package kozelek.objekty;

import kozelek.Helper;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Servis implements IRecord<Servis> {
    private LocalDate datum;
    private double cena;
    private ArrayList<String> popis;

    public Servis() {
        this.popis = new ArrayList<>();
    }

    public Servis(LocalDate datum, double cena) {
        this.datum = datum;
        this.cena = cena;
        this.popis = new ArrayList<>();
    }

    public void addPopis(String popis) {
        if (popis.length() > Helper.MAX_POPIS_DLZKA) {
            throw new IllegalArgumentException("Popis size too large");
        }
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
        return Helper.VELKOST_INTEGER + 2 * Helper.VELKOST_BYTE +
                Helper.VELKOST_DOUBLE +
                (Helper.MAX_POPIS_POCET * (Helper.VELKOST_BYTE + Helper.MAX_POPIS_DLZKA));
    }

    @Override
    public byte[] toByteArray() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            dataStream.writeInt(datum.getYear());
            dataStream.writeByte(datum.getMonthValue());
            dataStream.writeByte(datum.getDayOfMonth());

            dataStream.writeDouble(cena);

            for (int i = 0; i < Helper.MAX_POPIS_POCET; i++) {
                if (i < popis.size() && popis.get(i) != null) {
                    Helper.writeString(dataStream, popis.get(i), Helper.MAX_POPIS_DLZKA);
                } else {
                    dataStream.writeByte(0);
                    dataStream.write(new byte[Helper.MAX_POPIS_DLZKA]);
                }
            }

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error servis tobytearray", e);
        }
    }

    @Override
    public void fromByteArray(byte[] bytes) {
        if (bytes == null || bytes.length != this.getSize()) {
            throw new IllegalArgumentException("Invalid byte array size");
        }

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)) {
            DataInputStream dataStream = new DataInputStream(byteStream);

            int year = dataStream.readInt();
            int month = dataStream.readByte();
            int day = dataStream.readByte();
            this.datum = LocalDate.of(year, month, day);

            this.cena = dataStream.readDouble();

            this.popis = new ArrayList<>();
            for (int i = 0; i < Helper.MAX_POPIS_POCET; i++) {
                String description = Helper.readString(dataStream, Helper.MAX_POPIS_DLZKA);
                if (description != null) {
                    this.popis.add(description);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error servis, frombytearray", e);
        }
    }

    @Override
    public boolean equal(Servis other) {
        if (other == null) return false;
        return this.datum.equals(other.datum);
    }

    @Override
    public Servis createInstance() {
        return new Servis();
    }

    @Override
    public String toString() {
        return "Servis{" +
                "datum=" + datum.getDayOfMonth() + "." + datum.getMonthValue() + "." + datum.getYear() +
                ", cena=" + cena +
                ", popis=" + popis +
                '}';
    }
}
