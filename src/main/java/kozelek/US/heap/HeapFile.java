package kozelek.US.heap;

import kozelek.FileManager;
import kozelek.interfaces.IRecord;

import java.io.*;
import java.util.ArrayList;

public class HeapFile<T extends IRecord<T>> {
    private int maxRecordCount;
    private final Class<T> recordClass;
    private int indexEmptyBlock;
    private int indexPartialBlock;

    private final FileManager<T> fileManager;
    private Block<T> block;

    public HeapFile(String fileName, Class<T> recordClass, int recordCount) {
        this.maxRecordCount = recordCount;
        this.recordClass = recordClass;
        this.fileManager = new FileManager<>(fileName);

        this.indexEmptyBlock = -1;
        this.indexPartialBlock = -1;

        this.loadState();

        if (maxRecordCount != -1)
            this.block = new Block<>(maxRecordCount, recordClass);
        else
            this.block = new Block<>(recordCount, recordClass);

    }

    public int insert(T record) {
        int index = getIndexOfNextInsert();
        int address = index * block.getSize();

        if (index == -1) {
            return handleNewBlockInsertion(record);
        }
        block = this.loadBlock(index);
        boolean wasEmpty = block.getValidCount() == 0;

        if (block.addRecord(record)) {
            if (wasEmpty && block.isFull()) {
                handleInsertionIntoEmptyBlockBecomesFull(index);
            } else if (wasEmpty) {
                handleInsertionIntoEmptyBlock(index);
            } else if (block.isFull()) {
                // ak sa z neho stane plny, bol partial
                handleFullBlockFromPartial(index);
            }

            this.fileManager.writeBlock(address, block);
            return index;
        } else {
            throw new RuntimeException("Record nebol vložený");
        }
    }

    public void delete(int index, T record) {
        block = this.loadBlock(index);
        int countBeforeDelete = block.getValidCount();

        if (block.removeRecord(record)) {
            int countAfterDelete = block.getValidCount();

            // ak bol partial aj zostal, neriešime nič
            if (countBeforeDelete != block.getMaxRecordCount() && countAfterDelete != 0) {
                this.fileManager.writeBlock(index * block.getSize(), block);
                return;
            }

            // bol plny, je prazdny
            // blok. faktor 1
            if (countBeforeDelete == block.getMaxRecordCount() && countAfterDelete == 0) {
                this.becomesEmptyFromFull(index);
            } else if (countAfterDelete == 0) {
                // blok je prazdny
                this.becomesEmptyBlockFromPartial(index);

                // Check if this is the last block
                if (index == this.fileManager.getLastBlockIndex(block.getSize())) {
                    this.truncateEmptyBlocks();
                    return;
                }
            } else if (countBeforeDelete == block.getMaxRecordCount()) {
                this.becomesPartialFromFull(index);
            }

            this.fileManager.writeBlock(index * block.getSize(), block);
        }
    }


    /**
     * Block je prazdny po vložení sa stane plnym
     * @param index block do ktorého sa vklada
     */
    private void handleInsertionIntoEmptyBlockBecomesFull(int index) {
        if (block.getNext() != -1) {
            Block<T> blockNext = this.loadBlock(block.getNext());
            blockNext.setPrev(-1);
            this.fileManager.writeBlock(index * block.getSize(), blockNext);

            indexEmptyBlock = block.getNext();
            block.setNext(-1);
        } else {
            indexEmptyBlock = -1;
        }
    }

    public int getIndexOfNextInsert() {
        int position = -1;
        if (indexPartialBlock != -1) {
            position = indexPartialBlock;
        } else if (indexEmptyBlock != -1) {
            position = indexEmptyBlock;
        }
        return position;
    }

    /**
     * Ak je neexistuje partial alebo prazdny, vytvorí sa nový block
     * vloží sa na koniec heapfile
     * @param record vkladany zaznam
     * @return int - index vkladaného bloku
     */
    private int handleNewBlockInsertion(T record) {
        Block<T> newBlock = new Block<>(maxRecordCount, recordClass);
        newBlock.addRecord(record);
        int newAddress = (int) (this.fileManager.appendBlock(newBlock) / block.getSize());

        // partial aj empty musia byt prazdne
        if (newBlock.getValidCount() != newBlock.getMaxRecordCount()) {
            indexPartialBlock = newAddress;
        }
        return newAddress;
    }

    /**
     * Ak vkladam do prazdneho bloku
     * @param blockIndex index na block do ktoreho vkladam
     */
    private void handleInsertionIntoEmptyBlock(int blockIndex) {
        if (block.getNext() != -1) {
            insertionIntoEmptyBlockIfBlockHasNext();
            // 2 pristupy
        } else {
            this.indexEmptyBlock = -1;
        }
        // ak neni plny nastavíme ho na partial
        if (block.getValidCount() != block.getMaxRecordCount() && indexPartialBlock != -1)
            updatePartialBlockChain(blockIndex);
        // 2 zapisy
        else
            indexPartialBlock = blockIndex;
    }


    private void insertionIntoEmptyBlockIfBlockHasNext() {
        // block ma dalsi
        // posun dalsieho na indexemptyblock
        Block<T> blockNext = this.loadBlock(block.getNext());
        blockNext.setPrev(-1);
        this.fileManager.writeBlock(block.getNext() * block.getSize(), blockNext);
        this.indexEmptyBlock = block.getNext();
        block.setNext(-1);
    }

    /**
     * Vkladame do empty blocku, stava sa z neho partial
     * aktualne existuje nejaky partial
     * nastavi aktualny block na vrchol partial
     * @param blockIndex - index blocku do ktorého vkladame (stava sa z neho partial)
     */
    private void updatePartialBlockChain(int blockIndex) {
        Block<T> currentPartial = this.loadBlock(indexPartialBlock);
        currentPartial.setPrev(blockIndex);
        this.fileManager.writeBlock(indexPartialBlock * block.getSize(), currentPartial);

        block.setNext(indexPartialBlock);
        block.setPrev(-1);
        indexPartialBlock = blockIndex;
        this.fileManager.writeBlock(blockIndex * block.getSize(), block);
    }

    /**
     * Block sa naplnil, nastav next ako partial
     * Ak nie je next, nastav partial na -1
     * Basically zmen partialblock na next
     */
    private void handleFullBlockFromPartial(int index) {
        if (block.getNext() != -1) {
            Block<T> blockNext = this.loadBlock(block.getNext());
            blockNext.setPrev(-1);

            this.fileManager.writeBlock(block.getNext() * block.getSize(), blockNext);

            this.indexPartialBlock = block.getNext();
            block.setNext(-1);

            this.fileManager.writeBlock(index * block.getSize(), block);
        } else {
            this.indexPartialBlock = -1;
        }
    }

    /**
     * ak je blokovaci faktor 1
     * vymaze sa a stava sa prazdny
     * @param index block z ktoreho sa vymazava
     */
    private void becomesEmptyFromFull(int index) {
        Block<T> blockPrev = this.loadBlock(indexEmptyBlock);
        blockPrev.setPrev(index);
        this.fileManager.writeBlock(indexEmptyBlock * block.getSize(), blockPrev);

        block.setNext(indexEmptyBlock);
        indexEmptyBlock = index;
    }

    /**
     * Block bol plny, ale vymazaním sa stáva partial
     * nastavime ho na indexpartial
     * aktualny index posunieme dalej
     * @param index block z ktoreho vymazavame record
     */
    private void becomesPartialFromFull(int index) {
        if (indexPartialBlock != -1) {
            Block<T> curPartial = this.loadBlock(indexPartialBlock);
            curPartial.setPrev(index);
            this.fileManager.writeBlock(indexPartialBlock * block.getSize(), curPartial);

            block.setNext(indexPartialBlock);
        }
        indexPartialBlock = index;
    }

    /**
     * Po vymazaní ostane blok prázdny, nastavime na emptyblock, posunieme ostatné
     * ak sa nachádza v partial, musíme spojiť prev s next blockom
     * STANE SA Z NEHO PRAZDNY BLOK
     * Ak má prev aj next, prev.next sa nastavi na next a next.prev sa nastavi na prev
     */
    private void becomesEmptyBlockFromPartial(int index) {
        boolean result = fixReferences(false);
        if (!result) {
            // nemá ani next ani prev
            // čiže musí byť partial index
            this.indexPartialBlock = -1;
        }
        if (indexEmptyBlock != -1) {
            // nastavime ho na index empty
            // pridame do zretazenia prazdnych
            Block<T> curEmptyBlock = this.loadBlock(indexEmptyBlock);
            curEmptyBlock.setPrev(index);
            this.fileManager.writeBlock(indexEmptyBlock * block.getSize(), curEmptyBlock);

            block.setNext(indexEmptyBlock);
        } else {
            block.setNext(-1);
        }
        block.setPrev(-1);

        this.indexEmptyBlock = index;
    }

    /**
     * Metoda načíta block na indexe
     * @param index index kde sa nachádza block v súbore
     * @return Block<T>, načítaný block
     */
    public Block<T> loadBlock(int index) {
        byte[] bytes = this.fileManager.readBytes(index * block.getSize(), block.getSize());
        Block<T> loadedBlock = new Block<>(maxRecordCount, recordClass);
        loadedBlock.fromByteArray(bytes);
        return loadedBlock;
    }

    public void truncateEmptyBlocks() {
        long fileSize = this.fileManager.getFileSize();
        int size = block.getSize();
        int lastBlockIndex = (int) (fileSize / size) - 1;

        while (lastBlockIndex >= 0) {
            // nacitaj posledny blok

            // ak je prazadny
            if (block.getValidCount() == 0) {
                // ak ma prepojenie na pred. empty block, nastav mu next na -1
                boolean result = fixReferences(true);
                if (!result) {
                    indexEmptyBlock = -1;
                }

                this.fileManager.truncateFile(size);
                lastBlockIndex--;
                if (lastBlockIndex >= 0) {
                    block = loadBlock(lastBlockIndex);
                }
            } else {
                break;
            }
        }
    }

    public long fileSize() {
        return this.fileManager.getFileSize();
    }

    public boolean fixReferences(boolean truncate) {
        boolean result = false;
        if (block.getPrev() != -1 && block.getNext() != -1) {
            // je v medzi dvoma partial blokmi
            Block<T> blockPrev = this.loadBlock(block.getPrev());
            Block<T> blockNext = this.loadBlock(block.getNext());
            // nacitame oba bloky
            blockPrev.setNext(block.getNext());
            blockNext.setPrev(block.getPrev());
            this.fileManager.writeBlock(block.getPrev() * block.getSize(), blockPrev);
            this.fileManager.writeBlock(block.getNext() * block.getSize(), blockNext);
            block.setPrev(-1);
            block.setNext(-1);
            result = true;
        } else if (block.getPrev() != -1 && block.getNext() == -1) {
            // ma prev block ale nema next, je na konci
            Block<T> blockPrev = this.loadBlock(block.getPrev());
            blockPrev.setNext(-1);
            this.fileManager.writeBlock(block.getPrev() * block.getSize(), blockPrev);
            block.setPrev(-1);
            result = true;
        } else if (block.getNext() != -1 && block.getPrev() == -1) {
            // nema prev block, čiže je na začiatku partial
            // ale ma next, takže next dalsi partial
            Block<T> blockNext = this.loadBlock(block.getNext());
            blockNext.setPrev(-1);
            if (!truncate)
                this.indexPartialBlock = block.getNext();
            else {
                this.indexEmptyBlock = block.getNext();
            }
            this.fileManager.writeBlock(block.getNext() * block.getSize(), blockNext);
            block.setNext(-1);
            result = true;
        }
        return result;
    }

    public T getRecord(int index, T record) {
        Block<T> tempBlock = this.loadBlock(index);
        return tempBlock.getRecord(record);
    }

    public Block<T> getBlock(int index) {
        return this.loadBlock(index);
    }

    public void edit(int index, T oldRecord, T newRecord) {
        block = this.loadBlock(index);
        block.edit(oldRecord, newRecord);
        this.fileManager.writeBlock(index * block.getSize(), block);
    }

    public ArrayList<Block<T>> getAllBlocks() {
        ArrayList<Block<T>> blocks = new ArrayList<>();
        long fileSize = fileManager.getFileSize();

        int numberOfBlocks = (int) (fileSize / block.getSize());

        for (int i = 0; i < numberOfBlocks; i++) {
            // precitaj block na indexe
            Block<T> tempBlock = this.loadBlock(i);
            blocks.add(tempBlock);
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
        System.out.println(block.getSize());
        System.out.println(blocks.size());
        System.out.println(this.fileManager.getFileSize());
        System.out.println(this.fileManager.getFileSize() / block.getSize());
    }

    public int getMaxRecordCount() {
        return maxRecordCount;
    }

    public int getCountOfAllRecords() {
        ArrayList<Block<T>> blocks = this.getAllBlocks();
        int sum = 0;
        for (Block<T> block : blocks) {
            sum += block.getValidCount();
        }
        return sum;
    }

    public void closeFileManager() {
        this.fileManager.close();
    }

    @Override
    public String toString() {
        return "HeapFile{" +
                "indexEmptyBlock=" + indexEmptyBlock +
                ", indexPartialyEmptyBlock=" + indexPartialBlock +
                '}';
    }

    public boolean emptyExists() {
        return this.indexEmptyBlock != -1;
    }

    public boolean partialExists() {
        return this.indexPartialBlock != -1;
    }

    public void saveState() {
        File exportsDir = new File("exports");
        if (!exportsDir.exists()) {
            try {
                exportsDir.mkdir();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        File file = new File("exports/" + this.fileManager.getFileName() + "state");

        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(indexEmptyBlock + "\n");
            writer.write(indexPartialBlock + "\n");
            writer.write(maxRecordCount + "\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadState() {
        File file = new File("exports/" + this.fileManager.getFileName() + "state");

        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            this.indexEmptyBlock = Integer.parseInt(reader.readLine());
            this.indexPartialBlock = Integer.parseInt(reader.readLine());
            this.maxRecordCount = Integer.parseInt(reader.readLine());
            this.block = new Block<>(maxRecordCount, this.recordClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<T> getRecordClass() {
        return recordClass;
    }

    public int getIndexEmptyBlock() {
        return indexEmptyBlock;
    }

    public int getIndexPartialBlock() {
        return indexPartialBlock;
    }

    public FileManager<T> getFileManager() {
        return fileManager;
    }

    public Block<T> getBlock() {
        return block;
    }

    public void update(int index, Block<T> block) {
        this.fileManager.writeBlock(index * block.getSize(), block);
    }
}
