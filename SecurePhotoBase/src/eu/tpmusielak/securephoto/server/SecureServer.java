package eu.tpmusielak.securephoto.server;

import eu.tpmusielak.securephoto.SPConstants;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 24.11.11
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
public class SecureServer extends Thread implements IMessagePrinter {

    private final int port;
    private IMessagePrinter msgPrinter;


    public SecureServer(int port) {
        this.port = port;
        this.msgPrinter = this;
    }

    public void setMessagePrinter(IMessagePrinter msgPrinter) {
        this.msgPrinter = msgPrinter;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;

        try {
            msgPrinter.printMsg("Starting the server on port " + this.port);
            serverSocket = new ServerSocket(this.port);
        }
        catch (IOException e) {
            msgPrinter.printMsg("Unable to setup the server socket on port " + this.port);
            return;
        }

        msgPrinter.printMsg("OK");

        while(true) {
            try {
                Socket connSocket = serverSocket.accept();
                InetAddress clientAddress = connSocket.getInetAddress();

                msgPrinter.printMsg(clientAddress.getHostName() + " connected");

                BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
                PrintWriter outputBuffer = new PrintWriter(connSocket.getOutputStream(), true);

                connection_handler(inputBuffer, outputBuffer);
            }
            catch (IOException e) {
                msgPrinter.printMsg("Error establishing connection.");
            }
        }
    }

    public void connection_handler(BufferedReader input, PrintWriter output) {
        String msg = null;
        try {
            msgPrinter.printMsg(input.readLine());
        } catch (IOException e) {
            msgPrinter.printMsg("Error reading from incoming socket");
        }

        output.println(SPConstants.SERVER_OK_MESSAGE);
    }

    public void printMsg(String msg) {
        System.out.println(msg);
    }
}
