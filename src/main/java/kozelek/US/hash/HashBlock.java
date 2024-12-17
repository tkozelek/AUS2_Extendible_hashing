package kozelek.US.hash;

import kozelek.Helper;
import kozelek.US.heap.Block;
import kozelek.interfaces.IByteData;
import kozelek.interfaces.IHashableData;

import java.io.*;
import java.util.ArrayList;

public class HashBlock<T extends IHashableData<T>> extends Block<T> {
    private int localDepth;
    private final int blockDataSize = 2 * Helper.VELKOST_INTEGER;

    public HashBlock(int clusterSize, Class<T> recordClass) {
        super(clusterSize, recordClass);
        this.localDepth = 1;
    }

    public int getLocalDepth() {
        return localDepth;
    }

    public void setLocalDepth(int localDepth) {
        this.localDepth = localDepth;
    }

    public int getMaxRecordCount() {
        int cluster = getClusterSize();
        int size = this.getInstance().getSize();

        return (cluster - blockDataSize) / size;
    }

    @Override
    public int getSize() {
        // localDepth + pocetrecordov + (maxpocetrecordov * carId/carECV * size())
        // carid - 4 + 4 + (3*8)
        return Helper.VELKOST_INTEGER + Helper.VELKOST_INTEGER + (this.getMaxRecordCount() * this.getInstance().getSize());
    }

    @Override
    public byte[] toByteArray() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            dataStream.writeInt(localDepth);

            // recordy
            dataStream.writeInt(this.getValidCount());

            for (int i = 0; i < this.getMaxRecordCount(); i++) {
                if (i < this.getValidCount()) {
                    dataStream.write(this.getRecords().get(i).toByteArray());
                } else {
                    dataStream.write(new byte[this.getInstance().getSize()]);
                }
            }

            int currentSize = byteStream.size();
            if (currentSize < getClusterSize()) {
                dataStream.write(new byte[getClusterSize() - currentSize]);
            }

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             DataInputStream dataStream = new DataInputStream(byteStream)) {

            this.localDepth = dataStream.readInt();

            // pocet platnych
            this.setValidCount(dataStream.readInt());

            ArrayList<T> records = new ArrayList<>();
            for (int i = 0; i < this.getMaxRecordCount(); i++) {
                byte[] recordBytes = new byte[this.getInstance().getSize()];
                dataStream.readFully(recordBytes);

                if (i < this.getValidCount()) {
                    T record = createInstance();
                    record.fromByteArray(recordBytes);
                    records.add(record);
                }
            }
            this.setRecords(records);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "HB{" +
                "LD=" + localDepth +
                ", MRC=" + getMaxRecordCount() +
                ", " + getRecords();
    }
}
