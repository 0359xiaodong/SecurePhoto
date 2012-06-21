package eu.tpmusielak.securephoto.tools;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
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
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, new DescendingDateComparator());
        }
        return files;
    }

    public static File[] getFiles(final String extension) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(extension);
            }
        });

        if (files != null) {
            Arrays.sort(files, new DescendingDateComparator());
        }
        return files;
    }


    public static String[] getFileNames(final String extension) {
        File dir = new File(dirPath);
        return dir.list();
    }

    public static class DescendingDateComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            Long f1Date = f1.lastModified();
            Long f2Date = f2.lastModified();
            return f2Date.compareTo(f1Date);
        }
    }

}
