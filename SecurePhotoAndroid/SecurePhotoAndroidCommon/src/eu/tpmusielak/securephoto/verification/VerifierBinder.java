package eu.tpmusielak.securephoto.verification;

import android.content.Context;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 21:16
 */
public interface VerifierBinder {
    public void register(VerifierWrapper wrapper);

    public Context getApplicationContext();
}
