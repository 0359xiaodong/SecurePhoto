package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.Verifier;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 01:43
 */
public class SPImage implements Serializable {
    private static final long serialVersionUID = -8843183001340004634L;

    public final static String digestAlgorithm = "SHA-1";
    public final static String defaultExtension = "spi";

    private final byte[] imageData;
    private final byte[] imageHash;

    private List<Class<Verifier>> verificationFactors;
    private Map<Class<Verifier>, VerificationFactorData> verificationFactorData;

    private SPImage(byte[] imageData) {
        byte[] mImageHash = null;
        this.imageData = imageData;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
            mImageHash = messageDigest.digest(imageData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        this.imageHash = mImageHash;
        verificationFactors = new ArrayList<Class<Verifier>>();
        verificationFactorData = new HashMap<Class<Verifier>, VerificationFactorData>();
    }

    public static SPImage getInstance(byte[] imageData) {
        return getInstance(imageData, null);
    }

    public static SPImage getInstance(byte[] imageData, List<Verifier> verifiers) {
        SPImage image = new SPImage(imageData);

        if (verifiers != null) {
            for (Verifier v : verifiers) {

                Class<Verifier> verifierClass = (Class<Verifier>) v.getClass();
                VerificationFactorData data = v.onCapture(image);

                image.verificationFactors.add(verifierClass);
                image.verificationFactorData.put(verifierClass, data);
            }
        }
        return image;
    }

    public static SPImage fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = new ObjectInputStream(byteArrayInput);

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

    public byte[] getImageHash() {
        return imageHash;
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
            bytes = byteArrayOutput.toByteArray();

            objectOutput.close();
            byteArrayOutput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
