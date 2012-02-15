package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.securephoto.container.SPImage;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 14:42
 */
public class DummyVerifier implements VerificationFactor{
    @Override
    public void onCreate() {
    }

    @Override
    public VerificationFactorData onCapture(SPImage image) {
        return null;
    }
}
