package eu.tpmusielak.securephoto.verification.geo;

import eu.tpmusielak.securephoto.verification.VerificationFactorData;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 30/03/12
 * Time: 18:44
 */
public class CellularData implements VerificationFactorData, Serializable {
    public final int cid; // Cell ID
    public final int lac; // Location Area Code
    public final int mcc; // Mobile Country Code
    public final int mnc; // Mobile Network Code

    public final String simSerialNumber;

    public CellularData(int cid, int lac, int mcc, int mnc, String simSerialNumber) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        this.simSerialNumber = simSerialNumber;
    }

    @Override
    public byte[] getHash() {
        return ByteBuffer.allocate(4 * 4).putInt(cid).putInt(lac).putInt(mcc).putInt(mnc).array();
    }

    @Override
    public String toString() {
        final String format = "Cellular Data:\n" +
                "    SIM Serial: %s\n" +
                "    Cell ID:             %d\n" +
                "    Location Area Code:  %d\n" +
                "    Mobile Country Code: %d\n" +
                "    Mobile Network Code: %d";

        return String.format(format, simSerialNumber, cid, lac, mcc, mnc);
    }
}
