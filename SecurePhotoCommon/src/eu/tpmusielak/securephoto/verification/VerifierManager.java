package eu.tpmusielak.securephoto.verification;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 21:09
 */
public interface VerifierManager {

    void register(Verifier v);

    List<Verifier> getVerifiers();
}
