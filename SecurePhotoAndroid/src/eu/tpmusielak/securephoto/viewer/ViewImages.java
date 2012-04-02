package eu.tpmusielak.securephoto.viewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.tools.FileHandling;
import eu.tpmusielak.securephoto.viewer.lazylist.ImageLoader;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ListView listView = (ListView) findViewById(R.id.gallery_list);
        String[] fileNames = FileHandling.getFileNames(".spi");
        File[] files = FileHandling.getFiles();


        if (files.length > 0) {
            listView.setAdapter(new ImageViewAdapter(ViewImages.this, R.layout.gallery_row, files));
            listView.setOnItemClickListener(new ImageClickListener());
        } else {
            //TODO: show something in the gallery when no images are shown
        }
    }


    private class ImageViewAdapter extends ArrayAdapter<File> {
        // http://android-er.blogspot.com/2010/06/using-convertview-in-getview-to-make.html

        // inspired by: https://github.com/thest1/LazyList/
        private Context context;
        private int layoutResourceId;
        private File[] files;

        private LayoutInflater layoutInflater;
        private ImageLoader imageLoader;

        public ImageViewAdapter(Context context, int layoutResourceId, File[] files) {
            super(context, layoutResourceId, files);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.files = files;

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageLoader = new ImageLoader(context);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                row = layoutInflater.inflate(layoutResourceId, parent, false);
            }

            File file = getItem(position);
            String fileName = file.getName();

            TextView label = (TextView) row.findViewById(R.id.roll_descriptor);
            View fileView = row.findViewById(R.id.file_view);

            label.setText(fileName);

            ViewGroup parentView = (ViewGroup) row;
            int viewIndex = parentView.indexOfChild(fileView);
            parentView.removeViewAt(viewIndex);

            ImageView imageView = new ImageView(context);
            imageView.setId(R.id.file_view);

            imageLoader.load(file, imageView);

            parentView.addView(imageView, viewIndex);

            return row;
        }

    }

    private class ImageClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            View fileView = view.findViewById(R.id.file_view);

            if (fileView == null) {
            } else if (fileView instanceof ImageView) {
                File file = (File) parent.getItemAtPosition(position);

                Intent i = new Intent(getApplicationContext(), OpenImage.class);
                i.putExtra("filename", file.getAbsolutePath());
                startActivity(i);
            }
        }
    }

    // Adapter for image roll
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

//    try {
//                   if (fileName.endsWith(".spi")) {
//                       ImageView imageView = new ImageView(getContext());
//
//                       SPImage spImage = SPImage.fromFile(file);
//                       byte[] imageData = spImage.getImageData();
//
//                       Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
//                       bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 150);
//
//                       imageView.setImageBitmap(bitmap);
//
//                       displayView = imageView;
//
//
//                   } else if (fileName.endsWith(".spr")) {
//                       Gallery roll = new Gallery(getContext());
//                       roll.setAdapter(new ImageAdapter(getContext(), file));
//                       displayView = roll;
//
//                   } else if (fileName.endsWith(".jpg")) {
//                       ImageView imageView = new ImageView(getContext());
//                       imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_gallery));
//                       displayView = imageView;
//                   }
//
//                   if (displayView != null) {
//                       displayView.setId(R.id.file_view);
//                       parentView.addView(displayView);
//                   }
//
//               } catch (Exception e) {
//                   e.printStackTrace();
//               }


}
