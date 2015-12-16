package edu.msu.hlavaty1.fire.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import edu.msu.hlavaty1.fire.fire.MapManipulator;
import edu.msu.hlavaty1.fire.R;
import edu.msu.hlavaty1.fire.gcm.GCMIntentService;
import edu.msu.hlavaty1.fire.gcm.RegistrationTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ActiveListener activeListener = new ActiveListener();
    private LocationManager locationManager = null;

    private SensorManager sensorManager = null;
    private Sensor accelSensor = null;
    private AccelListener accelListener = null;

    private GoogleMap map;
    private LatLng currentLocation;
    private MapManipulator mapManipulator;

    private BroadcastReceiver receiver;
    public static final String RECEIVE = "edu.msu.hlavaty1.fire.ui.MapsActivity.receive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get the location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        new RegistrationTask(this).execute();
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(RECEIVE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getExtras().getString(GCMIntentService.ACTION_KEY, GCMIntentService.REFRESH_CASE);

                if (action.equals(GCMIntentService.REFRESH_CASE)) {
                    mapManipulator.populateFiresOnMap();
                }
            }
        };

        registerReceiver(receiver, intentFilter);

        sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelSensor != null) {
            accelListener = new AccelListener(); sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        registerListeners();
    }

    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        unregisterListeners();
        unregisterReceiver(receiver);

        if(accelSensor != null) {
            sensorManager.unregisterListener(accelListener); accelListener = null;
            accelSensor = null;
        }

        super.onPause();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        mapManipulator =  new MapManipulator(map, this);
    }

    private void setCurrentLocation() {
        CameraUpdate center = CameraUpdateFactory.newLatLng(currentLocation);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.moveCamera(center);
        map.animateCamera(zoom);

        mapManipulator.setCurrentLocation(currentLocation);
    }

    private void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if(bestAvailable != null) {
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            if (location != null) {
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                currentLocation = new LatLng(lat, lng);
            }
            else {
                currentLocation = new LatLng(-6.340815, -61.394275);
            }
        }
    }

    private void unregisterListeners() {
        try {
            locationManager.removeUpdates(activeListener);
        }
        catch (SecurityException e) {
            // Fail silently
        }
    }

    private class ActiveListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            setCurrentLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            registerListeners();
        }
    };

    private class AccelListener implements SensorEventListener {

        float[] gravity = {0, 0, 0};
        float[] linear_acceleration = new float[3];

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float kFilteringFactor=0.6f;

            gravity[0] = (event.values[0] * kFilteringFactor) + (gravity[0] * (1.0f - kFilteringFactor));
            gravity[1] = (event.values[1] * kFilteringFactor) + (gravity[1] * (1.0f - kFilteringFactor));
            gravity[2] = (event.values[2] * kFilteringFactor) + (gravity[2] * (1.0f - kFilteringFactor));

            linear_acceleration[0] = (event.values[0] - gravity[0]);
            linear_acceleration[1] = (event.values[1] - gravity[1]);
            linear_acceleration[2] = (event.values[2] - gravity[2]);

            float magnitude;
            magnitude = (float)Math.sqrt(linear_acceleration[0]*linear_acceleration[0]+linear_acceleration[1]*linear_acceleration[1]+linear_acceleration[2]*linear_acceleration[2]);
            magnitude = Math.abs(magnitude);

            if (mapManipulator != null) {
                if (magnitude < 0.01 && mapManipulator.inDanger()) {
                    SinglularToast.makeText(getBaseContext(), R.string.danger, Toast.LENGTH_SHORT).show();
                } else {
                    SinglularToast.makeText(getBaseContext(), R.string.danger, Toast.LENGTH_SHORT).cancel();
                }
            }
        }

    }
}
