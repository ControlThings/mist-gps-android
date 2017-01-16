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

import java.util.Timer;
import java.util.TimerTask;

import mist.Mist;
import mist.node.EndpointBoolean;
import mist.node.EndpointFloat;
import mist.node.EndpointInt;
import mist.node.NodeModel;

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

    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Mist library
        mist = new Intent(this, Mist.class);

        // Create new Mist node
        NodeModel model = new NodeModel("GPS", this);

        // Create new endpoints (String id, String label)
        final EndpointFloat lon = new EndpointFloat("lon", "Longitude");
        final EndpointFloat lat = new EndpointFloat("lat", "Latitude");
        final EndpointFloat accuracy = new EndpointFloat("accuracy", "Accuracy");
        final EndpointInt counter = new EndpointInt("counter", "Dymmy Counter");

        final EndpointBoolean enabled = new EndpointBoolean("enabled", "GPS enabled");

        enabled.setReadable(true);
        enabled.setWritable(new EndpointBoolean.Writable() {
            @Override
            public void write(boolean b) {
                // Handle write callback from Mist
                enabled.update(b);
            }
        });


        lon.setReadable(true);
        lat.setReadable(true);
        accuracy.setReadable(true);
        counter.setReadable(true);

        enabled.addNext(lon);
        enabled.addNext(lat);
        enabled.addNext(accuracy);
        enabled.addNext(counter);
        model.setRootEndpoint(enabled);

        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                Log.i(TAG, "Tick...");
                counter.update(++count);
            }
        },0,1000);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Update values to Mist
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
