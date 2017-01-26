package fi.ct.mist.gps;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Settings extends AppCompatActivity {

    private Intent serviceIntent;
    private boolean permission;
    private int permisionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Permissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            startGpsService();
        } else {
            permisionId = Permissions.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }

    }

    private void startGpsService() {
        serviceIntent = new Intent(this, GpsService.class);
        startService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permisionId == requestCode) {
            startGpsService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
