package kozelek.US.hash;

import kozelek.US.heap.HeapFile;
import kozelek.interfaces.IHashableData;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;


public class ExtendibleHashing<T extends IHashableData<T>> extends HeapFile<T> {
    private int globalDepth;
    private ArrayList<Integer> directory;
    private HashBlock<T> block;
    private ArrayList<Integer> freeBlocks;
    private ArrayList<Integer> localDepths;

    public ExtendibleHashing(String filePath, Class<T> recordClass, int clusterSize) {
        super(filePath, recordClass, clusterSize);
        if (directory == null || directory.isEmpty()) {
            this.globalDepth = 1;
            this.freeBlocks = new ArrayList<>();
            localDepths = new ArrayList<>();
            directory = new ArrayList<>();
            for (int i = 0; i < Math.pow(2, globalDepth); i++) {
                this.directory.add(i);
                this.localDepths.add(1);
                this.block = new HashBlock<>(clusterSize, recordClass);
                getFileManager().writeBlock(i * block.getClusterSize(), block);
            }
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
                if (localDepths.get(index) == this.globalDepth) {
                    this.doubleDirectory();
                }
                this.split(index, blockIndex, block);
            } else {
                if (!block.addRecord(data)) {
                    throw new RuntimeException("error inserting");
                }
                getFileManager().writeBlock(blockIndex * block.getClusterSize(), block);
                isInserted = true;
                return blockIndex;
            }
        }

        return 0;
    }

    private void split(int index, int blockIndex, HashBlock<T> block) {
        int localDepth = localDepths.get(index);

        for (int i = 0; i < directory.size(); i++) {
            if (directory.get(i) == blockIndex) {
                int ld = localDepths.get(i);
                localDepths.set(i, ld + 1);
            }
        }

        HashBlock<T> newBlock = new HashBlock<>(getClusterSize(), getRecordClass());

        int newBlockIndex;
        if (!freeBlocks.isEmpty()) {
            Collections.sort(freeBlocks, Collections.reverseOrder());
            newBlockIndex = freeBlocks.removeLast();
        } else {
            newBlockIndex = Collections.max(directory) + 1;
        }

        int split = 1 << localDepth;
        for (int i = 0; i < directory.size(); i++) {
            if (directory.get(i) == blockIndex && (i & split) != 0) {
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
        getFileManager().writeBlock(blockIndex * block.getClusterSize(), block);
        getFileManager().writeBlock(newBlockIndex * block.getClusterSize(), newBlock);
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
            directory.add(directory.get(i));
            localDepths.add(localDepths.get(i));
        }
        globalDepth++;
    }

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

            int neIndex = getNeighborIndex(index, localDepths.get(index));
            int toIndex = canMergeWithNeighbor(index, neIndex, block, localDepths.get(index), blockIndex);

            if (toIndex == -1) {
                // ak sa nemohol spojit
                getFileManager().writeBlock(blockIndex * block.getClusterSize(), block);
                isDeleted = true;
                continue;
            }
            // je spojeny
            index = toIndex;
            blockIndex = directory.get(index);
            block = loadBlock(blockIndex);
        }
    }

    private int canMergeWithNeighbor(int index, int neIndex, HashBlock<T> block, int localDepth, int blockIndex) {
        if (neIndex != -1 && neIndex < directory.size()) {
            int neBlockIndex = directory.get(neIndex);

            if (neBlockIndex == blockIndex)
                return -1;

            HashBlock<T> neBlock = loadBlock(neBlockIndex);
            // load neighbor

            if (localDepths.get(neIndex) == localDepth) {
                int sum = neBlock.getValidCount() + block.getValidCount();
                if (sum <= this.block.getMaxRecordCount()) {
                    HashBlock<T> toBlock = blockIndex < neBlockIndex ? block : neBlock;
                    HashBlock<T> fromBlock = blockIndex < neBlockIndex ? neBlock : block;
                    int toBlockIndex = Math.min(blockIndex, neBlockIndex);
                    int fromBlockIndex = Math.max(blockIndex, neBlockIndex);

                    int toIndex = Math.min(index, neIndex);

                    for (T record : fromBlock.getRecords()) {
                        toBlock.addRecord(record);
                    }

                    fromBlock.clearRecords();
                    freeBlocks.add(fromBlockIndex);

                    // zmenime pointre (indexy)
                    for (int i = 0; i < directory.size(); i++) {
                        if (directory.get(i) == fromBlockIndex || directory.get(i) == toBlockIndex) {
                            directory.set(i, toBlockIndex);
                            localDepths.set(i, localDepth - 1);
                        }
                    }

                    cleanFreeBlocks();

                    localDepths.set(toIndex, localDepth - 1);

                    getFileManager().writeBlock(toBlockIndex * toBlock.getClusterSize(), toBlock);

                    if (isLastBlockWithDepth(globalDepth)) {
                        toIndex = halfDirectory(toBlockIndex);
                    }

                    setFileSize();

                    return toIndex;
                }
            }
        }
        return -1; // nemoze sa spojit
    }

    private boolean isLastBlockWithDepth(int depth) {
        for (int ld : localDepths) {
            if (ld == depth) {
                return false;
            }
        }
        return true;
    }

    private int halfDirectory(int toIndex) {
        int halfSize = directory.size() / 2;
        int toReturn = 0;

        ArrayList<Integer> newDirectory = new ArrayList<>();
        ArrayList<Integer> newLocalDepths = new ArrayList<>();

        for (int i = 0; i < halfSize; i++) {
            newDirectory.add(directory.get(i));
            newLocalDepths.add(localDepths.get(i));
            if (directory.get(i) == toIndex) {
                toReturn = i;
            }
        }

        directory.clear();
        directory.addAll(newDirectory);
        localDepths.clear();
        localDepths.addAll(newLocalDepths);

        globalDepth--;
        return toReturn;
    }

    public void edit(T oldRecord, T newRecord) {
        // rovnaky kluc, len vymenime data
        int index = getIndex(oldRecord.getHash());
        int blockIndex = this.directory.get(index);
        if (oldRecord.equal(newRecord)) {
            this.block = getBlock(oldRecord);
            this.block.edit(oldRecord, newRecord);
            getFileManager().writeBlock(blockIndex * block.getClusterSize(), block);
        } else {
            this.delete(oldRecord);
            this.insert(newRecord);
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

    public ArrayList<String> getAllHashBlocksSequence() {
        ArrayList<String> blocks = new ArrayList<>();
        int maxIndex = Collections.max(directory);
        for (int i = 0; i <= maxIndex; i++) {
            int blockIndex = directory.get(i);
            HashBlock<T> block = this.loadBlock(blockIndex);
            blocks.add(block.toString());
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
        HashBlock<T> block = new HashBlock<>(this.getClusterSize(), getRecordClass());
        byte[] bytes = getFileManager().readBytes(blockIndex * block.getClusterSize(), block.getClusterSize());
        block.fromByteArray(bytes);
        return block;
    }

    public void printAll() {
        System.out.println(this.toString());
        ArrayList<Integer> checked = new ArrayList<>();

        int x = 0;
        for (int i : directory) {
            if (!checked.contains(i)) {
                HashBlock<T> tempBlock = this.loadBlock(i);
                System.out.printf("%d: %d %d %s\n", x, i, localDepths.get(x), tempBlock);
                checked.add(i);
            } else {
                System.out.printf("%d: -> %d\n", x, i);
            }
            x++;
        }
    }

    public ArrayList<String> toStringDirectory() {
        ArrayList<Integer> checked = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        int x = 0;
        for (int i : directory) {
            if (!checked.contains(i)) {
                HashBlock<T> tempBlock = this.loadBlock(i);
                result.add(String.format("%d: %d  %s\n", x, i, tempBlock));
                checked.add(i);
            } else {
                result.add(String.format("%d: -> %d\n", x, i));
            }
            x++;
        }
        return result;
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

        File file = new File("exports/" + getFileManager().getFileName() + "state");

        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(globalDepth + "\n");
            writer.write(getClusterSize() + "\n");

            for (int i = 0; i < freeBlocks.size(); i++) {
                if (i == freeBlocks.size() - 1) {
                    writer.write(freeBlocks.get(i) + "");
                } else {
                    writer.write(freeBlocks.get(i) + ",");
                }
            }
            writer.write("\n");
            for (int i = 0; i < directory.size(); i++) {
                if (i == directory.size() - 1) {
                    writer.write(directory.get(i) + "");
                } else {
                    writer.write(directory.get(i) + ",");
                }
            }
            writer.write("\n");
            for (int i = 0; i < localDepths.size(); i++) {
                if (i == localDepths.size() - 1) {
                    writer.write(localDepths.get(i) + "");
                } else {
                    writer.write(localDepths.get(i) + ",");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadState() {
        File file = new File("exports/" + getFileManager().getFileName() + "state");

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            this.globalDepth = Integer.parseInt(reader.readLine());
            this.setClusterSize(Integer.parseInt(reader.readLine()));
            this.directory = new ArrayList<>();
            this.freeBlocks = new ArrayList<>();
            this.localDepths = new ArrayList<>();
            String[] line = reader.readLine().split(",");
            for (String s : line) {
                if (!s.isEmpty())
                    freeBlocks.add(Integer.parseInt(s));
            }
            line = reader.readLine().split(",");
            for (String s : line) {
                if (!s.isEmpty())
                    directory.add(Integer.parseInt(s));
            }

            line = reader.readLine().split(",");
            for (String s : line) {
                if (!s.isEmpty())
                    localDepths.add(Integer.parseInt(s));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ExtendibleHashing{" +
                "GD=" + globalDepth +
                ", dir=" + directory +
                ", freeBlocks=" + freeBlocks +
                ", localDepths=" + localDepths +
                ", clusterSize=" + getClusterSize() +
                '}';
    }

    public void printFreeBlocks() {
        System.out.println(freeBlocks);
    }

    public void setFileSize() {
        int index = Collections.max(directory);
        // nastavime filesize na index * cluster
        int fileSize = (int) (getFileManager().getFileSize() - (index + 1) * getClusterSize());
        getFileManager().truncateFile(fileSize);
    }

    private void cleanFreeBlocks() {
        int maxIndex = Collections.max(directory);
        freeBlocks.removeIf(blockIndex -> blockIndex > maxIndex);
    }
}
