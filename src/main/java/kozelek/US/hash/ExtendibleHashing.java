package kozelek.US.hash;

import kozelek.FileManager;
import kozelek.US.heap.HeapFile;
import kozelek.interfaces.IHashableData;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;


public class ExtendibleHashing<T extends IHashableData<T>> extends HeapFile<T> {
    private int globalDepth;
    private ArrayList<Integer> directory;

    public ExtendibleHashing(String filePath, Class<T> recordClass, int maxRecordCount) {
        super(filePath, maxRecordCount, recordClass);

        this.globalDepth = 1;
        this.directory = new ArrayList<>();


        for (int i = 0; i < Math.pow(2, globalDepth); i++) {
            this.directory.add(i);
            HashBlock<T> block = new HashBlock<>(maxRecordCount, recordClass);
            getFileManager().writeBlock(i * block.getSize(), block);
        }
    }

    public int insert(T data) {
        BitSet hash = data.getHash();
        boolean isInserted = false;

        while (!isInserted) {
            int index = this.getIndex(hash);
            int blockIndex = this.directory.get(index);

            HashBlock<T> block = loadBlock(blockIndex);

            if (block.isFull()) {
                if (block.getLocalDepth() == this.globalDepth) {
                    this.doubleDirectory();
                }
                this.split(blockIndex, block);
            } else {
                block.addRecord(data);
                getFileManager().writeBlock(blockIndex * block.getSize(), block);
                isInserted = true;
                return blockIndex;
            }
        }
        return 0;
    }

    private void split(int blockIndex, HashBlock<T> block) {
        int localDepth = block.getLocalDepth();
        block.setLocalDepth(localDepth + 1);

        HashBlock<T> newBlock = new HashBlock<>(getRecordCount(), getRecordClass());
        newBlock.setLocalDepth(localDepth + 1);

        int newBlockIndex = Collections.max(directory) + 1;

        int splitMask = 1 << localDepth;
        for (int i = 0; i < directory.size(); i++) {
            if (directory.get(i) == blockIndex && (i & splitMask) != 0) {
                directory.set(i, newBlockIndex);
            }
        }

        ArrayList<T> records = new ArrayList<>(block.getRecords());
        block.clearRecords();

        for (T record : records) {
            int targetIndex = getIndex(record.getHash());
            int targetBlockIndex = directory.get(targetIndex);

            if (targetBlockIndex == blockIndex) {
                block.addRecord(record);
            } else if (targetBlockIndex == newBlockIndex) {
                newBlock.addRecord(record);
            } else {
                throw new RuntimeException("Chyba pri splite");
            }
        }

        // zapiseme oba bloky
        getFileManager().writeBlock(blockIndex * block.getSize(), block);
        getFileManager().writeBlock(newBlockIndex * block.getSize(), newBlock);
    }

    private void doubleDirectory() {
        int directorySize = directory.size();
        for (int i = 0; i < directorySize; i++) {
            directory.add(directory.get(i)); // Duplicate blocks
        }
        globalDepth++;
    }

    private int getIndex(BitSet hash) {
        long hashValue = hash.toLongArray()[0];
        return (int) (hashValue & ((1 << globalDepth) - 1));
    }

    public HashBlock<T> loadBlock(int blockIndex) {
        HashBlock<T> block = new HashBlock<>(getRecordCount(), getRecordClass());
        byte[] bytes = getFileManager().readBytes(blockIndex * block.getSize(), block.getSize());
        block.fromByteArray(bytes);
        return block;
    }
}
