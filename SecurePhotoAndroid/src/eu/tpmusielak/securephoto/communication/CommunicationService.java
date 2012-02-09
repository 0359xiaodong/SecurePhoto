package eu.tpmusielak.securephoto.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Pair;
import eu.tpmusielak.securephoto.SPConstants;
import org.bouncycastle.tsp.*;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 06.12.11
 * Time: 00:37
 */
public class CommunicationService extends Service {
    public final IBinder mBinder = new CommServiceBinder();

    private boolean authenticated = false;
    private String serverAddress = "";
    private String androidID = "";

    private static final String timestampServerAddress = "http://www.cryptopro.ru/tsp/tsp.srf";


    public class CommServiceBinder extends Binder {
        public CommunicationService getService() {
            return CommunicationService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final int CONNECTION_TIMEOUT = 2000;
    private final int SOCKET_TIMEOUT = 10000;

    public ServerMessage authenticate(String serverName, String androidID) {
        ServerMessage serverMessage = null;
        Exception exception = null;

        try {
            SocketAddress address = new InetSocketAddress(serverName, SPConstants.SERVER_DEFAULT_PORT);

            Socket s = new Socket();
            s.setSoTimeout(SOCKET_TIMEOUT);

            s.connect(address);

            ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());
            outputStream.writeObject(ClientMessage.getAuthenticationRequest(androidID));
            outputStream.flush();

            ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());

            serverMessage = (ServerMessage) inputStream.readObject();

            s.close();
            inputStream.close();

        } catch (UnknownHostException e) {
            exception = e;
        } catch (IOException e) {
            exception = e;
        } catch (ClassNotFoundException e) {
            exception = e;
        }

        if (serverMessage == null) {
            serverMessage = new ServerMessage(ServerMessage.ServerResponse.AUTH_FAILED, serverName, "");
            if (exception != null) {
                serverMessage.setErrorMessage(exception.getMessage());
            }
        }

        this.authenticated = true;
        this.serverAddress = serverMessage.getServerAddress();
        this.androidID = androidID;
        return serverMessage;
    }

    public ServerMessage sendImageNotification(byte[] imageHash) {
        ServerMessage serverMessage = null;

        if (authenticated) {
            Exception exception = null;

            try {
                SocketAddress address = new InetSocketAddress(serverAddress, SPConstants.SERVER_DEFAULT_PORT);

                Socket s = new Socket();
                s.setSoTimeout(SOCKET_TIMEOUT);

                s.connect(address);

                ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());
                outputStream.writeObject(ClientMessage.getImageTakenMessage(androidID, imageHash));
                outputStream.flush();

                ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());

                serverMessage = (ServerMessage) inputStream.readObject();

                s.close();
                inputStream.close();

            } catch (UnknownHostException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            } catch (ClassNotFoundException e) {
                exception = e;
            }

        }

        return serverMessage;
    }

    public Pair<TimeStampRequest, TimeStampResponse> getTimestamp(byte[] imageHash) {
        TimeStampRequest timeStampRequest = null;
        TimeStampResponse timeStampResponse = null;
        try {
            TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            BigInteger nonce = BigInteger.valueOf(secureRandom.nextLong());

            timeStampRequest = reqGen.generate(TSPAlgorithms.SHA1, imageHash, nonce);

            byte[] requestBytes = timeStampRequest.getEncoded();

            URL netAddress = new URL(timestampServerAddress);
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
            timeStampResponse = new TimeStampResponse(responseStream);
            responseStream.close();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TSPException e) {
            e.printStackTrace();
        }
        return new Pair<TimeStampRequest, TimeStampResponse>(timeStampRequest, timeStampResponse);
    }

}



