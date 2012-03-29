package eu.tpmusielak.securephoto.verification;

import android.graphics.drawable.Drawable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27/03/12
 * Time: 00:04
 */
public class TimestampVerifierWrapper extends VerifierWrapper {
    public TimestampVerifierWrapper() {
        verifier = new RFC3161Timestamp("http://www.cryptopro.ru/tsp/tsp.srf");
    }

    @Override
    public Drawable getDrawable() {
        return context.getResources().getDrawable(R.drawable.ic_stat_clock);
    }

    @Override
    public int getPreferenceID() {
        return R.xml.ts_preferences;
    }

}
