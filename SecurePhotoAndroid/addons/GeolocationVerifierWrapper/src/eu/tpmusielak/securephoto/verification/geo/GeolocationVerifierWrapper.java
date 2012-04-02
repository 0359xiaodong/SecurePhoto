package eu.tpmusielak.securephoto.verification.geo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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
        locationManager = (LocationManager) verifierManager.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onCameraStart() {
        super.onCameraStart();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            notifyNoGPS();

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        startFlashingIcon();
    }

    @Override
    public void onCameraExit() {
        stopFlashingIcon();

        locationManager.removeUpdates(this);
        super.onCameraExit();
    }

    public GeolocationVerifierWrapper() {
        verifier = new GeolocationVerifier(this);
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
        if (currentLocation == null) {
            //TODO: ensure to get location if locking settings is on
            //TODO: create a civilised way of giving stubbed-out verifier data
            return new GeolocationData(0, 0, 0, 0, 0);
        }
        return new GeolocationData(currentLocation.getTime(), currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAccuracy());
    }

    /**
     * GUI methods *
     */

    public void notifyNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(guiReceiver.getContext());

        final String dialogMessage = context.getResources().getString(R.string.gps_disabled_message);
        final String yes = context.getResources().getString(R.string.yes);
        final String no = context.getResources().getString(R.string.no);

        builder.setMessage(dialogMessage)
                .setCancelable(true)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent openGPSSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        guiReceiver.getContext().startActivity(openGPSSettingsIntent);
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /* Location listener methods */

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, currentLocation))
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


    /**
     * *************************************
     * <p/>
     * Code from http://developer.android.com/guide/topics/location/obtaining-user-location.html
     * <p/>
     * *************************************
     */

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /*****
     *
     * End of external code
     *
     */


}
