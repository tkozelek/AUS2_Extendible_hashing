package kozelek.US.hash;

import kozelek.US.heap.HeapFile;
import kozelek.interfaces.IHashableData;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.stream.Collectors;


public class ExtendibleHashing<T extends IHashableData<T>> extends HeapFile<T> {
    private int globalDepth;
    private ArrayList<Integer> directory;
    private HashBlock<T> block;
    private ArrayList<Integer> freeBlocks;

    public ExtendibleHashing(String filePath, Class<T> recordClass, int maxRecordCount) {
        super(filePath, maxRecordCount, recordClass);

        this.globalDepth = 1;
        this.directory = new ArrayList<>();
        this.freeBlocks = new ArrayList<>();

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

            block = loadBlock(blockIndex);

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

        HashBlock<T> newBlock = new HashBlock<>(getMaxRecordCount(), getRecordClass());
        newBlock.setLocalDepth(localDepth + 1);
        int newBlockIndex;
        if (!freeBlocks.isEmpty()) {
            Collections.sort(freeBlocks, Collections.reverseOrder());
            newBlockIndex = freeBlocks.removeLast();
        } else {
            newBlockIndex = Collections.max(directory) + 1;
        }

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

    private void cleanDirectory() {
        int maxIndex = (int) Math.pow(2, globalDepth) - 1; // max index
        for (int i = directory.size() - 1; i >= 0; i--) {
            if (directory.get(i) > maxIndex) {
                directory.remove(i);
            }
        }
    }

    public T getRecord(T data) {
        BitSet hash = data.getHash();
        int index = getIndex(hash);
        int blockIndex = this.directory.get(index);
        block = loadBlock(blockIndex);
        return block.getRecord(data);
    }

    public HashBlock<T> getBlock(T data) {
        BitSet hash = data.getHash();
        int index = getIndex(hash);
        int blockIndex = this.directory.get(index);
        block = loadBlock(blockIndex);
        return block;
    }

    private void doubleDirectory() {
        int directorySize = directory.size();
        for (int i = 0; i < directorySize; i++) {
            directory.add(directory.get(i)); // Duplicate blocks
        }
        globalDepth++;
    }

    // pridat kontrolu, localdepth == localdepth
    public void delete(T data) {
        boolean isDeleted = false;

        int index = getIndex(data.getHash());
        int blockIndex = this.directory.get(index);
        this.block = loadBlock(blockIndex);

        if (!block.removeRecord(data)) {
            printAll();
            throw new RuntimeException("zaznam nebol najdeny.");
        }

        while (!isDeleted) {
            if (block.getValidCount() == block.getMaxRecordCount())
                break;
            int neIndex = getNeighborIndex(index, block.getLocalDepth());
            int toIndex = canMergeWithNeighbor(neIndex, block, blockIndex);
            if (toIndex == -1) {
                // ak sa nemohol spojit
                getFileManager().writeBlock(blockIndex * block.getSize(), block);
                isDeleted = true;
                continue;
            }
            // je spojeny
            index = toIndex;
            blockIndex = directory.get(index);
            block = loadBlock(blockIndex);
        }
    }

    private int canMergeWithNeighbor(int neIndex, HashBlock<T> block, int blockIndex) {
        if (neIndex != -1 && neIndex < directory.size()) {
            int neBlockIndex = directory.get(neIndex);
            HashBlock<T> neBlock = loadBlock(neBlockIndex);
            // load neighbor

            if (neBlock.getLocalDepth() == block.getLocalDepth()) {
                int sum = neBlock.getValidCount() + block.getValidCount();
                if (sum <= this.getMaxRecordCount()) {
                    HashBlock<T> toBlock = blockIndex < neBlockIndex ? block : neBlock;
                    HashBlock<T> fromBlock = blockIndex < neBlockIndex ? neBlock : block;
                    int toIndex = Math.min(blockIndex, neBlockIndex);
                    int fromIndex = Math.max(blockIndex, neBlockIndex);
                    for (T record : fromBlock.getRecords()) {
                        toBlock.addRecord(record);
                    }
                    fromBlock.clearRecords();
                    freeBlocks.add(fromIndex);

                    // zmenime pointre (indexy)
                    for (int i = 0; i < directory.size(); i++) {
                        if (directory.get(i) == fromIndex) {
                            directory.set(i, toIndex);
                        }
                    }

                    toBlock.setLocalDepth(toBlock.getLocalDepth() - 1);
                    getFileManager().writeBlock(toIndex * toBlock.getSize(), toBlock);
                    if (isLastBlockWithDepth(toBlock.getLocalDepth() + 1)) {
                        halfDirectory();
                    }

                    return toIndex;
                }
            }
        }
        return -1; // nemoze sa spojit
    }

    private boolean isLastBlockWithDepth(int depth) {
        ArrayList<Integer> checked = new ArrayList<>();
        for (int blockIndex : directory) {
            if (!checked.contains(blockIndex)) {
                HashBlock<T> block = loadBlock(blockIndex);
                if (block.getLocalDepth() == depth) {
                    return false;
                }
                checked.add(blockIndex);
            }
        }
        return true;
    }

    private void halfDirectory() {
        int halfSize = directory.size() / 2;

        ArrayList<Integer> newDirectory = new ArrayList<>();

        for (int i = 0; i < halfSize; i++) {
            newDirectory.add(directory.get(i));
        }

        directory.clear();
        directory.addAll(newDirectory);

        globalDepth--;


        for (int i = 0; i < directory.size(); i++) {
            int blockIndex = directory.get(i);

            directory.set(i, blockIndex % (1 << globalDepth));
        }
    }

    public ArrayList<HashBlock<T>> getAllHashBlocks() {
        ArrayList<HashBlock<T>> blocks = new ArrayList<>();
        ArrayList<Integer> checked = new ArrayList<>();

        for (int i : directory) {
            if (!checked.contains(i)) {
                HashBlock<T> tempBlock = this.loadBlock(i);
                blocks.add(tempBlock);
                checked.add(i);
            }
        }

        return blocks;
    }

    // 0001
    // gd = 3
    // 1000 - 1
    // 0111
    // AND 0001
    public int getIndex(BitSet hash) {
        long hashValue = hash.toLongArray()[0];
        int gd = (int) Math.pow(2, globalDepth) - 1;
        return (int) (hashValue & gd);
    }

    // index 1
    // 001
    // ld = 3
    // 100 <<
    //____
    // 101
    public int getNeighborIndex(int index, int localDepth) {
        if (localDepth <= 1)
            return -1;
        int ld = (int) Math.pow(2, localDepth - 1);
        return index ^ ld;
    }

    public HashBlock<T> loadBlock(int blockIndex) {
        HashBlock<T> block = new HashBlock<>(getMaxRecordCount(), getRecordClass());
        byte[] bytes = getFileManager().readBytes(blockIndex * block.getSize(), block.getSize());
        block.fromByteArray(bytes);
        return block;
    }

    public void printAll() {
        System.out.println();
        ArrayList<Integer> checked = new ArrayList<>();

        int x = 0;
        for (int i : directory) {
            if (!checked.contains(i)) {
                HashBlock<T> tempBlock = this.loadBlock(i);
                System.out.printf("ind %d: %d %s\n", x, i, tempBlock);
                checked.add(i);
            } else {
                System.out.printf("%d: -> %d\n", x, i);
            }
            x++;
        }
    }

    public void printDirectory() {
        int x = 0;
        for (int i : directory) {
            System.out.printf("%d %d\n", x, i);
            ++x;
        }
    }

    @Override
    public String toString() {
        return "ExtendibleHashing{" +
                "directory=" + directory +
                ", globalDepth=" + globalDepth +
                '}';
    }
}
