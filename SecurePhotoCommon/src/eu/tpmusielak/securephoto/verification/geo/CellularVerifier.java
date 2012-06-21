package eu.tpmusielak.securephoto.verification.geo;

import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.verification.BasicVerifier;
import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.VerifierState;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 30/03/12
 * Time: 18:44
 */
public class CellularVerifier extends BasicVerifier {

    private CellularDataProvider cellularDataProvider;

    public CellularVerifier(CellularDataProvider cellularDataProvider) {
        this.cellularDataProvider = cellularDataProvider;
    }

    @Override
    protected VerifierState onInitialize() {
        if (cellularDataProvider != null) {
            return VerifierState.INIT_SUCCESS;
        } else {
            return VerifierState.INIT_FAILURE;
        }
    }

    @Override
    public VerificationFactorData onCapture(SPImage image) {
        return cellularDataProvider.getCellularData();
    }

    @Override
    public String toString() {
        return "CellularVerifier";
    }
}
