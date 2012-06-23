package eu.tpmusielak.securephoto;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import eu.tpmusielak.securephoto.camera.TakeImage;
import eu.tpmusielak.securephoto.communication.CommunicationService;
import eu.tpmusielak.securephoto.communication.ServerMessage;
import eu.tpmusielak.securephoto.preferences.ShowPreferences;
import eu.tpmusielak.securephoto.verification.SCVerifierManager;
import eu.tpmusielak.securephoto.viewer.ViewImages;

import static eu.tpmusielak.securephoto.communication.ServerMessage.ServerResponse;

public class HomeScreen extends Activity {
    private static final int PREFERENCES_INTENT = 100;
    private static final int TAKE_IMAGE_INTENT = 101;
    private static final int AUTHENTICATION_INTENT = 102;
    private static final int VIEW_IMAGE_INTENT = 103;

    private Button getRollButton;
    private Button takeImgButton;
    private Button viewImgButton;
    private Button prefButton;

    private CommunicationService communicationService;

    private ServiceConnection communicationServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            communicationService = ((CommunicationService.CommuncationServiceBinder) iBinder).getService();
        }

        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent verifierServiceIntent = new Intent(this, SCVerifierManager.class);
        startService(verifierServiceIntent);

        Intent communicationServiceIntent = new Intent(this, CommunicationService.class);
        startService(communicationServiceIntent);

        setContentView(R.layout.home_screen);
        initialiseUI();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, CommunicationService.class);
        bindService(intent, communicationServiceConnection, Context.BIND_AUTO_CREATE);
    }


    public void initialiseUI() {
        getRollButton = (Button) findViewById(R.id.btn_get_roll);
        getRollButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                getRoll();
            }
        });

        takeImgButton = (Button) findViewById(R.id.btn_takeimg);
        takeImgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                takeImage();
            }
        });

        viewImgButton = (Button) findViewById(R.id.btn_viewimg);
        viewImgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                startViewer();
            }
        });

        prefButton = (Button) findViewById(R.id.btn_pref);
        prefButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                showPreferences();
            }
        });
    }

    private void takeImage() {
        Intent takeImageIntent = new Intent(this, TakeImage.class);
        startActivityForResult(takeImageIntent, TAKE_IMAGE_INTENT);
    }

    private void startViewer() {
        Intent startViewerIntent = new Intent(this, ViewImages.class);
        startActivityForResult(startViewerIntent, VIEW_IMAGE_INTENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showPreferences() {
        Intent preferencesIntent = new Intent(this, ShowPreferences.class);
        startActivityForResult(preferencesIntent, PREFERENCES_INTENT);
    }

    private void getRoll() {
        communicationService.getSPRoll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AUTHENTICATION_INTENT:
                onAuthenticationResult(resultCode, data);
                break;
            case VIEW_IMAGE_INTENT:
                break;
        }
    }

    private void onAuthenticationResult(int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                ServerMessage serverMessage
                        = (ServerMessage) data.getSerializableExtra(ServerMessage.BUNDLE_ID);

                ServerResponse serverResponse = serverMessage.getServerResponse();
                String userMessage = null;
                int toastLength = Toast.LENGTH_SHORT;

                switch (serverResponse) {
                    case AUTH_OK:
                        userMessage = getString(R.string.msg_authenticating_ok);
                        userMessage = String.format(userMessage, serverMessage.getServerName());

                        getRollButton.setEnabled(false);
                        getRollButton.setText(R.string.msg_autheticated);

                        // Starting the communication service
                        Intent serviceIntent = new Intent(this, CommunicationService.class);
                        serviceIntent.putExtra(ServerMessage.BUNDLE_ID, serverMessage);

                        startService(serviceIntent);

                        takeImgButton.setEnabled(true);

                        break;
                    case AUTH_FAILED:
                        userMessage = getString(R.string.msg_authenticating_failed);
                        userMessage = String.format(userMessage, serverMessage.getServerAddress(), serverMessage.getErrorMessage());
                        toastLength = Toast.LENGTH_LONG;
                        break;
                }

                Toast t = Toast.makeText(this, userMessage, toastLength);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                break;
            case Activity.RESULT_CANCELED:
                break;
            default:
                break;

        }
        if (resultCode == Activity.RESULT_OK) {


        }
    }

    @Override
    protected void onDestroy() {
        unbindService(communicationServiceConnection);
        if (isFinishing()) {
            stopService(new Intent(this, CommunicationService.class));
            stopService(new Intent(this, SCVerifierManager.class));
        }


        super.onDestroy();
    }
}
