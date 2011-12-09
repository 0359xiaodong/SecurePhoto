package eu.tpmusielak.securephoto;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 27.10.11
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class ShowPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private SharedPreferences preferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

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