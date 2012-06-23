package eu.tpmusielak.securephoto.container;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 23/06/12
 * Time: 14:08
 */
public class FrameInfo {
    /* Class holding information about SecurePhoto Roll stored on the SecurePhoto Server */

    public final long issueDate;
    public final String imageHash;
    public final String deviceID;
    public final String imageID;
    public final String sprID;
    public final String user;

    public FrameInfo(long issueDate, String imageHash, String deviceID, String imageID, String sprID, String user) {
        this.issueDate = issueDate;
        this.imageHash = imageHash;
        this.deviceID = deviceID;
        this.imageID = imageID;
        this.sprID = sprID;
        this.user = user;
    }

    @Override
    public String toString() {
        Date date = new Date(issueDate * 1000);

        return String.format(
                "Frame ID: %s" +
                        "SPR ID: %s\n" +
                        "Received: %s\n" +
                        "Device ID: %s\n" +
                        "Image Hash: %s\n" +
                        "User: %s", imageID, sprID, date.toString(), deviceID, imageHash, user);
    }
}
