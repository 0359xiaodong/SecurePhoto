package eu.tpmusielak.securephoto.verification;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 20:39
 */
public interface VerifierGUIReceiver {
    Context getBaseContext();

    Context getContext();

    void runOnUiThread(Runnable runnable);

    View findViewById(int iconID);

    ViewGroup getPluginsPane();

    void startBackgroundOperation();

    void endBackgroundOperation();
}
