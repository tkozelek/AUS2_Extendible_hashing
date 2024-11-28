package kozelek.objekty;

import jdk.jshell.spi.ExecutionControl;
import kozelek.Helper;
import kozelek.interfaces.IByteData;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class Car implements IRecord<Car> {
    private String meno;
    private String priezvisko;
    private int id;
    private String ECV;
    private Servis[] zaznamy;

    public Car() {
    }

    public Car(String meno, String priezvisko, int id, String ECV) {
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.id = id;
        this.ECV = ECV;
        this.zaznamy = new Servis[Helper.MAX_POCET_SERVISOV];
    }

    @Override
    public int getSize() {
        return Helper.VELKOST_BYTE + Helper.MAX_MENO + // meno
                Helper.VELKOST_BYTE + Helper.MAX_PRIEZVISKO + // priezvisko
                Helper.VELKOST_INTEGER + // id
                Helper.VELKOST_BYTE + Helper.MAX_ECV; //+ // ecv
                //(Helper.MAX_POCET_SERVISOV * new Servis().getSize());
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
            Helper.writeString(dataStream, ECV, Helper.MAX_ECV);

//            // byte pocet zaznamov
//            dataStream.writeByte(zaznamy.length);
//
//            for (Servis s : zaznamy) {
//                if (s != null) {
//                    byte[] bytes = s.toByteArray();
//                    dataStream.writeInt(bytes.length);
//                    dataStream.write(bytes);
//                } else {
//                    dataStream.writeInt(0);
//                }
//            }

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

            this.ECV = Helper.readString(dataStream, Helper.MAX_ECV);

//            int visitCount = dataStream.readByte();
//            this.zaznamy = new Servis[visitCount];
//
//            for (int i = 0; i < visitCount; i++) {
//                int length = dataStream.readInt();
//                if (length > 0) {
//                    byte[] byteArray = new byte[length];
//                    dataStream.read(byteArray);
//                    Servis servis = new Servis();
//                    servis.fromByteArray(byteArray);
//                    zaznamy[i] = servis;
//                } else {
//                    zaznamy[i] = null;
//                }
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public String toString() {
        return "Car{" +
                "meno='" + meno + '\'' +
                ", priezvisko='" + priezvisko + '\'' +
                ", id=" + id +
                ", ECV='" + ECV + '\'';
    }

    @Override
    public boolean equal(Car other) {
        return this.id == other.id || this.ECV.equals(other.ECV);
    }

    @Override
    public Car createInstance() {
        return new Car();
    }

    public int getId() {
        return id;
    }

    public String getECV() {
        return ECV;
    }
}
