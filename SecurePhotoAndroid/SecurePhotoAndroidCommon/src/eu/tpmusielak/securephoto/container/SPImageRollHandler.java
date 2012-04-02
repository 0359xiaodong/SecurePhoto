package eu.tpmusielak.securephoto.container;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 17:13
 */
public class SPImageRollHandler implements SPFileHandler {

    private VerifierProvider verifierProvider;

    public SPImageRollHandler(VerifierProvider provider) {
        this.verifierProvider = provider;
    }

    @Override
    public File saveFile(byte[] bytes) {
        //TODO: Check auto-generated code
        return null;
    }
}
