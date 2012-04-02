package eu.tpmusielak.securephoto.verification.geo;

import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.verification.BasicVerifier;
import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.VerifierState;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27/03/12
 * Time: 23:50
 */
public class GeolocationVerifier extends BasicVerifier {

    private GeolocationProvider geolocationProvider;

    public GeolocationVerifier(GeolocationProvider geolocationProvider) {
        this.geolocationProvider = geolocationProvider;
    }

    @Override
    protected VerifierState onInitialize() {
        return VerifierState.INIT_SUCCESS;
    }

    @Override
    public VerificationFactorData onCapture(SPImage image) {
        return geolocationProvider.getLocation();
    }

    @Override
    public String toString() {
        return "GeolocaionVerifier";
    }
}
