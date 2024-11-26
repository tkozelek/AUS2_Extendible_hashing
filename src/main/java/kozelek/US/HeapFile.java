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
        Block<T> tempBlock = new Block<>(recordCount, recordClass);
        // nemame prazdny ani volny blok
        // vytvorime novy, vlozime
        if (adress == -1) {
            Block<T> newBlock = new Block<>(recordCount, recordClass);
            newBlock.addRecord(record);
            // zapiseme na koniec suboru
            int adresa = (int) (this.fileManager.appendBlock(newBlock) / block.getSize()); // vloženie
            // ak nemáme partial block
            // už máme
            if (indexPartialyEmptyBlock == -1 && newBlock.getValidCount() != newBlock.getMaxRecordCount()) {
                indexPartialyEmptyBlock = adresa;
            }
            return adresa;
        }

        // inak načitame blok
        byte[] bytes = this.fileManager.readBytes(adress, block.getSize()); // citanie
        block.fromByteArray(bytes);
        block.addRecord(record);

        int wasEmptyIndex = adress / block.getSize();
        if (wasEmptyIndex == this.indexEmptyBlock) { // ak vkladame do toho čo bol ako prazdny index
            // ak bol prazdny a ma dalsi prazdny
            // dalsi posunieme na indexemptyblock
            if (block.getNext() != -1) {
                bytes = this.fileManager.readBytes(block.getNext() * block.getSize(), block.getSize()); // precitame next
                tempBlock.fromByteArray(bytes);
                tempBlock.setPrev(-1);
                this.fileManager.writeBlock(block.getNext() * block.getSize(), tempBlock);

                this.indexEmptyBlock = block.getNext();
                block.setNext(-1);
            } else {
                this.indexEmptyBlock = -1;
            }

            this.changePartialIndex(wasEmptyIndex);


        } else if (block.getValidCount() == block.getMaxRecordCount()) { // ak sa naplnil
            if (block.getNext() != -1) {
                this.indexPartialyEmptyBlock = block.getNext();
                block.setNext(-1);
                // musime nacitat nasledovnika a upravit mu prev index na -1, posun na prve miesto
                // -1 _ 100 _ 200 _ 300 _ -1
                // -1 _ 200 _ 300 _ -1
                bytes = this.fileManager.readBytes(this.indexPartialyEmptyBlock * block.getSize(), block.getSize()); // citanie
                tempBlock.fromByteArray(bytes);
                tempBlock.setPrev(-1);
                this.fileManager.writeBlock(this.indexPartialyEmptyBlock * block.getSize(), tempBlock); // zapis upraveneho
            } else {
                this.indexPartialyEmptyBlock = -1;
            }
        }
        this.fileManager.writeBlock(adress, block); // zapisanie
        return adress / block.getSize();
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

    public void changePartialIndex(int beforeIndex) {
        if (indexPartialyEmptyBlock == -1 && block.getValidCount() != block.getMaxRecordCount()) {
            indexPartialyEmptyBlock = beforeIndex;
        } else if (indexPartialyEmptyBlock != -1 && block.getValidCount() != block.getMaxRecordCount()) {
            Block<T> tempBlock = new Block<>(recordCount, recordClass);
            byte[] bytes = this.fileManager.readBytes(indexPartialyEmptyBlock * block.getSize(), block.getSize());
            tempBlock.fromByteArray(bytes);
            // aktualnemu partial nastavíme predchodzieho doteraz prazdnemu bloku
            tempBlock.setPrev(beforeIndex);

            block.setNext(indexPartialyEmptyBlock); // aktualnemu nastavime ako dalsi pred. partial
            block.setPrev(-1);

            this.fileManager.writeBlock(indexPartialyEmptyBlock * block.getSize(), tempBlock);

            indexPartialyEmptyBlock = beforeIndex;
        }
    }

    public void changeEmptyIndex(int beforeIndex) {
        if (indexEmptyBlock == -1) {
            indexEmptyBlock = beforeIndex;
        } else if (indexEmptyBlock != -1) {
            Block<T> tempBlock = new Block<>(recordCount, recordClass);
            byte[] bytes = this.fileManager.readBytes(indexEmptyBlock * block.getSize(), block.getSize());
            tempBlock.fromByteArray(bytes);
            // aktualnemu partial nastavíme predchodzieho doteraz prazdnemu bloku
            tempBlock.setPrev(beforeIndex);

            block.setNext(indexEmptyBlock); // aktualnemu nastavime ako dalsi pred. partial
            block.setPrev(-1);
            this.fileManager.writeBlock(indexEmptyBlock * block.getSize(), tempBlock);

            indexEmptyBlock = beforeIndex;
        }
    }

    public void delete(int index, T record) {
        int adress = index * block.getSize();
        byte[] bytes = this.fileManager.readBytes(adress, block.getSize());
        block.fromByteArray(bytes);
        int beforeDelete = block.getValidCount();
        int afterDelete = -1;

        // vymaze sa, ak sa tam nachadza ofc
        if (block.removeRecord(record)) {
            afterDelete = block.getValidCount();

            // ak bol poloprazdny a zostal poloprazdny
            // zapiseme zmenu a nerisime
            if (beforeDelete != block.getMaxRecordCount() && afterDelete != 0) {
                this.fileManager.writeBlock(adress, block);
                return;
            }

            // ak je po zmazaní prazdny
            if (block.getValidCount() == 0) {
                this.changeEmptyIndex(index);
                this.truncateEmptyBlocks();
            } else {
                this.changePartialIndex(index);
            }

            // Write the modified block back to the file
            this.fileManager.writeBlock(adress, block);
        }
    }

    public void truncateEmptyBlocks() {
        long fileSize = this.fileManager.getFileSize();
        int size = block.getSize();
        int lastBlockIndex = (int) (fileSize / size) - 1;
        Block<T> lastBlock = new Block<>(recordCount, recordClass);

        while (lastBlockIndex >= 0) {
            // nacitaj posledny blok
            byte[] bytes = this.fileManager.readBytes(lastBlockIndex * size, size);
            lastBlock.fromByteArray(bytes);

            // ak je prazadny
            if (lastBlock.getValidCount() == 0) {
                // ak ma prepojenie na pred. empty block, nastav mu next na -1
                if (lastBlock.getPrev() != -1) {
                    byte[] prevBytes = this.fileManager.readBytes(lastBlock.getPrev() * size, size);
                    Block<T> prevBlock = new Block<>(recordCount, recordClass);
                    prevBlock.fromByteArray(prevBytes);
                    prevBlock.setNext(-1);
                    this.fileManager.writeBlock(lastBlock.getPrev() * size, prevBlock);
                }

                if (lastBlockIndex == indexEmptyBlock) {
                    indexEmptyBlock = -1;
                }

                this.fileManager.truncateFile(size);
                lastBlockIndex--;
            } else {
                break;
            }
        }
    }

    public T getRecord(int index, T record) {
        int adress = index * block.getSize();
        Block<T> tempBlock = new Block<>(recordCount, recordClass);
        byte[] bytes = this.fileManager.readBytes(adress, block.getSize());
        tempBlock.fromByteArray(bytes);
        return tempBlock.getRecord(record);
    }

    public Block<T> getBlock(int index, T record) {
        int adress = index * block.getSize();
        Block<T> tempBlock = new Block<>(recordCount, recordClass);
        byte[] bytes = this.fileManager.readBytes(adress, block.getSize());
        tempBlock.fromByteArray(bytes);

        if (tempBlock.contains(record))
            return tempBlock;
        else
            return null;
    }

    public void edit(int index, T oldRecord, T newRecord) {
        int adress = index * block.getSize();
        byte[] bytes = this.fileManager.readBytes(adress, block.getSize());
        block.fromByteArray(bytes);
        block.edit(oldRecord, newRecord);
    }

    public ArrayList<Block<T>> getAllBlocks() {
        ArrayList<Block<T>> blocks = new ArrayList<>();
        long fileSize = fileManager.getFileSize();

        int numberOfBlocks = (int) (fileSize / block.getSize());

        for (int i = 0; i < numberOfBlocks; i++) {
            // precitaj block na indexe
            byte[] blockData = this.fileManager.readBytes(i * block.getSize(), block.getSize());
            Block<T> block = new Block<>(recordCount, recordClass);
            block.fromByteArray(blockData);
            blocks.add(block);
        }

        return blocks;
    }

    public void printAll() {
        int index = 0;
        ArrayList<Block<T>> blocks = this.getAllBlocks();
        for (Block<T> block : blocks) {
            System.out.print(index + " ");
            System.out.println(block);
            index++;
        }
    }

    public void closeFileManager() {
        this.fileManager.close();
    }

    @Override
    public String toString() {
        return "HeapFile{" +
                "indexEmptyBlock=" + indexEmptyBlock +
                ", indexPartialyEmptyBlock=" + indexPartialyEmptyBlock +
                '}';
    }
}
