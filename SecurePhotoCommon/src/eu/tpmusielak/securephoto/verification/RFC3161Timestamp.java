package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.bouncy.tsp.*;
import eu.tpmusielak.securephoto.container.SPImage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 03:11
 */
public class RFC3161Timestamp implements VerificationFactor {

    private final String timestampServerAddress;

    public RFC3161Timestamp(String timestampServerAddress) {
        this.timestampServerAddress = timestampServerAddress;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public VerificationFactorData onCapture(SPImage image) {
        // TODO: add error handling

        byte[] imageHash = image.getImageHash();

        byte[] request = null;
        byte[] response = null;

        try {
            TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            BigInteger nonce = BigInteger.valueOf(secureRandom.nextLong());

            TimeStampRequest timeStampRequest = reqGen.generate(TSPAlgorithms.SHA1, imageHash, nonce);

            request = timeStampRequest.getEncoded();

            URL netAddress = new URL(timestampServerAddress);
            HttpURLConnection connection = (HttpURLConnection) netAddress.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("ContentType", "application/timestamp-query");
            connection.setRequestProperty("ContentLength", String.valueOf(request.length));
            connection.setDoOutput(true);
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            stream.write(request, 0, request.length);
            stream.close();

            InputStream responseStream = new BufferedInputStream(connection.getInputStream());
            TimeStampResponse timeStampResponse = new TimeStampResponse(responseStream);
            response = timeStampResponse.getEncoded();
            responseStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TSPException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new TimestampData(request, response);
    }

}
