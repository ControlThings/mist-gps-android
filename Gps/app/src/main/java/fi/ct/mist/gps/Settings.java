package fi.ct.mist.gps;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Settings extends AppCompatActivity {

    private Intent serviceIntent;
    private boolean permission;
    private int permisionId;
    EditText editText;

    LocalBroadcastManager localBroadcastManager;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intent = new Intent(GpsService.GPS_Service);

        setContentView(R.layout.activity_settings);

        editText = (EditText) findViewById(R.id.name);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    intent.putExtra("name", editText.getText().toString());
                    localBroadcastManager.sendBroadcast(intent);
                    return true;
                }
                return false;
            }
        });

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
