package eu.tpmusielak.securephoto.server.test;

import eu.tpmusielak.securephoto.SPConstants;
import eu.tpmusielak.securephoto.server.ISecurePhotoBase;
import eu.tpmusielak.securephoto.server.SecurePhotoBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 24.11.11
 * Time: 17:10
 * To change this template use File | Settings | File Templates.
 */
public class SecurePhotoBaseTests {
    private ISecurePhotoBase base;

    @Test
    public void testBaseStartup() {
        base = new SecurePhotoBase();
        base.start();
    }

    @Test(timeout = 10000)
    public void testServerConnectivity() throws Exception {
        Socket clientSocket = null;
        PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String response = null;

        try {
            clientSocket = new Socket("localhost", SPConstants.SERVER_DEFAULT_PORT);
            outWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outWriter.println("Testing connectivity");
            response = inReader.readLine();

        } catch (IOException e) {
            throw new Exception("Exception during writing to socket: " + e.getMessage());
        }
        Assert.assertEquals(SPConstants.SERVER_OK_MESSAGE, response);
    }




}
