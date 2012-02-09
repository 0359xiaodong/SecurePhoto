package eu.tpmusielak.securephoto.container;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 03:11
 */
public class TimestampRecord implements VerificationFactor {
    private final byte[] timestampRequest;
    private final byte[] timestampResponse;
    private final byte[] timestampHash;

    public TimestampRecord(byte[] timestampRequest, byte[] timestampResponse) {
        this.timestampRequest = timestampRequest;
        this.timestampResponse = timestampResponse;
        byte[] mTimestampHash = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);

            byte[] jointTS = new byte[timestampRequest.length + timestampResponse.length];
            System.arraycopy(timestampRequest, 0, jointTS, 0, timestampRequest.length);
            System.arraycopy(timestampResponse, 0, jointTS, timestampRequest.length, timestampResponse.length);

            mTimestampHash = messageDigest.digest(jointTS);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.timestampHash = mTimestampHash;
    }

    @Override
    public byte[] getHash() {
        return timestampHash;
    }
}
