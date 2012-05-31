package eu.tpmusielak.securephoto.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImage;
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

    private Button showVerifiersButton;
    private List<Class<Verifier>> verifiers;
    private Map<Class<Verifier>, VerificationFactorData> verifierData;
    private String[] verifierNamesArray;

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

        Bitmap bitmap;
        if (frameIndex < 0) {
            bitmap = ImageLoader.decodeFile(file, imageSize);
        } else {
            bitmap = ImageLoader.decodeFile(new ImageLoader.ImageRoll(file, frameIndex), imageSize);
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

        verifierNamesArray = new String[verifierNames.size()];
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
}
