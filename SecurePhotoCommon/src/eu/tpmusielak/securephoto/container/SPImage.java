package eu.tpmusielak.securephoto.container;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 01:43
 */
public class SPImage implements Serializable {
    public final static String digestAlgorithm = "SHA-1";
    public final static String defaultExtension = "spi";

    private final byte[] imageData;
    private final byte[] imageDigest;

    public Set<VerificationFactor> getVerificationFactors() {
        return verificationFactors;
    }

    private Set<VerificationFactor> verificationFactors;

    public SPImage(byte[] imageData) {
        byte[] mImageDigest = null;
        this.imageData = imageData;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(VerificationFactor.digestAlgorithm);
            mImageDigest = messageDigest.digest(imageData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        this.imageDigest = mImageDigest;
        verificationFactors = new HashSet<VerificationFactor>();
    }

    public static SPImage fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = new ObjectInputStream(byteArrayInput);


        SPImage image = null;
        try {
            image = (SPImage) objectInput.readObject();
        } catch (ClassNotFoundException ignored) {
        }

        objectInput.close();
        byteArrayInput.close();

        return image;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public byte[] getImageDigest() {
        return imageDigest;
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
