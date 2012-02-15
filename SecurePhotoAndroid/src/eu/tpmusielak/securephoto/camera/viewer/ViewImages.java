package eu.tpmusielak.securephoto.camera.viewer;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import eu.tpmusielak.securephoto.FileHandling;
import eu.tpmusielak.securephoto.R;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 16:33
 */
public class ViewImages extends Activity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScreen();
    }

    private void setupScreen() {
        setContentView(R.layout.gallery_view);

        ListView listView = (ListView) findViewById(R.id.gallery_list);
        String[] fileNames = FileHandling.getFileNames(".spi");
        File[] files = FileHandling.getFiles();

        listView.setAdapter(new ImageViewAdapter(ViewImages.this, R.layout.gallery_row, files));


    }


    private class ImageViewAdapter extends ArrayAdapter<File> {
        // http://android-er.blogspot.com/2010/06/using-convertview-in-getview-to-make.html

        public ImageViewAdapter(Context context, int textViewResourceId, File[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.gallery_row, parent, false);
            }

            File file = getItem(position);
            String fileName = file.getName();

            TextView label = (TextView) row.findViewById(R.id.roll_descriptor);
            label.setText(fileName);

            ViewGroup parentView = (ViewGroup) row;

            parentView.removeAllViews();
            parentView.addView(label);

            View displayView = null;

            try {
                if (fileName.endsWith(".spi")) {
                    Gallery roll = new Gallery(getContext());
                    roll.setAdapter(new ImageAdapter(getContext(), file));
                    displayView = roll;

                } else if (fileName.endsWith(".jpg")) {
                    ImageView imageView = new ImageView(getContext());

//                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 150);
//                    imageView.setImageBitmap(bitmap);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_gallery));
                    displayView = imageView;
                }

                if (displayView != null) {
                    displayView.setId(R.id.file_view);
                    parentView.addView(displayView);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return row;
        }
    }

    class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
        private final File spiFile;

        private Integer[] mImageIds = {
                R.drawable.ic_menu_camera,
                R.drawable.ic_menu_forward,
                R.drawable.ic_menu_preferences,
                R.drawable.ic_menu_login
        };

        public ImageAdapter(Context c, File file) {
            mContext = c;
            TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
            mGalleryItemBackground = attr.getResourceId(
                    R.styleable.HelloGallery_android_galleryItemBackground, 0);
            attr.recycle();
            spiFile = file;
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);

            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_gallery));

            imageView.setImageResource(mImageIds[position]);

            imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setBackgroundResource(mGalleryItemBackground);


            return imageView;
        }
    }
}
