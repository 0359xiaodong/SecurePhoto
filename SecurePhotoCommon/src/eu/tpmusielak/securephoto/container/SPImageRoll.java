package eu.tpmusielak.securephoto.container;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 15.02.12
 * Time: 11:53
 */
public final class SPImageRoll implements Serializable {
    private static final long serialVersionUID = 10;

    public static final long NO_EXPIRY = -1;

    public final static String DIGEST_ALGORITHM = "SHA-1";
    public static final String DEFAULT_EXTENSION = "spr";
    public static final int DIGEST_LENGTH = 20;

    protected class Header implements Serializable {
        protected final long expiryDate;
        protected final byte[] uniqueID;
        protected int frameCount;
        protected byte[] currentHash;

        public Header(byte[] uniqueID, long expiryDate) {
            this.expiryDate = expiryDate;

            this.uniqueID = uniqueID;
            frameCount = 0;
            currentHash = Arrays.copyOf(uniqueID, uniqueID.length);
        }
    }

    private Header header;
    private File rollFile;


    public SPImageRoll(File file) {
        Random random = new Random();
        byte[] uniqueID = new byte[DIGEST_LENGTH];
        random.nextBytes(uniqueID);

        new SPImageRoll(file, uniqueID, NO_EXPIRY);
    }


    public SPImageRoll(File file, byte[] uniqueID) {
        new SPImageRoll(file, uniqueID, NO_EXPIRY);
    }

    public SPImageRoll(File file, long expiryDate) {
        Random random = new Random();
        byte[] uniqueID = new byte[DIGEST_LENGTH];
        random.nextBytes(uniqueID);

        new SPImageRoll(file, uniqueID, expiryDate);
    }

    public SPImageRoll(File file, byte[] uniqueID, long expiryDate) {
        rollFile = file;
        header = new Header(uniqueID, expiryDate);

        writeHeader();
    }

    // Constructor for reading SPImageRoll from file
    private SPImageRoll(Header header, File file) {
        this.header = header;
        rollFile = file;
    }

    public static SPImageRoll fromFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);
        long fileLength = file.length();

        ObjectInput objectInput = new ObjectInputStream(inputStream);
        Header header = (Header) objectInput.readObject();

        inputStream.close();
        objectInput.close();

        return new SPImageRoll(header, file);
    }

    private void writeHeader() {
        writeImage(null);
    }

    private void writeImage(SPImage image) {
        try {
            RandomAccessFile file = new RandomAccessFile(rollFile, "rw");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(header);

            byte[] headerBytes = byteArrayOutputStream.toByteArray();

            byteArrayOutputStream.close();
            objectOutput.close();

            file.seek(0); // Go to beginning
            file.write(headerBytes); // Update header

            // If writing image
            if (image != null) {
                file.seek(file.length()); // Go to EOF
                file.write(image.toByteArray()); // Write new image
            }

            file.close();

            // TODO: add exception handling
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addImage(SPImage image) {
        header.currentHash = image.getImageHash();
        header.frameCount++;
        writeImage(image);
    }


    public int getFrameCount() {
        return header.frameCount;
    }

    public SPImage getFrame(int index) {
        SPImage frame = null;

        if (index > (getFrameCount() - 1) || index < 0)
            throw new IndexOutOfBoundsException();

        try {
            FileInputStream fileInputStream = new FileInputStream(rollFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            Header rollHeader = (Header) objectInputStream.readObject(); // Read and discard the header
            long skipped;

            // Now the pointer is at the first frame header

            SPImage.SPImageHeader frameHeader = null;

            for (int i = 0; i < index; i++) {
                objectInputStream = new ObjectInputStream(fileInputStream);
                frameHeader = (SPImage.SPImageHeader) objectInputStream.readObject(); // Read the SPImageHeader

                long frameSize = frameHeader.getSize();
                skipped = fileInputStream.skip(frameSize); // Skip the frame

                if (skipped != frameSize)
                    throw new IOException("Could not skip the frame");
            }

            // Now we are at the header of the right frame

            objectInputStream = new ObjectInputStream(fileInputStream);
            frameHeader = (SPImage.SPImageHeader) objectInputStream.readObject(); // Reading and discarding the frame header
            objectInputStream = new ObjectInputStream(fileInputStream);
            frame = (SPImage) objectInputStream.readObject(); // Reading the frame;

            // TODO: Exception handling
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return frame;
    }


    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(Integer.toHexString(aByte & 0xFF).toUpperCase());
        }

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SPImageRoll \n");
        sb.append(String.format("\n"));

        Date date = new Date(header.expiryDate);

        sb.append(String.format("Unique ID: %s\n", byteArrayToHex(header.uniqueID)));
        sb.append(String.format("Expiry date: %s\n", date.toString()));
        sb.append(String.format("Images stored: %d\n", header.frameCount));
        sb.append(String.format("Current hash: %s\n", byteArrayToHex(header.currentHash)));

        return sb.toString();
    }


}
