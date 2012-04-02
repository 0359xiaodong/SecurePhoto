package eu.tpmusielak.securephoto.verification;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 20:43
 */
public abstract class BasicVerifier implements Verifier {
    private VerifierState state = VerifierState.UNINITIALIZED;

    @Override
    public final void initialize() {
        state = VerifierState.INITIALIZING;
        state = this.onInitialize();
    }

    protected abstract VerifierState onInitialize();

    @Override
    public final VerifierState getState() {
        return state;
    }

}
