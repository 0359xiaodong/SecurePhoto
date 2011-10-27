package eu.tpmusielak.securephoto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeScreen extends Activity
{
    private static final int PREFERENCES_INTENT = 100;
    private static final int TAKE_IMAGE_INTENT = 101;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        initialiseUI();
    }

    public void initialiseUI() {
        Button authButton = (Button) findViewById(R.id.btn_auth);
        authButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
            }
        });

        Button takeImgButton = (Button) findViewById(R.id.btn_takeimg);
        takeImgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                takeImage();
            }
        });

        Button viewImgButton = (Button) findViewById(R.id.btn_viewimg);
        viewImgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        Button exportImgButton = (Button) findViewById(R.id.btn_export);
        exportImgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        Button prefButton = (Button) findViewById(R.id.btn_pref);
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


}
