package eu.tpmusielak.securephoto.viewer;

import android.app.Activity;
import android.app.AlertDialog;
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
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.verification.Verifier;
import eu.tpmusielak.securephoto.viewer.lazylist.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 31/03/12
 * Time: 20:33
 */
public class OpenImage extends Activity {
    private int width;
    private int height;


    private byte[] imageData;

    private String filename;

    private Button showVerifiersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        width = display.getWidth();
        height = display.getHeight();


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
            filename = i.getStringExtra("filename");
            displayFile();
        }

    }

    private void displayFile() {
        ImageView preview = (ImageView) findViewById(R.id.image);
        Bitmap bitmap = ImageLoader.decodeFile(new File(filename), Math.min(width, height));
        preview.setImageBitmap(bitmap);
    }


    private class VerifiersButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            displayVerifiers();
        }
    }

    protected void displayVerifiers() {
        if (!filename.endsWith(".spi"))
            return;

        SPImage image = null;
        try {
            image = SPImage.fromFile(new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (image == null)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("Verifiers:\n");

        List<Class<Verifier>> verifiers = image.getVerificationFactors();
        for (Class<Verifier> factorClass : verifiers) {
            sb.append("  * ");
            sb.append(factorClass.getSimpleName());
            sb.append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(sb.toString());
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
