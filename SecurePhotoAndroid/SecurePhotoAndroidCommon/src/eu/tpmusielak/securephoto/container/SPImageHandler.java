package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.tools.FileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 16:21
 */
public class SPImageHandler implements SPFileHandler {

    private VerifierProvider verifierProvider;

    public SPImageHandler(VerifierProvider provider) {
        verifierProvider = provider;
    }

    @Override
    public String saveFile(byte[] bytes) {
        File pictureFile = null;
        try {
            pictureFile = FileHandling.getOutputFile(SPImage.defaultExtension);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            SPImage image = SPImage.getInstance(bytes, verifierProvider.getVerifiers());
            fileOutputStream.write(image.toByteArray());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return pictureFile.getName();
    }
}

