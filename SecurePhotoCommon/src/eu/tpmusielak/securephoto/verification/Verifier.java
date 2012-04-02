package eu.tpmusielak.securephoto.verification;

import eu.tpmusielak.securephoto.container.SPImage;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 03:08
 */
public interface Verifier {

    public void initialize();

    public VerifierState getState();

    public VerificationFactorData onCapture(SPImage image);


}
