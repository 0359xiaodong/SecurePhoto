package eu.tpmusielak.securephoto.verification;

import android.graphics.drawable.Drawable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 01:22
 */
public interface VerificationFactorWrapper {
    public VerificationFactor getVerificationFactor();

    public String getName();

    public Drawable getDrawable();

}
