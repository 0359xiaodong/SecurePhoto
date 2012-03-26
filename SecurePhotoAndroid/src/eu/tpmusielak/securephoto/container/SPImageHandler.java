package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.FileHandling;
import eu.tpmusielak.securephoto.verification.VerificationFactor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 16:21
 */
public class SPImageHandler implements SPFileHandler {

    private List<VerificationFactor> verifiers;

    public SPImageHandler(List<VerificationFactor> verifiers) {
        this.verifiers = verifiers;
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
            SPImage image = SPImage.getInstance(bytes, verifiers);
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

