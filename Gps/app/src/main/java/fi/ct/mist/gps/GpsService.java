package fi.ct.mist.gps;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import mist.Mist;
import mist.node.EndpointBoolean;
import mist.node.EndpointFloat;
import mist.node.EndpointInt;
import mist.node.EndpointString;
import mist.node.NodeModel;


public class GpsService extends Service {

    public static String GPS_Service = "gps_service_reciver";

    private Intent mist;
    LocationManager locationManager = null;
    LocationListener locationListener;

    private EndpointFloat lon;
    private EndpointFloat lat;
    private EndpointFloat accuracy;
    private EndpointString name;
    private EndpointBoolean enabled;

    public static final String TAG = "GpsService";

    private int count = 0;

    int minTime = 10*1000; /* milliseconds minimum between updates */
    int minDist = 10; /* 10 meter change at minimum */

    @Override
    public void onCreate() {
        super.onCreate();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(GPS_Service));

        // Init Mist library
        mist = new Intent(this, Mist.class);

        // Create new Mist node
        NodeModel model = new NodeModel("GPS", this);

        // Create new endpoints (String id, String label)
        lon = new EndpointFloat("lon", "Longitude");
        lat = new EndpointFloat("lat", "Latitude");
        accuracy = new EndpointFloat("accuracy", "Accuracy");
        name = new EndpointString("name", "Alias");
        enabled = new EndpointBoolean("enabled", "GPS enabled");

        enabled.setReadable(true);
        enabled.update(false);
        enabled.setWritable(new EndpointBoolean.Writable() {
            @Override
            public void write(final boolean b) {
                // Handle write callback from Mist
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (Permissions.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                            if (b) {
                                enableGps();
                            } else {
                                disableGps();
                            }
                        } else {
                            enabled.update(false);
                        }
                    }
                });
            }
        });

        lon.setReadable(true);
        lat.setReadable(true);
        accuracy.setReadable(true);
        name.setReadable(true);

        enabled.addNext(lon);
        enabled.addNext(lat);
        enabled.addNext(accuracy);
        enabled.addNext(name);
        model.setRootEndpoint(enabled);

        if (Permissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            onAccess();
        }

        startService(mist);
    }


    private void onAccess() {
        enabled.update(true);
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
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                lon.update(location.getLongitude());
                lat.update(location.getLatitude());
            } else {
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDist, locationListener);
        } catch (SecurityException e) {
            Log.e(TAG, "LocationListener security exception: " + e);
            //ActivityCompat.requestPermissions(getApplicationContext(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 123);
        }

    }

    private void enableGps() throws SecurityException {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDist, locationListener);
        enabled.update(true);
    }

    private void disableGps() throws SecurityException {
        locationManager.removeUpdates(locationListener);
        enabled.update(false);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String alias = intent.getStringExtra("name");
            name.update(alias);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(mist);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
