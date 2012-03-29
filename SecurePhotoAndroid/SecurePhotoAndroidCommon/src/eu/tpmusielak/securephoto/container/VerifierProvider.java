package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.verification.Verifier;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 29/03/12
 * Time: 17:11
 */
public interface VerifierProvider {
    public List<Verifier> getVerifiers();
}
