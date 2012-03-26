package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.securephoto.container.SPImage;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 21:50
 */
public class DummyVerifier2 implements VerificationFactor {


    @Override
    public void onCreate() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VerificationFactorData onCapture(SPImage image) {
        return null;
    }

    @Override
    public String toString() {
        return "DummyVerifier2";
    }
}
