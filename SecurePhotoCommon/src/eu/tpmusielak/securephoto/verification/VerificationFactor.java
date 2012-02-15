package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.securephoto.container.SPImage;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 03:08
 */
public interface VerificationFactor {

    public void onCreate();
    public VerificationFactorData onCapture(SPImage image);
}
