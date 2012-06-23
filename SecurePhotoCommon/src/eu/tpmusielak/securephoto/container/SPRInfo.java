package eu.tpmusielak.securephoto.container;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 23/06/12
 * Time: 14:03
 */
public class SPRInfo {
    /* Class holding information about SecurePhoto Roll stored on the SecurePhoto Server */

    public final long issueDate;
    public final String deviceID;
    public final String sprID;
    public final String user;

    public SPRInfo(long issueDate, String deviceID, String sprID, String user) {
        this.issueDate = issueDate;
        this.sprID = sprID;
        this.deviceID = deviceID;
        this.user = user;
    }

    @Override
    public String toString() {
        Date date = new Date(issueDate * 1000);

        return String.format(
                "SPR ID: %s\n" +
                        "Issued: %s\n" +
                        "Device ID: %s\n" +
                        "User: %s", sprID, date.toString(), deviceID, user);
    }
}
