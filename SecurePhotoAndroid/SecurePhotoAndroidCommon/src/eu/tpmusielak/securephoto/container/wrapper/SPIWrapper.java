package eu.tpmusielak.securephoto.container.wrapper;

import eu.tpmusielak.securephoto.container.SPImage;

import java.io.File;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 05/06/12
 * Time: 00:17
 */
public class SPIWrapper extends SPFileWrapper implements Serializable {
    private final SPImage.SPImageHeader header;

    public SPIWrapper(File file, SPImage.SPImageHeader header) {
        this(file, header, null);
    }

    public SPIWrapper(File file, SPImage.SPImageHeader header, byte[] hash) {
        super(file);
        this.header = header;
        if (hash != null)
            setFrameHash(hash);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getFileTypeName() {
        return "SPI";
    }

    public SPImage.SPImageHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getUniqueFrameID() {
        if (header != null) {
            return header.getUniqueFrameID();
        }
        return null;
    }
}
