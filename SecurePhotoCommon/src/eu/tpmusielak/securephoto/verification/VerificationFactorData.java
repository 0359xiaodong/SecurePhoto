package eu.tpmusielak.securephoto.verification;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 12:57
 */
public interface VerificationFactorData {
    //
    // !! Make sure that the implementing interface is Serializable !!
    //

    public byte[] getHash();
}
