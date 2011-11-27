package eu.tpmusielak.securephoto;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    public static File getOutputRAWFile() throws IOException {
        return getOutputFile("raw");
    }
    
    public static File getOutputJPEGFile() throws IOException {
        return getOutputFile("jpg");
    }

    public static File getOutputFile(String extension) throws IOException {
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

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "SCIMG_" + timeStamp + "." + extension);

        return mediaFile;
    }



}
