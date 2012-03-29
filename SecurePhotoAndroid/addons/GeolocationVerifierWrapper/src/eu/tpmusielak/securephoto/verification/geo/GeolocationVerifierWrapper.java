package eu.tpmusielak.securephoto.verification.geo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import eu.tpmusielak.securephoto.verification.VerifierBinder;
import eu.tpmusielak.securephoto.verification.VerifierWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27/03/12
 * Time: 23:56
 */
public class GeolocationVerifierWrapper extends VerifierWrapper implements GeolocationProvider, LocationListener {

    private LocationManager locationManager;

    private Location currentLocation;

    @Override
    public void register(VerifierBinder m) {
        super.register(m);
        locationManager = (LocationManager) manager.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    public GeolocationVerifierWrapper() {
        verifier = new GeolocationVerifier();
    }

    @Override
    public Drawable getDrawable() {
        return context.getResources().getDrawable(R.drawable.ic_stat_location);
    }

    @Override
    public int getPreferenceID() {
        return R.xml.geo_prefs;
    }

    @Override
    public String getName() {
        return context.getResources().getString(R.string.geolocation_verifier);
    }

    @Override
    public GeolocationData getLocation() {
        return new GeolocationData(currentLocation.getTime(), currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAccuracy());
    }


    /* Location listener methods */

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}
