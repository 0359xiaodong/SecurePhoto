package eu.tpmusielak.securephoto.timestamp;


import eu.tpmusielak.bouncy.cms.SignerId;
import eu.tpmusielak.bouncy.tsp.*;

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
 * Date: 05.02.12
 * Time: 22:09
 */
public class TimestampClient {

    private static final String serverAddress = "http://www.cryptopro.ru/tsp/tsp.srf";
//    private static final String serverAddress = "http://timestamping.edelweb.fr/service/tsp";


    static void requestTSP() throws IOException, NoSuchAlgorithmException, TSPException {
        TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        BigInteger nonce = BigInteger.valueOf(secureRandom.nextLong());

        TimeStampRequest request = reqGen.generate(TSPAlgorithms.SHA1, new byte[20], nonce);

        byte[] requestBytes = request.getEncoded();

        URL netAddress = new URL(serverAddress);
        HttpURLConnection connection = (HttpURLConnection) netAddress.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("ContentType", "application/timestamp-query");
        connection.setRequestProperty("ContentLength", String.valueOf(requestBytes.length));
        connection.setDoOutput(true);
        connection.connect();

        OutputStream stream = connection.getOutputStream();
        stream.write(requestBytes, 0, requestBytes.length);
        stream.close();

        InputStream responseStream = new BufferedInputStream(connection.getInputStream());
        TimeStampResponse timeStampResponse = new TimeStampResponse(responseStream);
        responseStream.close();

        byte[] response = timeStampResponse.getEncoded();

        TimeStampRequest recoveredRequest = new TimeStampRequest(requestBytes);
        TimeStampResponse recoveredResponse = new TimeStampResponse(response);

        recoveredResponse.validate(recoveredRequest);

        TimeStampToken  tsToken = recoveredResponse.getTimeStampToken();
        TimeStampTokenInfo tsInfo= tsToken.getTimeStampInfo();
        SignerId signer_id = tsToken.getSID();
        BigInteger cert_serial_number = signer_id.getSerialNumber();
        System.out.println ("Generation time " + tsInfo.getGenTime());
        System.out.println ("Signer ID serial "+signer_id.getSerialNumber());
        System.out.println ("Signer ID issuer "+signer_id.getIssuerAsString());



    }

    public static void main(String[] args) throws Exception {
        requestTSP();
    }
}
