package kozelek.objekty.HashObjekty;

import kozelek.Helper;
import kozelek.interfaces.IHashableData;

import java.io.*;
import java.util.Arrays;
import java.util.BitSet;

public class CarSPZ implements IHashableData<CarSPZ> {
    private String ECV;
    private int address;

    public CarSPZ(String ECV, int address) {
        this.ECV = ECV;
        this.address = address;
    }

    public CarSPZ() {
    }

    @Override
    public BitSet getHash() {
        byte[] ecvBytes = ECV.getBytes();

        int start = Math.max(0, ecvBytes.length - 4);

        byte[] lastBytes = Arrays.copyOfRange(ecvBytes, start, ecvBytes.length);
        return BitSet.valueOf(lastBytes);
    }

    @Override
    public boolean equal(CarSPZ other) {
        return this.ECV.equals(other.ECV);
    }

    @Override
    public CarSPZ createInstance() {
        return new CarSPZ();
    }

    @Override
    public int getSize() {
        return Helper.VELKOST_BYTE + Helper.MAX_ECV + Helper.VELKOST_INTEGER;
    }

    @Override
    public byte[] toByteArray() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            Helper.writeString(dataStream, ECV, Helper.MAX_ECV);
            dataStream.writeInt(this.address);

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error tobytearray", e);
        }

    }

    @Override
    public void fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             DataInputStream dataStream = new DataInputStream(byteStream)) {

            this.ECV = Helper.readString(dataStream, Helper.MAX_ECV);
            this.address = dataStream.readInt();

        } catch (IOException e) {
            throw new RuntimeException("Error frombytearray", e);
        }
    }
}
