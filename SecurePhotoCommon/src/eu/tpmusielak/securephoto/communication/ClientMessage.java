package eu.tpmusielak.securephoto.communication;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 07.12.11
 * Time: 01:02
 */
public class ClientMessage implements Serializable {

    public static enum MessageType {AuthenticationRequest, ImageTaken}

    private final MessageType type;
    private String deviceID;
    private byte[] imageHash;

    private ClientMessage setImageHash(byte[] imageHash) {
        this.imageHash = imageHash;
        return this;
    }

    public byte[] getImageHash() {
        return imageHash;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public static ClientMessage getAuthenticationRequest(String deviceID) {
        return new ClientMessage(MessageType.AuthenticationRequest, deviceID);
    }

    public static ClientMessage getImageTakenMessage(String deviceID, byte[] imageHash) {
        return new ClientMessage(MessageType.ImageTaken, deviceID).setImageHash(imageHash);
    }

    private ClientMessage(MessageType type, String deviceID) {
        this.type = type;
        this.deviceID = deviceID;
    }

    public MessageType getType() {
            return type;
    }

}
