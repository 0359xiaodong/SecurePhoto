package eu.tpmusielak.securephoto;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 27.10.11
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class ShowPreferences extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}