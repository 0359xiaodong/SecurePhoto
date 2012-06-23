package eu.tpmusielak.securephoto.container;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 21/06/12
 * Time: 18:16
 */
public class SPImageHeader implements Serializable {
    protected long size;
    protected byte[] uniqueFrameID;
    protected byte[] frameHash;

    public SPImageHeader(long size, byte[] frameHash, byte[] uniqueFrameID) {
        this.size = size;
        this.frameHash = frameHash;
        this.uniqueFrameID = uniqueFrameID;
    }

    public static SPImageHeader fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = new ObjectInputStream(byteArrayInput);

        SPImageHeader header = (SPImageHeader) objectInput.readObject();

        objectInput.close();
        byteArrayInput.close();

        return header;
    }

    public static SPImageHeader fromFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);

        long fileLength = file.length();

        byte[] bytes = new byte[(int) fileLength];
        int bytesRead = 0;

        bytesRead = inputStream.read(bytes);

        inputStream.close();

        if (bytesRead != fileLength)
            throw new IOException("Could not read the entire file");

        return fromBytes(bytes);
    }

    public SPImageHeader() {
        this(0, null, null);
    }

    public long getSize() {
        return size;
    }

    public byte[] getFrameHash() {
        return frameHash;
    }

    public byte[] getUniqueFrameID() {
        return uniqueFrameID;
    }
}
