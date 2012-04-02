package eu.tpmusielak.securephoto.verification.geo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import eu.tpmusielak.securephoto.verification.VerifierBinder;
import eu.tpmusielak.securephoto.verification.VerifierWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 30/03/12
 * Time: 18:45
 */
public class CellularVerifierWrapper extends VerifierWrapper implements CellularDataProvider {

    private TelephonyManager telephonyManager;

    public CellularVerifierWrapper() {
        verifier = new CellularVerifier(this);
    }

    @Override
    public void register(VerifierBinder m) {
        super.register(m);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public Drawable getDrawable() {
        return context.getResources().getDrawable(R.drawable.ic_stat_basestation);
    }

    @Override
    public int getPreferenceID() {
        return R.xml.cell_prefs;
    }

    @Override
    public String getName() {
        return context.getResources().getString(R.string.cellular_verifier);
    }

    /**
     * Cellular data extraction inspired by:
     * <p/>
     * https://labs.ericsson.com/developer-community/blog/create-simple-cell-id-look-application-android
     */

    @Override
    public CellularData getCellularData() {
        GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();

        final int cid = location.getCid();
        final int lac = location.getLac();

        final String networkOperator = telephonyManager.getNetworkOperator();
        final int mcc = Integer.parseInt(networkOperator.substring(0, 3));
        final int mnc = Integer.parseInt(networkOperator.substring(3));

        String simSerialNumber = telephonyManager.getSimSerialNumber();

        simSerialNumber = simSerialNumber == null ? "N/A" : simSerialNumber;

        return new CellularData(cid, lac, mcc, mnc, simSerialNumber);
    }
}
