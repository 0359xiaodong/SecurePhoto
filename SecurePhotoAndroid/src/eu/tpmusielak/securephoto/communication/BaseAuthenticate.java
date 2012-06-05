package eu.tpmusielak.securephoto.communication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImageRoll;
import eu.tpmusielak.securephoto.tools.FileHandling;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 24.11.11
 * Time: 12:01
 */
public class BaseAuthenticate extends Activity {
    private Resources resources;
    private SharedPreferences preferences;

    private ProgressDialog progressDialog;

    private final int AUTH_PROGRESS_DIALOG = 0;
    private String androidID;

    private Button authButton;
    private Button createSPIRollButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        setContentView(R.layout.authentication);

        final String address = preferences.getString("base_station_address", null);

        authButton = (Button) findViewById(R.id.btn_go_auth);
        authButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //TODO:
            }
        });

        createSPIRollButton = (Button) findViewById(R.id.btn_debug_create_SPIroll);
        createSPIRollButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    File f = FileHandling.getOutputFile("spr");
                    new SPImageRoll(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, CommunicationService.class);
        String address = preferences.getString("base_station_address", null);

        if (address != null) {

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case AUTH_PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(BaseAuthenticate.this);
                progressDialog.setMessage(resources.getString(R.string.msg_authenticating_wait));
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(true);
//                progressDialog.setCancelMessage(Message );
                return progressDialog;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(Activity.RESULT_CANCELED);
    }


    private Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ServerMessage authenticationMessage = (ServerMessage) msg.obj;

            removeDialog(AUTH_PROGRESS_DIALOG);

            Intent i = new Intent();
            i.putExtra(ServerMessage.BUNDLE_ID,
                    authenticationMessage);

            setResult(Activity.RESULT_OK, i);
            finish();
        }
    };


}



