package eu.tpmusielak.securephoto.viewer.lazylist;

import android.content.Context;

import java.io.File;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {

        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = context.getExternalCacheDir();
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        return new File(cacheDir, url);
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}