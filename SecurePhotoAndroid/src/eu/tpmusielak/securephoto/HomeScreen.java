package eu.tpmusielak.securephoto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.StringTokenizer;

public class HomeScreen extends Activity
{
    private static final int PREFERENCES_INTENT = 100;
    private static final int TAKE_IMAGE_INTENT = 101;
    private static final int AUTHENTICATION_INTENT = 102;
    private static final int VIEW_IMAGE_INTENT = 103;

    private Button authButton;
    private Button takeImgButton;
    private Button viewImgButton;
    private Button exportImgButton;
    private Button prefButton;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        initialiseUI();
    }

    public void initialiseUI() {
        authButton = (Button) findViewById(R.id.btn_auth);
        authButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                startAuthentication();
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
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, VIEW_IMAGE_INTENT);
            }
        });

        exportImgButton = (Button) findViewById(R.id.btn_export);
        exportImgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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

    protected void startAuthentication() {
        Intent authenticationIntent = new Intent(this, BaseAuthenticate.class);
        startActivityForResult(authenticationIntent, AUTHENTICATION_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AUTHENTICATION_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast t = Toast.makeText(this, R.string.msg_authenticating_ok, Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();

                    authButton.setEnabled(false);
                    authButton.setText(R.string.msg_autheticated);

                    takeImgButton.setEnabled(true);
                }
                break;
            case VIEW_IMAGE_INTENT:
                break;
        }
    }

}
