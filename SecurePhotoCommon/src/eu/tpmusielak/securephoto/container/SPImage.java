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
 * Date: 09.02.12
 * Time: 01:43
 */
public final class SPImage implements Serializable {
    private static final long serialVersionUID = -8843183001340004634L;

    public final static String DIGEST_ALGORITHM = "SHA-1";
    public final static String DEFAULT_EXTENSION = "spi";

    private final byte[] imageData;
    private byte[] frameHash;

    private List<Class<Verifier>> verificationFactors;
    private Map<Class<Verifier>, VerificationFactorData> verificationFactorData;


    public class SPImageHeader implements Serializable {
        private final long size;
        private final byte[] frameHash;

        public SPImageHeader(long size, byte[] frameHash) {
            this.size = size;
            this.frameHash = frameHash;
        }

        public long getSize() {
            return size;
        }

        public byte[] getFrameHash() {
            return frameHash;
        }
    }

    private SPImage(byte[] imageData, byte[] inputHash) {
        if (inputHash == null)
            inputHash = new byte[0];

        byte[] mImageHash = null;
        this.imageData = imageData;

        byte[] mFrameHash = Arrays.copyOf(imageData, imageData.length + inputHash.length);
        System.arraycopy(inputHash, 0, mFrameHash, imageData.length, inputHash.length);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            mImageHash = messageDigest.digest(mFrameHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("Digest algorighm %s is not available", DIGEST_ALGORITHM));
        }

        this.frameHash = mImageHash;
        verificationFactors = new ArrayList<Class<Verifier>>();
        verificationFactorData = new HashMap<Class<Verifier>, VerificationFactorData>();
    }

    private SPImage(byte[] imageData) {
        this.imageData = imageData;
        new SPImage(imageData, null);
    }

    public static SPImage getInstance(byte[] imageData) {
        return getInstance(imageData, null);
    }


    public static SPImage getInstance(byte[] imageData, List<Verifier> verifiers, byte[] inputHash) {
        SPImage image = new SPImage(imageData, inputHash);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException ignored) {
            throw new RuntimeException(String.format("Digest algorighm %s is not available", DIGEST_ALGORITHM));
        }

        if (verifiers != null) {
            for (Verifier v : verifiers) {

                Class<Verifier> verifierClass = (Class<Verifier>) v.getClass();
                VerificationFactorData data = v.onCapture(image);

                // Add verification factors and data to file
                image.verificationFactors.add(verifierClass);
                image.verificationFactorData.put(verifierClass, data);

                // Recompute frame hash including the verifier data

                // Update frame hash
                messageDigest.update(image.frameHash);
                messageDigest.update(data.getHash());

                image.frameHash = messageDigest.digest();
            }
        }
        return image;
    }

    @SuppressWarnings("unchecked")
    public static SPImage getInstance(byte[] imageData, List<Verifier> verifiers) {
        return getInstance(imageData, verifiers, null);
    }

    public static SPImage fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = new ObjectInputStream(byteArrayInput);

        // Read and discard the header
        SPImageHeader header = (SPImageHeader) objectInput.readObject();

        objectInput = new ObjectInputStream(byteArrayInput); // OIS workaround
        SPImage image = (SPImage) objectInput.readObject();

        objectInput.close();
        byteArrayInput.close();

        return image;
    }

    public static SPImage fromFile(File file) throws IOException, ClassNotFoundException {
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

    public static byte[] extractImageData(File file) throws ClassNotFoundException, IOException {
        SPImage image = fromFile(file);
        return image.getImageData();
    }

    public byte[] getImageData() {
        return imageData;
    }

    public byte[] getFrameHash() {
        return frameHash;
    }

    public List<Class<Verifier>> getVerificationFactors() {
        return verificationFactors;
    }

    public Map<Class<Verifier>, VerificationFactorData> getVerificationFactorData() {
        return verificationFactorData;
    }

    public byte[] toByteArray() {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutput);

            objectOutput.writeObject(SPImage.this);
            byte[] frameBytes = byteArrayOutput.toByteArray();

            // TODO: frameHash instead of frameHash
            SPImageHeader header = new SPImageHeader(frameBytes.length, frameHash);
            byteArrayOutput.reset();
            objectOutput = new ObjectOutputStream(byteArrayOutput);
            objectOutput.writeObject(header);

            byte[] headerBytes = byteArrayOutput.toByteArray();

            objectOutput.close();
            byteArrayOutput.close();

            bytes = new byte[headerBytes.length + frameBytes.length];
            System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
            System.arraycopy(frameBytes, 0, bytes, headerBytes.length, frameBytes.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
