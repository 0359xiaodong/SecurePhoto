package eu.tpmusielak.securephoto.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 24.11.11
 * Time: 12:24
 */
public class SecureServer extends Thread implements IMessagePrinter {

    public static final String KNOWN_DEVICES_FILE = "knownDevices.sec";
    private final int port;
    private IMessagePrinter msgPrinter;

    private Set<String> knownDevices;

    public SecureServer(int port) {
        this.port = port;
        this.msgPrinter = this;
        this.knownDevices = new HashSet<String>();

        try {
            InputStream file = new FileInputStream(KNOWN_DEVICES_FILE);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            this.knownDevices = (Set<String>) input.readObject();

            input.close();


        } catch (FileNotFoundException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void setMessagePrinter(IMessagePrinter msgPrinter) {
        this.msgPrinter = msgPrinter;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;

        try {
            msgPrinter.printMsg("Starting the server on port " + this.port);
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            msgPrinter.printMsg("Unable to setup the server socket on port " + this.port);
            return;
        }

        msgPrinter.printMsg("OK");

        while (true) {
            try {
                Socket connSocket = serverSocket.accept();
                InetAddress clientAddress = connSocket.getInetAddress();

                msgPrinter.printMsg(clientAddress.getHostName() + " connected");

                Thread t = new ConnectionHandler(this, connSocket);
                t.start();

            } catch (IOException e) {
                msgPrinter.printMsg("Error establishing connection.");
                break;
            }
        }
    }

    public synchronized void addKnownDevice(String deviceID) {
        knownDevices.add(deviceID);
        OutputStream file;
        try {
            file = new FileOutputStream(KNOWN_DEVICES_FILE);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            output.writeObject(knownDevices);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized boolean isKnown(String deviceID) {
        return knownDevices.contains(deviceID);
    }

    public synchronized void printMsg(String msg) {
        System.out.println(msg);
    }
}
