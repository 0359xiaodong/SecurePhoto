package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.Verifier;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 15.02.12
 * Time: 11:53
 */
public final class SPImageRoll implements Serializable, SPIFile {
    private static final long serialVersionUID = 10;

    public static final long NO_EXPIRY = -1;

    public final static String DIGEST_ALGORITHM = "SHA-1";
    public static final String DEFAULT_EXTENSION = "spr";
    public static final int DIGEST_LENGTH = 20;

    public class Header implements Serializable {
        private final long expiryDate;
        private final byte[] uniqueID;
        private int frameCount;
        private byte[] currentHash;

        public Header(byte[] uniqueID, long expiryDate) {
            this.expiryDate = expiryDate;

            this.uniqueID = uniqueID;
            frameCount = 0;
            currentHash = Arrays.copyOf(uniqueID, uniqueID.length);
        }

        public long getExpiryDate() {
            return expiryDate;
        }

        public byte[] getUniqueID() {
            return uniqueID;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public byte[] getCurrentHash() {
            return currentHash;
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

    public int addImage(SPImage image) {
        header.currentHash = image.getFrameHash();
        header.frameCount++;
        writeImage(image);
        return header.frameCount - 1;
    }


    public int getFrameCount() {
        return header.frameCount;
    }

    public byte[] getCurrentHash() {
        return header.currentHash;
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

            SPImageHeader frameHeader = null;

            for (int i = 0; i < index; i++) {
                objectInputStream = new ObjectInputStream(fileInputStream); // KLUDGE
                frameHeader = (SPImageHeader) objectInputStream.readObject(); // Read the SPImageHeader

                long frameSize = frameHeader.getSize();
                skipped = fileInputStream.skip(frameSize); // Skip the frame

                if (skipped != frameSize)
                    throw new IOException("Could not skip the frame");
            }

            // Now we are at the header of the right frame

            objectInputStream = new ObjectInputStream(fileInputStream); // KLUDGE
            frameHeader = (SPImageHeader) objectInputStream.readObject(); // Reading and discarding the frame header
            objectInputStream = new ObjectInputStream(fileInputStream); // KLUDGE
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

    public boolean checkIntegrity() {
        if (header.frameCount == 0)
            return true;

        byte[] calculatedHash = header.uniqueID;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SPImage.DIGEST_ALGORITHM);

            for (int i = 0; i < header.frameCount; i++) {
                SPImage frame = getFrame(i);

                byte[] imageData = frame.getImageData();
                messageDigest.update(imageData);
                messageDigest.update(calculatedHash);

                calculatedHash = messageDigest.digest();

                List<Class<Verifier>> verificationFactors = frame.getVerificationFactors();
                Map<Class<Verifier>, VerificationFactorData> verificationFactorData = frame.getVerificationFactorData();

                for (Class<Verifier> verifierClass : verificationFactors) {
                    VerificationFactorData factorData = verificationFactorData.get(verifierClass);

                    if (factorData != null) {
                        messageDigest.update(calculatedHash);
                        messageDigest.update(factorData.getHash());

                        calculatedHash = messageDigest.digest();
                    }

                }
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return Arrays.equals(header.currentHash, calculatedHash);
    }


    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(Integer.toHexString(aByte & 0xFF).toUpperCase());
        }

        return sb.toString();
    }

    public Header getHeader() {
        return header;
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
