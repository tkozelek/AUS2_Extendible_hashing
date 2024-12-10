package kozelek.test;

import kozelek.Helper;
import kozelek.US.hash.ExtendibleHashing;
import kozelek.interfaces.IHashableData;
import kozelek.interfaces.IRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ExtendibleHashingTest {

    private ExtendibleHashing<TestRecord> ext;
    private final int recordCount = 5;
    private Random rand;
    private ArrayList<TestRecord> records;
    private ArrayList<Integer> indexes;
    private final int pocet = 5000;
    private final int pocetDelete = 500;

    @BeforeEach
    void setUp() {
        String tempFile = "testExtendibleFile.bin";

        File file = new File(tempFile);
        if (file.exists()) {
            file.delete();
        }

        ext = new ExtendibleHashing<>(tempFile, TestRecord.class, recordCount);
        rand = new Random();

        this.records = new ArrayList<>(pocet);
    }

    @Test
    void testInsertAndGet() {
        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            ext.insert(record);
            this.records.add(record);
        }

        for (int i = 0; i < records.size(); i++) {
            TestRecord retrieved = ext.getRecord(records.get(i));
            assertNotNull(retrieved, "Mal by existovat");
            assertTrue(records.get(i).equal(retrieved));
        }
    }

    @Test
    void testInsertEditGet() {
        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            int address = ext.insert(record);
            assertTrue(address >= 0);
        }

        for (int i = 0; i < records.size(); i++) {
            TestRecord edited = new TestRecord(i, "Edited " + i);
            int index = rand.nextInt(records.size());
            TestRecord toEdit = records.remove(index);
            int blockIndex = indexes.remove(index);

            ext.edit(blockIndex, toEdit, edited);
            indexes.add(blockIndex);
            records.add(edited);
            TestRecord notFound = ext.getRecord(blockIndex, toEdit);
            assertNull(notFound);
        }

        for (int i = 0; i < records.size(); i++) {
            int index = rand.nextInt(records.size());
            TestRecord edited = records.remove(index);
            int blockIndex = indexes.remove(index);

            TestRecord found = ext.getRecord(blockIndex, edited);
            assertNotNull(found);
            assertTrue(found.equal(edited));
        }
    }

    @Test
    void testInsertAndDelete() {
        for (int i = 0; i < pocet; i++) {
            TestRecord record = new TestRecord(i, "Record " + i);
            ext.insert(record);
            records.add(record);
        }

        for (int i = 0; i < pocetDelete; i++) {
            int index = rand.nextInt(records.size());
            TestRecord record = records.remove(index);

            TestRecord retrieved = ext.getRecord(record);
            assertNotNull(retrieved, "Tu by este mal existovat");

            ext.delete(record);

            retrieved = ext.getRecord(record);
            assertNull(retrieved, "Nemali by existovat");
        }

        for (int i = 0; i < records.size(); i++) {
            TestRecord retrieved = ext.getRecord(records.get(i));
            assertNotNull(retrieved, "Mal by existovat");
            assertTrue(records.get(i).equal(retrieved));
        }
    }

    @Test
    void testRandomOperations() {
        int init = 0;
        int numberOfRandomOperations = 3;
        for (int i = 0; i < init; i++) {
            TestRecord test = new TestRecord(i, "Record " + i);
            ext.insert(test);
            this.records.add(test);
        }


        for (int j = init; j < pocet + init; j++) {
            int operation = rand.nextInt(numberOfRandomOperations);
            switch (operation) {
                case 0: // insert
                    TestRecord newEntity = new TestRecord(j, "Record " + j);
                    ext.insert(newEntity);
                    this.records.add(newEntity);
                    continue;
                case 1: // delete
                    if (!records.isEmpty()) {
                        int index = rand.nextInt(this.records.size());
                        TestRecord toDelete = this.records.remove(index);

                        TestRecord found = ext.getRecord(toDelete);
                        if (found == null) {
                            System.out.println(j);
                            System.out.println(toDelete);
                            ext.printAll();
                        }

                        assertTrue(found.equal(toDelete));

                        ext.delete(toDelete);


                        found = ext.getRecord(toDelete);
                        assertNull(found, "Test == deleted");
                    } else {
                        j--;
                    }
                    continue;
//                case 2: // edit
//                    if (!records.isEmpty()) {
//                        TestRecord edited = new TestRecord(j + 10000, "edited " + j);
//
//                        int index = rand.nextInt(this.records.size());
//                        TestRecord toEdit = this.records.remove(index);
//                        int indexBlock = indexes.remove(index);
//
//                        ext.edit(indexBlock, toEdit, edited);
//
//                        this.records.add(edited);
//                        this.indexes.add(indexBlock);
//
//                        TestRecord notFound = null;
//                        try {
//                            notFound = this.ext.getRecord(indexBlock, toEdit);
//                        } catch (RuntimeException e) {
//                            System.out.println(edited);
//                            System.out.printf("%d %s", indexBlock, toEdit);
//                        }
//                        TestRecord found = this.ext.getRecord(indexBlock, edited);
//
//                        assertNull(notFound);
//                        assertNotNull(found, " " + index + " " + indexBlock);
//                        assertTrue(found.equal(edited));
//                    } else {
//                        j--;
//                    }
//                    continue;
                case 2:
                    if (!records.isEmpty()) {
                        int index = rand.nextInt(this.records.size());
                        TestRecord toFind = this.records.get(index);

                        TestRecord found = this.ext.getRecord(toFind);
                        assertNotNull(found);
                        assertTrue(found.equal(toFind), "Test == found");
                    } else {
                        j--;
                    }
                    continue;
            }
        }
    }

    public static class TestRecord implements IRecord<TestRecord>, IHashableData<TestRecord> {
        private int id;
        private String data;
        private final int MAX_DATA = 15;
        public TestRecord(int id, String data) {
            this.id = id;
            this.data = data;
        }

        public TestRecord() {
        }

        public int getId() {
            return id;
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

        @Override
        public BitSet getHash() {
            if (id == 0) {
                BitSet bitSet = new BitSet(1);
                bitSet.set(0);
                return bitSet;
            }

            return BitSet.valueOf(new long[] {id});
        }
    }
}
