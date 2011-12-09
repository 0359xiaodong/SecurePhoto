package eu.tpmusielak.securephoto.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import eu.tpmusielak.securephoto.SPConstants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

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
            serverMessage.setErrorMessage(exception.getMessage());
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

}



