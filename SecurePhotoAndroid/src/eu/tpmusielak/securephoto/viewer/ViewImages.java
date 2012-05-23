package eu.tpmusielak.securephoto.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.*;
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
    public static final int THUMBNAIL_SIZE = 80;

    private Context mContext;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScreen();
    }

    private void setupScreen() {
        setContentView(R.layout.gallery_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        listView = (ListView) findViewById(R.id.gallery_list);
        File[] files = FileHandling.getFiles();

        if (!(files == null) && files.length > 0) {
            ImageViewAdapter adapter = new ImageViewAdapter(ViewImages.this, R.layout.gallery_row, files);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new ImageClickListener());

            registerForContextMenu(listView);
        } else {
            TextView textView = new TextView(ViewImages.this);
            textView.setText(R.string.no_files_found);

            TextView galleryInfo = (TextView) findViewById(R.id.gallery_info);
            galleryInfo.setText(R.string.no_files_found);

            galleryInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_menu, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.delete:
                deleteFile((File) listView.getItemAtPosition(info.position));
                break;
            default:
                break;
        }

        return false;
    }

    private void deleteFile(final File file) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewImages.this);


        builder.setMessage(R.string.ask_confirm)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        boolean success = file.delete();
                        int message = success ? R.string.delete_success : R.string.delete_failure;
                        Toast.makeText(ViewImages.this, message, Toast.LENGTH_SHORT).show();

                        setupScreen();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
            ViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.roll_descriptor);
                holder.image = (ImageView) convertView.findViewById(R.id.file_view);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            File file = getItem(position);
            String fileName = file.getName();

            holder.text.setText(fileName);
            imageLoader.load(file, holder.image);

            return convertView;
        }

    }

    private static class ViewHolder {
        TextView text;
        ImageView image;
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
