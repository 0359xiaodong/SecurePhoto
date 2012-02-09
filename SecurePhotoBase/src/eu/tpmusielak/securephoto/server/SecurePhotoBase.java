package eu.tpmusielak.securephoto.server;

import eu.tpmusielak.securephoto.SPConstants;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 20.11.11
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
public class SecurePhotoBase implements ISecurePhotoBase {
    private final int serverPort;
    private SecureServer server;

    public SecurePhotoBase() {
        this.serverPort = SPConstants.SERVER_DEFAULT_PORT;
    }

    public SecurePhotoBase(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        if (this.server == null) {
            this.server = new SecureServer(this.serverPort);
        }
        if (!this.server.isAlive()) {
            this.server.start();
        }
    }

    public static void main(String[] args) {
        System.out.println("SecurePhotoBase");
        ISecurePhotoBase base = new SecurePhotoBase();
        base.start();
    }

}
