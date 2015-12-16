package edu.msu.hlavaty1.fire.fire;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import edu.msu.hlavaty1.fire.R;
import edu.msu.hlavaty1.fire.ui.LoadingDlg;
import edu.msu.hlavaty1.fire.ui.MapsActivity;
import edu.msu.hlavaty1.fire.ui.NewReportDlg;
import edu.msu.hlavaty1.fire.ui.ReportDlg;
import edu.msu.hlavaty1.fire.util.Cloud;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class MapManipulator implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private final GoogleMap map;
    private final MapsActivity activity;
    private LatLng currentLocation;
    private ArrayList<Fire> fires = new ArrayList<Fire>();
    private Cloud cloud;
    private FrameLayout view;

    public MapManipulator(GoogleMap googleMap, MapsActivity mapsActivity) {
        map = googleMap;
        activity = mapsActivity;

        map.setOnMapClickListener(this);

        map.setOnMarkerClickListener(this);

        map.setMyLocationEnabled(true);

        cloud = new Cloud(activity);

        view = (FrameLayout) activity.findViewById(R.id.map);

        currentLocation = new LatLng(-6.340815, -61.394275);

        populateFiresOnMap();
    }

    /**
     * handle map click event
     */
    @Override
    public void onMapClick(LatLng location) {
        if (inArea(location)) {
            promptForReport(location);
        } else {
            Toast.makeText(activity, "Fire not in your general area", Toast.LENGTH_SHORT).show();
        }
        //promptForReport(location);
    }

    /**
     * handle marker click event
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        for (Fire fire : fires) {
            if (fire.getId().equals(Integer.parseInt(marker.getTitle()))) {
                Bundle bundle = new Bundle();
                bundle.putString(Fire.FURNITURE, fire.getFurniture());
                bundle.putString(Fire.DESCRIPTION, fire.getDescription());
                bundle.putBoolean(Fire.EXTINGUISHED, fire.isExtinguished());

                ReportDlg reportDlg = new ReportDlg();
                reportDlg.setArguments(bundle);
                reportDlg.setFire(fire);
                reportDlg.show(activity.getFragmentManager(), "report");
                return true;
            }
        }
        return false;
    }

    private void promptForReport(LatLng location) {
        NewReportDlg newReportDlg = new NewReportDlg();
        newReportDlg.setLocation(location);
        newReportDlg.setMapManipulator(this);
        newReportDlg.setCallingView(view);
        newReportDlg.show(activity.getFragmentManager(), "new report");
    }

    public void addMarkerAndMove(LatLng location, Fire fire) {
        fires.add(fire);

        MarkerOptions marker = new MarkerOptions().position(location).title(Integer.toString(fire.getId()));
        CameraUpdate center = CameraUpdateFactory.newLatLng(location);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.addMarker(marker);
        map.moveCamera(center);
        map.animateCamera(zoom);
    }


    public void addMarker(LatLng location, Fire fire) {
        fires.add(fire);

        MarkerOptions marker =  new MarkerOptions().position(location).title(Integer.toString(fire.getId()));
        map.addMarker(marker);
    }

    public boolean inArea(LatLng tappedlocation) {
        float[] distance = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                tappedlocation.latitude, tappedlocation.longitude, distance);

        if (distance[0] <= 91.44) {
            return true;
        }

        return false;
    }

    /**
     * This should get called on create and when a notification is received
     */
    public void populateFiresOnMap() {
        fires.clear();
        map.clear();

        final LoadingDlg loadingDlg = new LoadingDlg();
        loadingDlg.show(activity.getFragmentManager(), "populate map");

        new Thread(new Runnable() {
            @Override
            public void run() {
                XmlPullParser reports = cloud.loadFiresFromCloud();

                try {
                    reports.nextTag();      // Advance to first tag

                    String id = reports.getAttributeValue(null, "fire");
                    if (id != null) {
                        cloud.skipToEndTag(reports);

                        while (id != null) {

                            reports.nextTag();
                            reports.require(XmlPullParser.START_TAG, null, "report");

                            final Fire fire = Fire.fromXML(reports, id);

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    addMarker(fire.getLatLng(), fire);
                                }
                            });

                            cloud.skipToEndTag(reports);

                            reports.nextTag();
                            id = reports.getAttributeValue(null, "fire");

                            if (id != null) {
                                cloud.skipToEndTag(reports);
                            }
                        }
                    }
                    loadingDlg.dismiss();
                }
                catch (XmlPullParserException xml) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.populate_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (IOException io) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.populate_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    public boolean inDanger(){

        boolean inDanger = false ;

        for (Fire fire : fires){

          if( inArea(fire.getLatLng()) && !fire.isExtinguished()){
                inDanger = true ;
            }
        }

        return inDanger ;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

}

