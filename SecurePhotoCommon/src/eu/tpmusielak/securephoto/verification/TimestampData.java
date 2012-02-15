package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.securephoto.container.SPImage;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 12:58
 */
public class TimestampData implements VerificationFactorData, Serializable {
    private final byte[] request;
    private final byte[] response;
    private final byte[] timestampHash;

    public TimestampData(byte[] request, byte[] response) {
        this.request = request;
        this.response = response;

        byte[] mTimestampHash = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SPImage.digestAlgorithm);

            byte[] jointTS = new byte[request.length + response.length];
            System.arraycopy(request, 0, jointTS, 0, request.length);
            System.arraycopy(response, 0, jointTS, request.length, response.length);

            mTimestampHash = messageDigest.digest(jointTS);
        } catch (NoSuchAlgorithmException ignore) {
        }
        this.timestampHash = mTimestampHash;
    }

    public byte[] getRequest() {
        return request;
    }

    public byte[] getResponse() {
        return response;
    }

    @Override
    public byte[] getHash() {
        return timestampHash;
    }
}
