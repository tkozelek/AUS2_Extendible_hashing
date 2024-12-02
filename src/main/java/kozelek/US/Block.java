package kozelek.US;

import kozelek.Helper;
import kozelek.interfaces.IByteData;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

public class Block<T extends IRecord<T>> implements IByteData {
    private ArrayList<T> records;
    private int validCount; // pocet platnych zaznamov
    private final int maxRecordCount; // maximalny pocet zaznamov v bloku
    private int next; // adresa dalsieho volneho bloku
    private int prev; // adresa pred. volneho bloku

    private final Class<T> recordClass;
    private final T instance;

    public Block(int maxRecordCount, Class<T> recordClass) {
        this.maxRecordCount = maxRecordCount;
        this.validCount = 0;
        this.next = -1; // -1 znamená koniec reťazca
        this.prev = -1; // -1 znamená začiatok reťazca
        this.records = new ArrayList<>();
        this.recordClass = recordClass;
        this.instance = createInstance();
    }

    private T createInstance() {
        try {
            return recordClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public int getPrev() {
        return prev;
    }

    public void setPrev(int prev) {
        this.prev = prev;
    }

    public int getValidCount() {
        return validCount;
    }

    public int getMaxRecordCount() {
        return maxRecordCount;
    }

    public void edit(T oldRecord, T newRecord) {
        for (int i = 0; i < validCount; i++) {
            if (records.get(i).equal(oldRecord)) {
                records.set(i, newRecord);
                return;
            }
        }
    }

    public T getRecord(T record) {
        for (int i = 0; i < validCount; i++) {
            if (record.equal(records.get(i))) {
                return records.get(i);
            }
        }
        return null;
    }

    public boolean contains(T record) {
        for (int i = 0; i < validCount; i++) {
            if (record.equal(records.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean addRecord(T record) {
        if (validCount < maxRecordCount) {
            this.records.add(record);
            this.validCount++;
            return true;
        } else {
            return false;
        }
    }

    public boolean removeRecord(T record) {
        for (int i = 0; i < validCount; i++) {
            if (record.equal(records.get(i))) {
                // ak nie je na konci
                // vymenim s poslednym v arrayliste
                if (i < validCount - 1) {
                    Collections.swap(records, i, validCount - 1);
                }

                // odstanime posledny record
                records.remove(validCount - 1);
                validCount--;

                return true; // vratime vymazany record
            }
        }
        // neexistuje
        return false;
    }

    @Override
    public int getSize() {
        // pocet platnych (byte?) + zaznam * 5 + prev + next
        return Helper.VELKOST_INTEGER + (maxRecordCount * this.instance.getSize()) + Helper.VELKOST_INTEGER + Helper.VELKOST_INTEGER;
    }

    @Override
    public byte[] toByteArray() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            // recordy
            dataStream.writeInt(this.validCount);
            for (int i = 0; i < this.maxRecordCount; i++) {
                if (i < this.validCount) {
                    dataStream.write(this.records.get(i).toByteArray());
                } else {
                    dataStream.write(new byte[this.instance.getSize()]);
                }
            }

            // pointre na dalsi blok
            dataStream.writeInt(this.prev);
            dataStream.writeInt(this.next);

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             DataInputStream dataStream = new DataInputStream(byteStream)) {

            // pocet platnych
            this.validCount = dataStream.readInt();

            this.records = new ArrayList<>();
            for (int i = 0; i < this.maxRecordCount; i++) {
                byte[] recordBytes = new byte[this.instance.getSize()];
                dataStream.readFully(recordBytes);

                if (i < this.validCount) {
                    T record = createInstance();
                    record.fromByteArray(recordBytes);
                    this.records.add(record);
                }
            }

            // adresy blokov
            this.prev = dataStream.readInt();
            this.next = dataStream.readInt();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Block{" +
                "records=" + records +
                ", validCount=" + validCount +
                ", maxRecordCount=" + maxRecordCount +
                ", next=" + next +
                ", prev=" + prev;
    }

    public boolean isFull() {
        return validCount == maxRecordCount;
    }
}
