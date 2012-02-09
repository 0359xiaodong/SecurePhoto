package eu.tpmusielak.securephoto.communication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.res.Resources;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.communication.CommunicationService.CommServiceBinder;

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

    private CommunicationService communicationService;
    private boolean boundToCommService = false;

    private ServiceConnection communicationServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CommServiceBinder binder = (CommServiceBinder) iBinder;
            communicationService = binder.getService();
            boundToCommService = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            boundToCommService = false;
        }
    };
    private Button authButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        
        setContentView(R.layout.authentication);

        final String address = preferences.getString(getString(R.string.kpref_base_station_address), null);
        
        authButton = (Button) findViewById(R.id.btn_go_auth);
        authButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ConnectToServerTask().execute(address);
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, CommunicationService.class);
        bindService(intent, communicationServiceConnection, Context.BIND_AUTO_CREATE);
        String address = preferences.getString(getString(R.string.kpref_base_station_address), null);

        if (address != null) {
//            new ConnectToServerTask().execute(address);
//            ServerMessage serverMessage = communicationService.authenticate(address, androidID);

//            Message m = new Message();
//            m.obj = serverMessage;

//            msgHandler.sendMessage(m);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        
        if(boundToCommService) {
            unbindService(communicationServiceConnection);
            boundToCommService = false;
        }
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

    private class ConnectToServerTask extends AsyncTask<String, Void, ServerMessage> {

        @Override
        protected void onPreExecute() {
            showDialog(AUTH_PROGRESS_DIALOG);
        }

        @Override
        protected ServerMessage doInBackground(String... servers) {
            String serverName = servers[0];

            return communicationService.authenticate(serverName, androidID);
        }

        @Override
        protected void onPostExecute(ServerMessage serverMessage) {
            Message m = new Message();
            m.obj = serverMessage;

            msgHandler.sendMessage(m);
        }
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



