package eu.tpmusielak.securephoto.verification.geo;

import eu.tpmusielak.securephoto.verification.VerificationFactorData;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 29/03/12
 * Time: 14:08
 */
public class GeolocationData implements VerificationFactorData, Serializable {
    public final double latitude;
    public final double longitude;
    public final double altitude;

    public final long fixtime;

    public final float accuracy;

    public final byte[] dataHash;

    public GeolocationData(long fixtime, double latitude, double longitude, double altitude, float accuracy) {
        this.fixtime = fixtime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;

        dataHash = ByteBuffer.allocate(4 * 8 + 4)
                .putDouble(latitude)
                .putDouble(longitude)
                .putDouble(altitude)
                .putLong(fixtime)
                .putFloat(accuracy).array();
    }

    public GeolocationData(long fixtime, double latitude, double longitude, double altitude) {
        this(fixtime, latitude, longitude, altitude, 0);
    }

    @Override
    public byte[] getHash() {
        return dataHash;
    }

    @Override
    public String toString() {
        Date fixDate = new Date(fixtime);
        final String format = "Geolocation Data:\n" +
                "    Fix acquired: %s\n" +
                "    Latitude: %f\n" +
                "    Longitude: %f\n" +
                "    Altitude: %f\n" +
                "    Accuracy: +/- %f m";

        return String.format(format, fixDate.toString(), latitude, longitude, altitude, accuracy);
    }
}
