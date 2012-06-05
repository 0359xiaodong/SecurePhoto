package eu.tpmusielak.securephoto.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.container.SPImageRoll;
import eu.tpmusielak.securephoto.container.wrapper.SPRWrapper;
import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.Verifier;
import eu.tpmusielak.securephoto.viewer.lazylist.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 31/03/12
 * Time: 20:33
 */
public class OpenImage extends Activity {
    private int width;
    private int height;
    private int imageSize;


    private byte[] imageData;

    private File file;
    private int frameIndex = -1;
    private int frameCount = 0;

    private Button showVerifiersButton;
    private List<Class<Verifier>> verifiers;
    private Map<Class<Verifier>, VerificationFactorData> verifierData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        width = display.getWidth();
        height = display.getHeight();
        imageSize = Math.min(width, height);


        setupScreen();
    }

    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.image_view);

        showVerifiersButton = (Button) findViewById(R.id.btn_show_verifiers);
        showVerifiersButton.setOnClickListener(new VerifiersButtonListener());


        // CC-Attribution
        // Source: StackOverflow
        // Author: Thomas Frankhauser (http://stackoverflow.com/users/408557/thomas-fankhauser)
        // Question: http://stackoverflow.com/questions/937313/android-basic-gesture-detection
        ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
        RelativeLayout lowestLayout = (RelativeLayout) this.findViewById(R.id.open_image_view);
        lowestLayout.setOnTouchListener(activitySwipeDetector);
        // End of attribution

    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();

        if (i.hasExtra("filename")) { // Displaying image from browser
            file = new File(i.getStringExtra("filename"));

        }
        if (i.hasExtra("frameIndex")) {
            frameIndex = i.getIntExtra("frameIndex", -1);
        }

        displayFile();

    }

    private void displayFile() {
        ImageView preview = (ImageView) findViewById(R.id.image);

        Bitmap bitmap = null;
        if (frameIndex < 0) {
            bitmap = ImageLoader.decodeFile(file, imageSize);
        } else {
            try {
                SPImageRoll roll = SPImageRoll.fromFile(file);
                frameCount = roll.getFrameCount();

                bitmap = ImageLoader.decodeFile(new SPRWrapper(file, roll.getHeader(), frameIndex), imageSize);
            } catch (IOException ignored) {
            } catch (ClassNotFoundException ignored) {
            }

        }

        preview.setImageBitmap(bitmap);

        TextView filename = (TextView) findViewById(R.id.filename);
        filename.setText(file.getName());

        TextView filedate = (TextView) findViewById(R.id.filedate);
        Date date = new Date(file.lastModified());
        filedate.setText(date.toLocaleString());

        if (file.getName().endsWith(SPImage.DEFAULT_EXTENSION))
            showVerifiersButton.setVisibility(View.VISIBLE);
    }

    protected void loadNextFrame() {
        if (frameIndex < (frameCount - 1)) {
            frameIndex++;
            displayFile();
        } else if (frameCount > 0) {
            Toast.makeText(OpenImage.this, R.string.last_frame, Toast.LENGTH_SHORT).show();
        }
    }

    protected void loadPreviousFrame() {
        if (frameIndex > 0) {
            frameIndex--;
            displayFile();
        } else if (frameCount > 0) {
            Toast.makeText(OpenImage.this, R.string.first_frame, Toast.LENGTH_SHORT).show();
        }

    }


    private class VerifiersButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            displayVerifiers();
        }
    }

    protected void displayVerifiers() {
        if (!file.getName().endsWith(SPImage.DEFAULT_EXTENSION))
            return;

        SPImage image = null;
        try {
            image = SPImage.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (image == null)
            return;

        List<String> verifierNames = new LinkedList<String>();


        verifiers = image.getVerificationFactors();
        verifierData = image.getVerificationFactorData();

        for (Class<Verifier> factorClass : verifiers) {
            verifierNames.add(factorClass.getSimpleName());
        }

        String[] verifierNamesArray = new String[verifierNames.size()];
        verifierNames.toArray(verifierNamesArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        if (verifierNamesArray.length > 0) {
            builder.setTitle(R.string.verification_factors);
            builder.setItems(verifierNamesArray, new VerifierPickListener(this));
        } else {
            builder.setMessage(R.string.no_verifiers_found);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class VerifierPickListener implements DialogInterface.OnClickListener {
        private Context context;

        private VerifierPickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);

            Class<Verifier> verifierClass = verifiers.get(i);
            VerificationFactorData factorData = verifierData.get(verifierClass);

            if (factorData != null)
                builder.setMessage(factorData.toString());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // CC-Attribution
    // Source: StackOverflow
    // Author: Thomas Frankhauser (http://stackoverflow.com/users/408557/thomas-fankhauser)
    // Question: http://stackoverflow.com/questions/937313/android-basic-gesture-detection
    private class ActivitySwipeDetector implements View.OnTouchListener {

        private OpenImage activity;
        static final int MIN_DISTANCE = 100;
        private float downX, downY, upX, upY;

        public ActivitySwipeDetector(OpenImage activity) {
            this.activity = activity;
        }

        public void onRightToLeftSwipe() {
            activity.loadNextFrame();
        }

        public void onLeftToRightSwipe() {
            activity.loadPreviousFrame();
        }

        public void onTopToBottomSwipe() {
        }

        public void onBottomToTopSwipe() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    upX = event.getX();
                    upY = event.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;

                    // swipe horizontal?
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        // left or right
                        if (deltaX < 0) {
                            this.onLeftToRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onRightToLeftSwipe();
                            return true;
                        }
                    } else {
                        return false; // We don't consume the event
                    }

                    // swipe vertical?
                    if (Math.abs(deltaY) > MIN_DISTANCE) {
                        // top or down
                        if (deltaY < 0) {
                            this.onTopToBottomSwipe();
                            return true;
                        }
                        if (deltaY > 0) {
                            this.onBottomToTopSwipe();
                            return true;
                        }
                    } else {
                        return false; // We don't consume the event
                    }

                    return true;
                }
            }
            return false;
        }

    }
    // End of attribution
}
