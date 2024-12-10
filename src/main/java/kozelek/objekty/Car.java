package kozelek.objekty;

import kozelek.Helper;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.util.ArrayList;

public class Car implements IRecord<Car> {
    private String meno;
    private String priezvisko;
    private int id;
    private String SPZ;
    // max 5 záznamov
    private ArrayList<Servis> servisy;
    private Servis servisInstance;

    public Car() {
        this.servisInstance = new Servis();
    }

    public Car(int id) {
        this.id = id;
        this.SPZ = "";
    }

    public Car(String SPZ) {
        this.SPZ = SPZ;
        this.id = -10;
    }

    public Car(String meno, String priezvisko, int id, String ECV) {
        this.setMeno(meno);
        this.setPriezvisko(priezvisko);
        this.id = id;
        this.setECV(ECV);
        this.servisy = new ArrayList<>();
        this.servisInstance = new Servis();
    }

    public void addServis(Servis servis) {
        if (servisy.size() < Helper.MAX_POCET_SERVISOV)
            servisy.add(servis);
        else
            throw new RuntimeException("Auto ma plne servisy: " + this.id);
    }

    public void removeServis(Servis servis) {
        servisy.remove(servis);
    }

    public String getMeno() {
        return meno;
    }

    public String getPriezvisko() {
        return priezvisko;
    }

    public ArrayList<Servis> getServisy() {
        return servisy;
    }

    public void setMeno(String meno) {
        if (meno.length() > Helper.MAX_MENO)
            throw new IllegalArgumentException("String longer than should be, NAME: " + meno);
        this.meno = meno;
    }

    public void setPriezvisko(String priezvisko) {
        if (priezvisko.length() > Helper.MAX_PRIEZVISKO)
            throw new IllegalArgumentException("String longer than should be, PRIEZIVKOS: " + priezvisko);
        this.priezvisko = priezvisko;
    }

    public void setECV(String ECV) {
        if (ECV.length() > Helper.MAX_SPZ)
            throw new IllegalArgumentException("String longer than should be, ECV: " + ECV);
        this.SPZ = ECV;
    }

    @Override
    public int getSize() {
        // 1 + 15
        // 1 + 20
        // 4
        // 1 + 10
        return Helper.VELKOST_BYTE + Helper.MAX_MENO + // meno
                Helper.VELKOST_BYTE + Helper.MAX_PRIEZVISKO + // priezvisko
                Helper.VELKOST_INTEGER + // id
                Helper.VELKOST_BYTE + Helper.MAX_SPZ
                + Helper.VELKOST_BYTE + (Helper.MAX_POCET_SERVISOV * this.servisInstance.getSize());
    }

    @Override
    public byte[] toByteArray() {
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            // byte _ meno
            Helper.writeString(dataStream, meno, Helper.MAX_MENO);

            // byte _ priezvisko
            Helper.writeString(dataStream, priezvisko, Helper.MAX_PRIEZVISKO);

            dataStream.writeInt(id);

            // byte _ ECV
            Helper.writeString(dataStream, SPZ, Helper.MAX_SPZ);

            int size = this.servisInstance.getSize();

            // pocet servisov
            dataStream.writeByte(servisy.size());
            for (int i = 0; i < Helper.MAX_POCET_SERVISOV; i++) {
                if (i < servisy.size()) {
                    byte[] bytes = servisy.get(i).toByteArray();
                    dataStream.write(bytes);
                } else {
                    dataStream.write(new byte[size]);
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

            this.meno = Helper.readString(dataStream, Helper.MAX_MENO);
            this.priezvisko = Helper.readString(dataStream, Helper.MAX_PRIEZVISKO);
            this.id = dataStream.readInt();
            this.SPZ = Helper.readString(dataStream, Helper.MAX_SPZ);


            this.servisy = new ArrayList<>();

            byte pocetServisov = dataStream.readByte();
            for (int i = 0; i < Helper.MAX_POCET_SERVISOV; i++) {
                if (i < pocetServisov) {
                    byte[] recordBytes = new byte[this.servisInstance.getSize()];
                    dataStream.readFully(recordBytes);

                    Servis servis = new Servis();
                    servis.fromByteArray(recordBytes);
                    this.servisy.add(servis);
                } else {
                    dataStream.skipBytes(this.servisInstance.getSize());
                }

            }
        } catch (IOException e) {
            throw new RuntimeException("Error, car frombytearray", e);
        }
    }


    @Override
    public String toString() {
        return  "id=" + id +
                ", SPZ='" + SPZ + '\'' +
                ", meno='" + meno + '\'' +
                ", priezvisko='" + priezvisko + '\'' +
                ", serv.=" + servisy;
    }

    @Override
    public boolean equal(Car other) {
        return this.id == other.id || this.SPZ.equals(other.SPZ);
    }

    @Override
    public Car createInstance() {
        return new Car();
    }

    public int getId() {
        return id;
    }

    public String getSPZ() {
        return SPZ;
    }
}
