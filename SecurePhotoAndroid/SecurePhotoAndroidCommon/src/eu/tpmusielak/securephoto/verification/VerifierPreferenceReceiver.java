package eu.tpmusielak.securephoto.verification;

import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27/03/12
 * Time: 03:56
 */
public interface VerifierPreferenceReceiver {
    void addPreferencesFromResource(int preferenceID);

    PreferenceScreen getPreferenceScreen();

    PreferenceActivity getPreferenceActivity();
}
