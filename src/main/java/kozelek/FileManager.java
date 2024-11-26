package kozelek;

import kozelek.US.Block;
import kozelek.interfaces.IRecord;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileManager<T extends IRecord<T>> {
    private File file;
    private RandomAccessFile raf;

    public FileManager(String nazovSuboru) {
        this.file = new File(nazovSuboru);

        try {
            // vytvori subor ak neexistuje
            if (!this.file.exists()) {
                this.file.createNewFile();
            }

            this.raf = new RandomAccessFile(this.file, "rw");
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeBytes(long position, byte[] data) throws IOException {
        raf.seek(position);
        raf.write(data);
    }

    public void writeBlock(long position, Block<T> block) throws IOException {
        this.writeBytes(position, block.toByteArray());
    }

    public byte[] readBytes(long position, int length) throws IOException {
        raf.seek(position);
        byte[] bytes = new byte[length];
        raf.read(bytes);
        return bytes;
    }

    public void close() throws IOException {
        if (this.raf != null) {
            this.raf.close();
        }
    }

    public long getFileSize() throws IOException {
        return raf.length();
    }

}
