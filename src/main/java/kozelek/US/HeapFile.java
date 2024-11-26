package kozelek.US;

import kozelek.FileManager;
import kozelek.interfaces.IRecord;

import java.io.File;
import java.util.ArrayList;

public class HeapFile<T extends IRecord<T>> {
    private int recordCount;
    private Class<T> recordClass;
    private int indexEmptyBlock;
    private int indexPartialyEmptyBlock;

    private FileManager<T> fileManager;
    private Block<T> block;

    public HeapFile(String fileName, int recordCount, Class<T> recordClass) {
        this.recordCount = recordCount;
        this.recordClass = recordClass;
        this.fileManager = new FileManager<>(fileName);

        this.block = new Block<>(recordCount, recordClass);

        this.indexEmptyBlock = -1;
        this.indexPartialyEmptyBlock = -1;
    }

    public int insert(T record) {
        int adress = getAdressOfNextInsert();
        // nemame prazdny ani volny blok
        // vytvorime novy, vlozime
        if (adress == -1) {
            Block<T> newBlock = new Block<>(recordCount, recordClass);
            newBlock.addRecord(record);
            // zapiseme na koniec suboru
            int adresa = (int) (this.fileManager.appendBlock(newBlock) / block.getSize());
            // ak nemáme partial block
            // už máme
            if (indexPartialyEmptyBlock == -1 && newBlock.getValidCount() != newBlock.getMaxRecordCount()) {
                indexPartialyEmptyBlock = adresa;
            }
            return adresa;
        }

        // inak načitame blok
        byte[] bytes = this.fileManager.readBytes(adress, block.getSize());
        block.fromByteArray(bytes);
        block.addRecord(record);

        // ak sa naplní po vložení
        if (block.getValidCount() == block.getMaxRecordCount()) {
            if (block.getNext() != -1) {
                this.indexPartialyEmptyBlock = block.getNext();
                block.setNext(-1);
                // musime nacitat nasledovnika a upravit mu prev index na -1, posun na prve miesto
                // -1 _ 100 _ 200 _ 300 _ -1
                // -1 _ 200 _ 300 _ -1
                bytes = this.fileManager.readBytes(this.indexPartialyEmptyBlock * block.getSize(), block.getSize());
                block.fromByteArray(bytes);
                block.setPrev(-1);
            } else {
                this.indexPartialyEmptyBlock = -1;
            }
        }
        this.fileManager.writeBlock(adress, block);
        return adress;
    }


    public int getAdressOfNextInsert() {
        int position = -1;
        if (indexPartialyEmptyBlock != -1) {
            position = indexPartialyEmptyBlock * block.getSize();
        } else if (indexEmptyBlock != -1) {
            position = indexEmptyBlock * block.getSize();
        }
        return position;
    }

    public ArrayList<Block<T>> getAllBlocks() {
        ArrayList<Block<T>> blocks = new ArrayList<>();
        long fileSize;
        try {
            fileSize = fileManager.getFileSize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file size", e);
        }

        int numberOfBlocks = (int) (fileSize / block.getSize());

        for (int i = 0; i < numberOfBlocks; i++) {
            // precitaj block na indexe
            byte[] blockData = this.fileManager.readBytes(i * block.getSize(), block.getSize());
            Block<T> block = new Block<>(recordCount, recordClass);
            block.fromByteArray(blockData);
            blocks.add(block);
        }

        this.fileManager.close();
        return blocks;
    }

    @Override
    public String toString() {
        return "HeapFile{" +
                "indexEmptyBlock=" + indexEmptyBlock +
                ", indexPartialyEmptyBlock=" + indexPartialyEmptyBlock +
                '}';
    }
}
