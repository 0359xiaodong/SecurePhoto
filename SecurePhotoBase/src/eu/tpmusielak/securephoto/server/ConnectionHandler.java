package eu.tpmusielak.securephoto.server;

import eu.tpmusielak.securephoto.communication.ClientMessage;
import eu.tpmusielak.securephoto.communication.ServerMessage;
import eu.tpmusielak.securephoto.communication.ServerMessage.ServerResponse;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 06.12.11
 * Time: 17:23
 */
public class ConnectionHandler extends Thread {

    private Socket clientSocket;
    private ObjectInputStream objectInput;
    private ObjectOutputStream objectOutput;
    private String address;
    private String name;

    private final SecureServer server;

    public ConnectionHandler(SecureServer server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;

        name = "";
        address = "";

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            address = Arrays.toString(inetAddress.getAddress());
            name = inetAddress.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {

            objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());

            // Wait for message
            objectInput = new ObjectInputStream(clientSocket.getInputStream());
            ClientMessage clientMessage = (ClientMessage) objectInput.readObject();

            handleMessage(clientMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        closeConnections();
    }

    private void handleMessage(ClientMessage clientMessage) throws IOException {
        String deviceID = clientMessage.getDeviceID();
        switch (clientMessage.getType()) {
            case AuthenticationRequest:
                if(server.isKnown(deviceID)) {
                    server.printMsg("Known device:" + deviceID);
                } else {
                    server.addKnownDevice(deviceID);
                }

                objectOutput.writeObject(new ServerMessage(ServerResponse.AUTH_OK, name, address));
                            objectOutput.flush();
                break;
            case ImageTaken:
                server.printMsg("Image taken by device: " + deviceID + " with hash: " + clientMessage.getImageHash());
                objectOutput.writeObject(new ServerMessage(ServerResponse.AUTH_OK, name, address));
                                            objectOutput.flush();
                break;
            default:
                break;
        }
    }

    private void closeConnections() {
        try {
            objectOutput.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
