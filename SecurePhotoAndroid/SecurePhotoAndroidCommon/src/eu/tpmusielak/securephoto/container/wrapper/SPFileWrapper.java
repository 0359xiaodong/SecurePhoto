package eu.tpmusielak.securephoto.container.wrapper;

import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.container.SPImageHeader;
import eu.tpmusielak.securephoto.container.SPImageRoll;

import java.io.File;
import java.io.IOException;
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

    public static SPFileWrapper getWrapperForFile(File file) {
        String extension = file.getName().toLowerCase();
        try {
            if (extension.endsWith(SPImage.DEFAULT_EXTENSION)) {
                return new SPIWrapper(file, SPImageHeader.fromFile(file));
            } else if (extension.endsWith(SPImageRoll.DEFAULT_EXTENSION)) {
                SPImageRoll roll = SPImageRoll.fromFile(file);
                return new SPRWrapper(file, roll.getHeader(), 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
