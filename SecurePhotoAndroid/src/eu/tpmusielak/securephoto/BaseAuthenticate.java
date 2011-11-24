package eu.tpmusielak.securephoto;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
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
    private Runnable authThread;

    private final int AUTH_PROGRESS_DIALOG = 0;

    private enum MESSAGES{AUTH_OK, AUTH_FAILED}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        preferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        authThread = connectToServer();
        showDialog(AUTH_PROGRESS_DIALOG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        authThread.run();
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

    private Thread connectToServer() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    //
                }
                Message message = new Message();
                message.obj = MESSAGES.AUTH_OK;
                msgHandler.sendMessage(message);
            }
        };
    }

    private Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == MESSAGES.AUTH_OK) {
                removeDialog(AUTH_PROGRESS_DIALOG);
            }
        }
    };








}



