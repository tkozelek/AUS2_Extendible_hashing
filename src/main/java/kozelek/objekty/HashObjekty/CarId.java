package kozelek.objekty.HashObjekty;

import kozelek.Helper;
import kozelek.interfaces.IHashableData;

import java.io.*;
import java.util.BitSet;

public class CarId implements IHashableData<CarId> {
    private int carId;
    private int adress;

    public CarId(int carId, int adress) {
        this.carId = carId;
        this.adress = adress;
    }

    public CarId() {
    }

    @Override
    public BitSet getHash() {
        if (carId == 0) {
            BitSet bitSet = new BitSet(1);
            bitSet.set(0);
            return bitSet;
        }

        return BitSet.valueOf(new long[] {carId});
    }

    @Override
    public boolean equal(CarId other) {
        return this.carId == other.carId;
    }

    @Override
    public CarId createInstance() {
        return new CarId();
    }

    @Override
    public int getSize() {
        return Helper.VELKOST_INTEGER + Helper.VELKOST_INTEGER;
    }

    @Override
    public byte[] toByteArray() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            dataStream.writeInt(this.carId);
            dataStream.writeInt(this.adress);

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error tobytearray", e);
        }

    }

    @Override
    public void fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             DataInputStream dataStream = new DataInputStream(byteStream)) {

            this.carId = dataStream.readInt();
            this.adress = dataStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "CId{" +
                "Id=" + carId +
                ", adr=" + adress +
                '}';
    }

    public int getId() {
        return carId;
    }

    public int getAdress() {
        return adress;
    }
}
