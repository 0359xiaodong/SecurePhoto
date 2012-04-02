package eu.tpmusielak.securephoto.verification;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27/03/12
 * Time: 02:13
 */
public class BasicVerifierManager implements VerifierManager {
    private List<Verifier> verifiers;

    public BasicVerifierManager() {
        verifiers = new LinkedList<Verifier>();
    }

    @Override
    public void register(Verifier v) {
        verifiers.add(v);
    }

    @Override
    public List<Verifier> getVerifiers() {
        return this.verifiers;
    }
}
