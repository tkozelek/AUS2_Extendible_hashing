package kozelek;

import kozelek.US.heap.Block;
import kozelek.interfaces.IRecord;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileManager<T extends IRecord<T>> {
    private final File file;
    private final RandomAccessFile raf;

    public FileManager(String nazovSuboru) {
        this.file = new File(nazovSuboru);

        try {
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            this.raf = new RandomAccessFile(this.file, "rw");
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeBytes(int adress, byte[] data) throws IOException {
        raf.seek(adress);
        raf.write(data);
    }

    public void writeBlock(int adress, Block<T> block) {
        try {
            this.writeBytes(adress, block.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Chyba pri zapise blocku.", e);
        }
    }

    public long appendBlock(Block<T> block) {
        try {
            long endOfFile = this.file.length();
            raf.seek(endOfFile);
            raf.write(block.toByteArray());
            return endOfFile;
        } catch (IOException e) {
            throw new RuntimeException("Chyba pri appende blocku na koniec suboru.", e);

        }
    }

    public byte[] readBytes(int adress, int byteCount) {
        try {
            raf.seek(adress);
            byte[] bytes = new byte[byteCount];
            raf.read(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Chyba pri citani bytov.", e);
        }
    }

    public void close() {
        try {
            if (this.raf != null) {
                this.raf.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Chyba pri zatvarani suboru", e);
        }
    }

    public long getFileSize() {
        try {
            return raf.length();
        } catch (IOException e) {
            throw new RuntimeException("Chyba getFileSize", e);
        }
    }

    public int getLastBlockIndex(int blockSize) {
        return (int) (getFileSize() / blockSize) - 1;
    }


    public void truncateFile(int bytesToRemove) {
        try {
            long newLength = Math.max(0, raf.length() - bytesToRemove);
            raf.setLength(newLength);
        } catch (IOException e) {
            throw new RuntimeException("Chyba pri skracovaní suboru", e);
        }
    }

//    public void truncateFile(int bytesToRemove) {
//        try {
//            long currentLength = raf.length();
//            long newLength = Math.max(0, currentLength - bytesToRemove);
//
//            if (newLength == 0) {
//
//                raf.setLength(0);
//            } else {
//                byte[] validData = new byte[(int) newLength];
//                raf.seek(0);
//                raf.readFully(validData);
//
//                raf.close();
//                try (RandomAccessFile tempRaf = new RandomAccessFile(file, "rw")) {
//                    tempRaf.setLength(0);
//                    tempRaf.write(validData);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Chyba pri skracovaní suboru", e);
//        }
//    }

    public String getFileName() {
        return this.file.getName();
    }

    public File getFile() {
        return this.file;
    }
}
