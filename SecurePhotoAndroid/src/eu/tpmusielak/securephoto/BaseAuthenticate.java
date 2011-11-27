package eu.tpmusielak.securephoto;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 24.11.11
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
public class BaseAuthenticate extends Activity {
    private Resources resources;
    private SharedPreferences preferences;

    private ProgressDialog progressDialog;

    private final int AUTH_PROGRESS_DIALOG = 0;

    private enum ServerMessage {AUTH_OK, AUTH_FAILED}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        preferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);


    }

    @Override
    protected void onStart() {
        super.onStart();
        new ConnectToServerTask().execute();
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
    
    private class ConnectToServerTask extends AsyncTask<Void, Void, ServerMessage> {

        @Override
        protected void onPreExecute() {
            showDialog(AUTH_PROGRESS_DIALOG);
        }

        @Override
        protected ServerMessage doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ServerMessage.AUTH_OK;
        }

        @Override
        protected void onPostExecute(ServerMessage serverMessage) {
            Message message = new Message();
            message.obj = serverMessage;
            msgHandler.sendMessage(message);
        }
    }

    private Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == ServerMessage.AUTH_OK) {
                removeDialog(AUTH_PROGRESS_DIALOG);
                setResult(Activity.RESULT_OK);
                finish();
            } else if (msg.obj == ServerMessage.AUTH_FAILED) {

            }
            
        }
    };








}



