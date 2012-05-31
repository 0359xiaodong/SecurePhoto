package eu.tpmusielak.securephoto.viewer.lazylist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.container.SPImageRoll;
import eu.tpmusielak.securephoto.viewer.ViewImages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private final int THUMBNAIL_SIZE = 100;

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(7); //TODO: replace with AsyncTask
    }

    final int stub_id = R.drawable.ic_menu_gallery;

    public void load(File file, ImageView imageView) {
        String name = file.getName();

        imageViews.put(imageView, name);
        Bitmap bitmap = memoryCache.get(name);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(file, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(File file, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(file, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(File file) {
        File f = fileCache.getFile(file.getName());

        //from SD cache
        Bitmap b = decodeFile(f, ViewImages.THUMBNAIL_SIZE);
        if (b != null)
            return b;

        //from disk
        try {
            return decodeFile(file, ViewImages.THUMBNAIL_SIZE);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f, int requiredSize) {
        String fileName = f.getName();

        try {

            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            if (fileName.endsWith(".spi")) {
                byte[] bytes = SPImage.extractImageData(f);
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o);

                int scale = getScale(requiredSize, o);

                //decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o2);
            } else if (fileName.endsWith(".spr")) {
                SPImageRoll imageRoll = SPImageRoll.fromFile(f);
                int imageCount = imageRoll.getFrameCount();
                SPImage spImage = imageRoll.getFrame(imageCount - 1);
                byte[] bytes = spImage.getImageData();

                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o);

                int scale = getScale(requiredSize, o);

                //decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o2);
            } else {
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                int scale = getScale(requiredSize, o);

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;

                return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getScale(int requiredSize, BitmapFactory.Options o) {
        //Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        return scale;
    }

    //Task for the queue
    private class PhotoToLoad {
        public File file;
        public ImageView imageView;

        public PhotoToLoad(File file, ImageView i) {
            this.file = file;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            Bitmap bmp = getBitmap(photoToLoad.file);
            memoryCache.put(photoToLoad.file.getName(), bmp);
            if (imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.file.getName()))
            return true;
        return false;
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
