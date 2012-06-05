package eu.tpmusielak.securephoto.viewer.lazylist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.container.SPImageRoll;
import eu.tpmusielak.securephoto.container.wrapper.SPFileWrapper;
import eu.tpmusielak.securephoto.container.wrapper.SPIWrapper;
import eu.tpmusielak.securephoto.container.wrapper.SPRWrapper;
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

    public void load(SPFileWrapper wrapper, ImageView imageView) {
        String name = wrapper.getName();

        imageViews.put(imageView, name);
        Bitmap bitmap = memoryCache.get(name);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(wrapper, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(SPFileWrapper wrapper, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(wrapper, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(SPFileWrapper wrapper) {
        File f = fileCache.getFile(wrapper.getName());

        //from SD cache
        Bitmap b = decodeFile(f, ViewImages.THUMBNAIL_SIZE);
        if (b != null)
            return b;

        //from disk
        try {
            return decodeFile(wrapper, ViewImages.THUMBNAIL_SIZE);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeFile(File file, int requiredSize) {
        return decodeFile(new SPIWrapper(file), requiredSize);
    }

    //decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(SPFileWrapper wrapper, int requiredSize) {
        String fileName = wrapper.getName();
        File file = wrapper.file;
        int frameIndex = -1;

        if (wrapper instanceof SPRWrapper) {
            frameIndex = ((SPRWrapper) wrapper).frameIndex;
        }

        try {

            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            if (fileName.endsWith(".spi")) {
                byte[] bytes = SPImage.extractImageData(file);
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o);

                int scale = getScale(requiredSize, o);

                //decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o2);
            } else if (fileName.endsWith(".spr")) {


                SPImageRoll imageRoll = SPImageRoll.fromFile(file);
                int imageCount = imageRoll.getFrameCount();
                if (frameIndex < 0) {
                    frameIndex = imageCount - 1;
                }

                SPImage spImage = imageRoll.getFrame(frameIndex);
                byte[] bytes = spImage.getImageData();

                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o);

                int scale = getScale(requiredSize, o);

                //decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o2);
            } else {
                BitmapFactory.decodeStream(new FileInputStream(file), null, o);

                int scale = getScale(requiredSize, o);

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;

                return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
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
        public SPFileWrapper file;
        public ImageView imageView;

        public PhotoToLoad(SPFileWrapper wrapper, ImageView i) {
            this.file = wrapper;
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
