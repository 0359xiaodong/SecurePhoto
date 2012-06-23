package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.Verifier;

import javax.xml.bind.DatatypeConverter;
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
public final class SPImage implements Serializable, SPIFile {
    private static final long serialVersionUID = -8843183001340004635L;

    public final static String DIGEST_ALGORITHM = "SHA-1";
    public final static int ID_LENGTH = 20;
    public final static String DEFAULT_EXTENSION = "spi";

    private final byte[] imageData;

    private List<Class<Verifier>> verificationFactors;
    private Map<Class<Verifier>, VerificationFactorData> verificationFactorData;

    private SPImageHeader header;

    public SPImageHeader getHeader() {
        return header;
    }

    private SPImage(byte[] imageData, byte[] inputHash) {
        header = new SPImageHeader();

        if (inputHash == null)
            inputHash = new byte[0];

        //Generate unique frame ID
        Random random = new Random();
        byte[] idBytes = new byte[20];
        random.nextBytes(idBytes);
        header.uniqueFrameID = idBytes;

        byte[] mImageHash;
        this.imageData = imageData;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            messageDigest.update(imageData);
            messageDigest.update(inputHash);
            mImageHash = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("Digest algorighm %s is not available", DIGEST_ALGORITHM));
        }

        header.frameHash = mImageHash;
        verificationFactors = new ArrayList<Class<Verifier>>();
        verificationFactorData = new HashMap<Class<Verifier>, VerificationFactorData>();
    }

    private SPImage(byte[] imageData) {
        this(imageData, null);
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
                messageDigest.update(image.header.frameHash);
                messageDigest.update(data.getHash());

                image.header.frameHash = messageDigest.digest();
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
        return header.frameHash;
    }

    public byte[] getUniqueFrameID() {
        return header.uniqueFrameID;
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

            header.size = frameBytes.length;

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

    public VerificationStatus checkIntegrity(SPValidator validator) {
        return checkIntegrity(validator, null);
    }

    public VerificationStatus checkIntegrity(SPValidator validator, byte[] inputHash) {
        byte[] calculatedHash = null;
        if (inputHash == null) {
            validator.log("No input hash");
            inputHash = new byte[0];
        }

        validator.log(String.format("Frame ID: %s", byteArrayToHex(header.uniqueFrameID)));


        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SPImage.DIGEST_ALGORITHM);
            messageDigest.update(imageData);
            messageDigest.update(inputHash);
            calculatedHash = messageDigest.digest();


            if (verificationFactors != null) {
                validator.log("VerificationFactorData found");
                for (Class<Verifier> v : verificationFactors) {
                    VerificationFactorData factorData = verificationFactorData.get(v);

                    if (factorData != null) {
                        messageDigest.update(calculatedHash);
                        messageDigest.update(factorData.getHash());
                        calculatedHash = messageDigest.digest();
                    }
                }
            } else {
                validator.log("No VerificationFactorData present");
            }

            validator.log(String.format("Calculated hash: %s", byteArrayToHex(calculatedHash)));
//            validator.log("Signature TODO");

            FrameInfo info = validator.lookupFrame(header.uniqueFrameID);

            if (info != null) {
                byte[] submittedHash = DatatypeConverter.parseBase64Binary(info.imageHash);
                validator.log(String.format("Retrieving frame hash: %s", byteArrayToHex(submittedHash)));
                validator.log(String.format("Frame hash submitted: %s", new Date(info.issueDate * 1000).toString()));

                boolean validationOK = Arrays.equals(calculatedHash, submittedHash);

                if (validationOK) {
                    validator.log("Hash OK");
                    validator.log("Frame VERIFIED");

                    return VerificationStatus.OK;
                } else {
                    validator.log("Hash verification FAILED.");
                    return VerificationStatus.FAILED;
                }


            } else {
                validator.log("No frame record on server.");
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return VerificationStatus.UNKNOWN;
    }


    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(Integer.toHexString(aByte & 0xFF).toUpperCase());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SPImage \n");
        sb.append(String.format("\n"));
        sb.append(String.format("Frame ID: %s\n", byteArrayToHex(header.uniqueFrameID)));
        sb.append(String.format("Frame hash: %s\n", byteArrayToHex(header.frameHash)));

        return sb.toString();
    }

    public enum VerificationStatus {
        OK, FAILED, UNKNOWN
    }
}
