package eu.tpmusielak.securephoto.container;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 03:08
 */
public interface VerificationFactor {
    public final String digestAlgorithm = "SHA-1";

    public byte [] getHash();
}
