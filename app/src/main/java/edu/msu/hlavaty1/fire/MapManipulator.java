package edu.msu.hlavaty1.fire;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class MapManipulator {

    private final GoogleMap map;
    private final MapsActivity activity;
    private LatLng currentLocation;
    private ArrayList<Fire> fires = new ArrayList<Fire>();

    public MapManipulator(GoogleMap googleMap, MapsActivity mapsActivity) {
        map = googleMap;
        activity = mapsActivity;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng location) {
                if (inArea(location)) {
                    promptForReport(location);
                } else {
                    Toast.makeText(activity, "Fire not in your general area", Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                // TODO: Get id from marker and look up matching fire from fires array
                // TODO: New fragment with fire object info
                return true;
            }

        });

        map.setMyLocationEnabled(true);

        populateFiresOnMap();
    }

    private void promptForReport(LatLng location) {
        NewReportDlg newLocationDlg = new NewReportDlg();
        newLocationDlg.setLocation(location);
        newLocationDlg.setMapManipulator(this);
        newLocationDlg.show(activity.getFragmentManager(), "report");
    }

    public void addFire(Fire fire) {
        fires.add(fire);
    }

    public void addMarkerAndMove(LatLng location) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(location);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.moveCamera(center);
        map.animateCamera(zoom);
    }


    public void addMarker(LatLng location) {
        MarkerOptions marker =  new MarkerOptions().position(location);
        map.addMarker(marker);
    }

    public boolean inArea(LatLng tappedlocation) {
        float[] distance = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                tappedlocation.latitude, tappedlocation.longitude, distance);
        Log.i("Difference", Float.toString(distance[0]));
        if (distance[0] <= 91.44) {
            return true;
        }

        return false;
    }

    /**
     * This should get called on create and when a notification is received
     */
    public void populateFiresOnMap() {
        // TODO: Call to server for all current fires
        // TODO: Build fire objects from xml
        // TODO: Call addFire and addMarker
        Log.i("REFRESH", "REFRESH");
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }
}

