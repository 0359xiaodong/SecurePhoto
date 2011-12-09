package eu.tpmusielak.securephoto.communication;

import java.io.Serializable;

public class ServerMessage implements Serializable {
    public static final String SRV_ADDRESS_STRING = "serverAddress";
    public static final String BUNDLE_ID = "ServerMessage";

    public enum ServerResponse {AUTH_OK, AUTH_FAILED}

    private final ServerResponse serverResponse;
    private final String serverName;
    private final String serverAddress;
    private String errorMessage = null;

    public ServerMessage(ServerResponse serverResponse, String serverName, String serverAddress) {
        this.serverResponse = serverResponse;
        this.serverName = serverName;
        this.serverAddress = serverAddress;
    }

    public String getServerName() {
        return serverName;
    }

    public ServerResponse getServerResponse() {
        return serverResponse;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
