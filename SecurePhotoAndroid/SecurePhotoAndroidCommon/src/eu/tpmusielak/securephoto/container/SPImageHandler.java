package eu.tpmusielak.securephoto.container;

import android.content.Context;
import eu.tpmusielak.securephoto.container.wrapper.SPIWrapper;
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
    public SPIWrapper saveFile(byte[] bytes) {
        File pictureFile;
        try {
            pictureFile = FileHandling.getOutputFile(SPImage.DEFAULT_EXTENSION);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create output file");
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            SPImage image = SPImage.getInstance(bytes, verifierProvider.getVerifiers());
            fileOutputStream.write(image.toByteArray());
            fileOutputStream.close();
            return new SPIWrapper(pictureFile, image.getHeader(), image.getFrameHash());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("Cannot write to file %s", pictureFile.getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e.toString());
        }
    }

    @Override
    public void onInitialize(Context context) {
    }
}

