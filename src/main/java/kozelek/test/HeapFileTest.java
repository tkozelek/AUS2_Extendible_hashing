package kozelek.test;

import kozelek.Helper;
import kozelek.US.HeapFile;
import kozelek.interfaces.IRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HeapFileTest {

    private HeapFile<TestRecord> heap;
    private final int recordCount = 5;
    private Random rand;
    private ArrayList<TestRecord> records;
    private ArrayList<Integer> indexes;

    private final int pocet = 20000;
    private final int pocetDelete = 5000;

    @BeforeEach
    void setUp() {
        String tempFile = "testHeapFile.bin";
        heap = new HeapFile<>(tempFile, recordCount, TestRecord.class);
        rand = new Random();

        this.records = new ArrayList<>(pocet);
        this.indexes = new ArrayList<>(pocet);
    }

    @Test
    void testInsertAndGet() {
        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            int address = heap.insert(record);
            assertTrue(address >= 0);
        }

        for (int i = 0; i < records.size(); i++) {
            TestRecord retrieved = heap.getRecord(indexes.get(i), records.get(i));
            assertNotNull(retrieved, "Mal by existovat");
            assertEquals(records.get(i), retrieved);
        }
    }

    @Test
    void testInsertEditGet() {
        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            int address = heap.insert(record);
            assertTrue(address >= 0);
        }

        for (int i = 0; i < records.size(); i++) {
            TestRecord edited = new TestRecord(i, "Edited " + i);
            int index = rand.nextInt(records.size());
            TestRecord toEdit = records.remove(index);
            int blockIndex = indexes.remove(index);

            heap.edit(blockIndex, toEdit, edited);
            indexes.add(blockIndex);
            records.add(edited);
            TestRecord notFound = heap.getRecord(blockIndex, toEdit);
            assertNull(notFound);
        }

        for (int i = 0; i < records.size(); i++) {
            int index = rand.nextInt(records.size());
            TestRecord edited = records.remove(index);
            int blockIndex = indexes.remove(index);

            TestRecord found = heap.getRecord(blockIndex, edited);
            assertNotNull(found);
            assertTrue(found.equal(edited));
        }
    }

    @Test
    void testInsertAndDelete() {

        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            int index = heap.insert(record);
            records.add(record);
            indexes.add(index);
        }

        for (int i = 0; i < pocetDelete; i++) {
            int index = rand.nextInt(records.size());
            TestRecord record = records.remove(index);
            int indexBlock = indexes.remove(index);

            TestRecord retrieved = heap.getRecord(indexBlock, record);
            assertNotNull(retrieved, "Tu by este mal existovat");

            heap.delete(indexBlock, record);

            retrieved = heap.getRecord(indexBlock, record);
            assertNull(retrieved, "Nemali by existovat");
        }

        for (int i = 0; i < records.size(); i++) {
            TestRecord retrieved = heap.getRecord(indexes.get(i), records.get(i));
            assertNotNull(retrieved, "Mal by existovat");
            assertTrue(records.get(i).equal(retrieved));
        }
    }

    @Test
    void testBlockManagement() {
        int size = heap.getAllBlocks().size();
        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            int index = heap.insert(record);
            records.add(record);
            indexes.add(index);
        }

        for (int i = 0; i < pocetDelete; i++) {
            TestRecord record = records.removeLast();
            int indexBlock = indexes.removeLast();
            heap.delete(indexBlock, record);
        }

        int afterSize = heap.getAllBlocks().size();
        assertTrue(afterSize > size);
    }

    @Test
    void testRandomOperations() {
        int init = 0;
        int numberOfRandomOperations = 4;
        for (int i = 0; i < init; i++) {
            TestRecord test = new TestRecord(i, "Record " + i);
            int index = heap.insert(test);
            this.records.add(test);
            this.indexes.add(index);
        }

        TestRecord lastInserted = null;

        for (int j = init; j < pocet + init; j++) {
            int operation = rand.nextInt(numberOfRandomOperations);
            switch (operation) {
                case 0: // insert
                    TestRecord newEntity = new TestRecord(j, "Record " + j);
                    int i = heap.insert(newEntity);

                    lastInserted = newEntity;
                    this.records.add(newEntity);
                    this.indexes.add(i);

                    continue;
                case 1: // delete
                    if (!records.isEmpty()) {
                        int index = rand.nextInt(this.records.size());
                        TestRecord toDelete = this.records.remove(index);
                        int indexBlock = indexes.remove(index);
                        TestRecord found = heap.getRecord(indexBlock, toDelete);
                        assertTrue(found.equal(toDelete));
                        heap.delete(indexBlock, toDelete);
                        found = heap.getRecord(indexBlock, toDelete);
                        assertNull(found, "Test == deleted");
                    } else {
                        j--;
                    }
                    continue;
                case 2: // edit
                    if (!records.isEmpty()) {
                        TestRecord edited = new TestRecord(j + 10000, "edited " + j);

                        int index = rand.nextInt(this.records.size());
                        TestRecord toEdit = this.records.remove(index);
                        int indexBlock = indexes.remove(index);

                        heap.edit(indexBlock, toEdit, edited);

                        this.records.add(edited);
                        this.indexes.add(indexBlock);

                        TestRecord notFound = null;
                        try {
                            notFound = this.heap.getRecord(indexBlock, toEdit);
                        } catch (RuntimeException e) {
                            System.out.println(edited);
                            System.out.printf("%d %s", indexBlock, toEdit);
                        }
                        TestRecord found = this.heap.getRecord(indexBlock, edited);

                        assertNull(notFound);
                        assertNotNull(found, " " + index + " " + indexBlock);
                        assertTrue(found.equal(edited));
                    } else {
                        j--;
                    }
                    continue;
                case 3:
                    if (!records.isEmpty()) {
                        int index = rand.nextInt(this.records.size());
                        TestRecord toFind = this.records.get(index);
                        int indexBlock = indexes.get(index);

                        TestRecord found = this.heap.getRecord(indexBlock, toFind);
                        assertNotNull(found);
                        assertTrue(found.equal(toFind), "Test == found");
                    } else {
                        j--;
                    }
                    continue;
            }
        }
    }

    public static class TestRecord implements IRecord<TestRecord> {
        private int id;
        private String data;
        private final int MAX_DATA = 15;
        public TestRecord(int id, String data) {
            this.id = id;
            this.data = data;
        }

        public TestRecord() {
        }

        @Override
        public byte[] toByteArray() {
            try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
                DataOutputStream dataStream = new DataOutputStream(byteStream);

                dataStream.writeInt(id);

                Helper.writeString(dataStream, data, MAX_DATA);

                return byteStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void fromByteArray(byte[] bytes) {
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)) {
                DataInputStream dataStream = new DataInputStream(byteStream);

                this.id = dataStream.readInt();
                this.data = Helper.readString(dataStream, MAX_DATA);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getSize() {
            return Helper.VELKOST_INTEGER + Helper.VELKOST_BYTE + MAX_DATA;
        }

        @Override
        public boolean equal(TestRecord other) {
            return this.id == other.id;
        }

        @Override
        public TestRecord createInstance() {
            return null;
        }

        @Override
        public String toString() {
            return "TestRecord{" +
                    "data='" + data + '\'' +
                    ", id=" + id +
                    '}';
        }
    }
}
