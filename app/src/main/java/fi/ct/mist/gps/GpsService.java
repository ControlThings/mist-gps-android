package fi.ct.mist.gps;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import fi.ct.mist.mistnodeapi.Mist;
import fi.ct.mist.mistnodeapi.api.mistNode.DeviceModel;
import fi.ct.mist.mistnodeapi.api.mistNode.EndpointBoolean;
import fi.ct.mist.mistnodeapi.api.mistNode.EndpointFloat;

public class GpsService extends Service {

    private Intent mist;
    private final IBinder mBinder = new LocalBinder();
    LocationManager locationManager;
    LocationListener locationListener;

    public static final String TAG = "GpsService";

    public GpsService() {
    }

    public class LocalBinder extends Binder {
        GpsService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GpsService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mist = new Intent(this, Mist.class);

        Toast.makeText(getApplicationContext(), "onBind: GpsService", Toast.LENGTH_SHORT).show();

        DeviceModel model = new DeviceModel("GPS");

        final EndpointFloat lon = new EndpointFloat("lon", "Longitude");
        final EndpointFloat lat = new EndpointFloat("lat", "Latitude");
        final EndpointFloat accuracy = new EndpointFloat("accuracy", "Accuracy");

        final EndpointBoolean enabled = new EndpointBoolean("enabled", "GPS enabled");

        enabled.setReadable(true);
        enabled.setWritable(new EndpointBoolean.Writable() {
            @Override
            public void write(boolean b) {
                enabled.update(b);
                lon.update(-1);
                lat.update(-2);
                accuracy.update(-3);
            }
        });


        lon.setReadable(true);
        lat.setReadable(true);
        accuracy.setReadable(true);

        enabled.addNext(lon);
        enabled.addNext(lat);
        enabled.addNext(accuracy);
        model.setRootEndpoint(enabled);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lon.update(location.getLongitude());
                lat.update(location.getLatitude());
                accuracy.update(location.getAccuracy());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "LocationListener security exception. "+e.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "LocationListener security exception: " + e);
        }

        startService(mist);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(mist);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
