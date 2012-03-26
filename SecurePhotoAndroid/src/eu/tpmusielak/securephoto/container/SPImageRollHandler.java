package eu.tpmusielak.securephoto.container;

import eu.tpmusielak.securephoto.verification.VerificationFactor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 17:13
 */
public class SPImageRollHandler implements SPFileHandler {

    private List<VerificationFactor> verifiers;

    public SPImageRollHandler(List<VerificationFactor> verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    public String saveFile(byte[] bytes) {
        //TODO: Check auto-generated code
        return null;
    }
}
