package eu.tpmusielak.securephoto.container.wrapper;

import java.io.File;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 05/06/12
 * Time: 00:17
 */
public abstract class SPFileWrapper implements Serializable {
    public final File file;
    private byte[] frameHash;
    protected byte[] uniqueFrameID;

    public SPFileWrapper(File file) {
        this.file = file;
        this.frameHash = new byte[0];
    }

    public abstract String getName();

    public abstract String getFileTypeName();

    protected void setFrameHash(byte[] frameHash) {
        this.frameHash = frameHash;
    }

    public File getFile() {
        return file;
    }

    public byte[] getFrameHash() {
        return this.frameHash;
    }

    public byte[] getUniqueFrameID() {
        return this.uniqueFrameID;
    }

}
