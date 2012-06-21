package eu.tpmusielak.securephoto.container;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 21/06/12
 * Time: 18:16
 */
public class SPImageHeader implements Serializable {
    protected long size;
    protected byte[] uniqueFrameID;
    protected byte[] frameHash;

    public SPImageHeader(long size, byte[] frameHash, byte[] uniqueFrameID) {
        this.size = size;
        this.frameHash = frameHash;
        this.uniqueFrameID = uniqueFrameID;
    }

    public SPImageHeader() {
        this(0, null, null);
    }

    public long getSize() {
        return size;
    }

    public byte[] getFrameHash() {
        return frameHash;
    }

    public byte[] getUniqueFrameID() {
        return uniqueFrameID;
    }
}
