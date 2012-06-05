package eu.tpmusielak.securephoto.container.wrapper;

import java.io.File;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 05/06/12
 * Time: 00:17
 */
public class SPIWrapper extends SPFileWrapper implements Serializable {

    public SPIWrapper(File file) {
        this(file, null);
    }

    public SPIWrapper(File file, byte[] hash) {
        super(file);
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
}
