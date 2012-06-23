package eu.tpmusielak.securephoto.container;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 23/06/12
 * Time: 13:04
 */
public interface SPValidator {

    void log(String s);

    SPRInfo lookupSPR(byte[] uniqueID);

    FrameInfo lookupFrame(byte[] uniqueID);
}
