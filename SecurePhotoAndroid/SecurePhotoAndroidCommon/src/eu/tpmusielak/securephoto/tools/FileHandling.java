package eu.tpmusielak.securephoto.tools;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27.11.11
 * Time: 16:26
 */
public class FileHandling {

    public static File getOutputFile(String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = "SCIMG_" + timeStamp;

        return getOutputFile(name, extension);
    }

    public static File getOutputFile(String name, String extension) throws IOException {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new IOException("External storage inaccessible");
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SecureCamera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                name + "." + extension);

        return mediaFile;
    }

    public static File[] getFiles() {
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(dirPath + "/SecureCamera");

        return dir.listFiles();

    }

    public static String[] getFileNames(final String extension) {
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(dirPath + "/SecureCamera");

        return dir.list();
    }

}
