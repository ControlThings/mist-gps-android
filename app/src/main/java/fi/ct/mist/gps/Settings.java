package fi.ct.mist.gps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        serviceIntent = new Intent(this, GpsService.class);
        //serviceIntent.setAction("fi.ct.gps.service.GpsService");
        startService(serviceIntent);
        Toast.makeText(getApplicationContext(), "startGpsService", Toast.LENGTH_LONG).show();
    }
}
