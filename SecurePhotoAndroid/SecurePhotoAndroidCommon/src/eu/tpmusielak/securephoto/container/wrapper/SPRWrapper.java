package eu.tpmusielak.securephoto.container.wrapper;

import eu.tpmusielak.securephoto.container.SPImageRoll;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 05/06/12
 * Time: 00:18
 */
public class SPRWrapper extends SPFileWrapper implements Serializable {
    public final int frameIndex;
    private final SPImageRoll.Header header;


    public SPRWrapper(File file, SPImageRoll.Header header, int frameIndex) {
        this(file, header, null, null, frameIndex);
    }

    public SPRWrapper(File file, SPImageRoll.Header header, byte[] hash, byte[] uniqueFrameID, int frameIndex) {
        super(file);
        this.frameIndex = frameIndex;
        this.header = header;
        this.uniqueFrameID = uniqueFrameID;
        if (hash != null)
            setFrameHash(hash);
    }

    public static SPRWrapper wrapFile(File file) {
        SPImageRoll roll = null;
        SPImageRoll.Header rollHeader = null;
        try {
            roll = SPImageRoll.fromFile(file);
            rollHeader = roll.getHeader();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new SPRWrapper(file, rollHeader, 0);
    }

    @Override
    public String getName() {
        return String.format("%04x_%s", frameIndex, file.getName());
    }

    @Override
    public String getFileTypeName() {
        return "SPR";
    }

    public SPImageRoll.Header getHeader() {
        return header;
    }
}
