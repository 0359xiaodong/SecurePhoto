package eu.tpmusielak.securephoto.tools;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
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

    public final static String dirPath
            = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/SecureCamera";

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
        File dir = new File(dirPath);
        return dir.listFiles();
    }

    public static File[] getFiles(final String extension) {
        File dir = new File(dirPath);
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(extension);
            }
        });
    }


    public static String[] getFileNames(final String extension) {
        File dir = new File(dirPath);
        return dir.list();
    }

}