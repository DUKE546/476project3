package edu.msu.hlavaty1.fire.map;

import android.location.Location;
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
import edu.msu.hlavaty1.fire.ui.MapsActivity;
import edu.msu.hlavaty1.fire.ui.NewReportDlg;
import edu.msu.hlavaty1.fire.ui.ReportDlg;
import edu.msu.hlavaty1.fire.util.Cloud;

public class MapManipulator {

    private final GoogleMap map;
    private final MapsActivity activity;
    private LatLng currentLocation;
    private ArrayList<Fire> fires = new ArrayList<>();
    private Cloud cloud;
    private FrameLayout view;

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
                for (Fire fire : fires) {
                    if (String.valueOf(fire.getId()).equals(arg0.getSnippet())) {
                        ReportDlg reportDlg = new ReportDlg();
                        reportDlg.populate(fire);
                        reportDlg.show(activity.getFragmentManager(), "report");
                    }
                }
                return true;
            }

        });

        map.setMyLocationEnabled(true);

        cloud = new Cloud(activity);

        view = (FrameLayout) activity.findViewById(R.id.map);

        currentLocation = new LatLng(-6.340815, -61.394275);

        //TODO: uncomment when server call complete
        //populateFiresOnMap();
    }

    private void promptForReport(LatLng location) {
        NewReportDlg newReportDlg = new NewReportDlg();
        newReportDlg.setLocation(location);
        newReportDlg.setMapManipulator(this);
        newReportDlg.show(activity.getFragmentManager(), "new report");
    }

    public void addFire(Fire fire) {
        fires.add(fire);
    }

    public void addMarkerAndMove(LatLng location, Integer id) {
        MarkerOptions marker = new MarkerOptions().position(location).snippet(Integer.toString(id));
        CameraUpdate center = CameraUpdateFactory.newLatLng(location);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.addMarker(marker);
        map.moveCamera(center);
        map.animateCamera(zoom);
    }


    public void addMarker(LatLng location, Integer id) {
        MarkerOptions marker = new MarkerOptions().position(location).snippet(Integer.toString(id));
        map.addMarker(marker);
    }

    public boolean inArea(LatLng tappedlocation) {
        float[] distance = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                tappedlocation.latitude, tappedlocation.longitude, distance);

        return distance[0] <= 91.44;
    }

    /**
     * This should get called on create and when a notification is received
     */
    public void populateFiresOnMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                XmlPullParser reports = cloud.getFiresFromCloud();

                try {
                    reports.nextTag();      // Advance to first tag
                    reports.require(XmlPullParser.START_TAG, null, "report");

                    String report = reports.getAttributeValue(null, "report");

                    while (report.equals("report")) {

                        final Fire fire = Fire.fromXML(reports);

                        addFire(fire);

                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                addMarker(fire.getLatLng(), fire.getId());
                            }
                        });

                        reports.nextTag();      // Advance to first tag
                        reports.require(XmlPullParser.START_TAG, null, "report");
                        report = reports.getAttributeValue(null, "report");
                    }
                } catch (XmlPullParserException | IOException xml) {
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

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }
}
