package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.securephoto.container.SPImage;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 14:42
 */
public class DummyVerifier extends BasicVerifier {

    private int startupdelay;

    public DummyVerifier() {
        this(0);
    }

    public DummyVerifier(int startupdelay) {
        this.startupdelay = startupdelay;
    }

    @Override
    protected VerifierState onInitialize() {
        try {
            Thread.sleep(startupdelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return VerifierState.INIT_SUCCESS;
    }

    @Override
    public VerificationFactorData onCapture(SPImage image) {
        return null;
    }

    @Override
    public String toString() {
        return String.format("DummyVerifier(%d)", startupdelay);
    }
}
