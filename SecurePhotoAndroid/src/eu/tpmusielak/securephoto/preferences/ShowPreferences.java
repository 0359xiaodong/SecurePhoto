package eu.tpmusielak.securephoto.preferences;

import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.verification.SCVerifierManager;
import eu.tpmusielak.securephoto.verification.VerifierPreferenceReceiver;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 27.10.11
 * Time: 17:01
 */
public class ShowPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener, VerifierPreferenceReceiver {

    private SharedPreferences preferences;
    private SCVerifierManager verifierManager;

    private ServiceConnection verifierServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            verifierManager = ((SCVerifierManager.VerifierServiceBinder) iBinder).getService();
            verifierManager.bindToPreferences(ShowPreferences.this);
//            verifierManager.generateVerifierEnablers();
            verifierManager.loadVerifierPreferences();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Intent verifierServiceIntent = new Intent(this, SCVerifierManager.class);
        bindService(verifierServiceIntent, verifierServiceConnection, Context.BIND_AUTO_CREATE);

//        verifierManager = SCVerifierManager.getInstance();
//        verifierManager.bindToPreferences(this);
//        verifierManager.generateVerifierEnablers();
//        verifierManager.loadVerifierPreferences();

        preferences = getPreferenceScreen().getSharedPreferences();

        Map<String, ?> allPreferences = preferences.getAll();

        for (String prefKey : allPreferences.keySet()) {
            Object item = allPreferences.get(prefKey);

            if (item instanceof String) {
                Preference preference = getPreferenceScreen().findPreference(prefKey);
                updateSummary(preferences, prefKey, preference);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public PreferenceActivity getPreferenceActivity() {
        return this;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preferenceKey) {
        Preference preference = getPreferenceScreen().findPreference(preferenceKey);

        if (preference instanceof EditTextPreference) {
            updateSummary(sharedPreferences, preferenceKey, preference);
        }

    }

    private void updateSummary(SharedPreferences sharedPreferences, String preferenceKey, Preference preference) {
        if (preference == null) {
            return;
        }

        String preferenceValue = sharedPreferences.getString(preferenceKey, null);

        if (preferenceValue != null) {
            preference.setSummary(preferenceValue);
        }
    }
}