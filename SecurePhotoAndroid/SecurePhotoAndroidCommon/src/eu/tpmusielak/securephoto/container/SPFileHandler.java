package eu.tpmusielak.securephoto.container;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 16:24
 */
public interface SPFileHandler {
    public File saveFile(byte[] bytes);
}
