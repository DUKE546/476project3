package edu.msu.hlavaty1.fire;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class NewReportDlg extends DialogFragment {

    private AlertDialog dlg;
    private LatLng location;

    private MapManipulator mapManipulator;

    /**
     * Create the dialog box
     *
     * @param savedInstanceState The saved instance bundle
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle(R.string.new_location_title);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.new_fire_report, null);
        builder.setView(view);

        // Add a cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Cancel just closes the dialog box
            }
        });

        // Add a login button
        builder.setPositiveButton(R.string.report_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                create();
            }
        });

        dlg = builder.create();

        return dlg;
    }

    private void create() {
        if (!(getActivity() instanceof MapsActivity)) {
            return;
        }

        final Fire fire = new Fire();
        fire.setFurniture(getFurniture());
        fire.setDescription(getDescription());
        fire.setExtinqished(false);
        fire.setLatLng(location);

        final SubmitLoadingDlg submitLoadingDlg = new SubmitLoadingDlg();
        submitLoadingDlg.show(getActivity().getFragmentManager(), "submitting");

        final EditText view = (EditText) getActivity().findViewById(R.id.editDescriptionText);

        Runnable registerRunnable = new Runnable() {
            @Override
            public void run() {
                // Create a cloud object
                Cloud cloud = new Cloud(view.getContext());
                final boolean ok = cloud.sendLocationToCloud(fire);
                if (!ok) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            // If we fail to register, display a toast
                            Toast.makeText(view.getContext(), R.string.report_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    mapManipulator.addFire(fire);
                    mapManipulator.addMarkerAndMove(location);

                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.report_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                submitLoadingDlg.dismiss();
            }
        };

        new Thread(registerRunnable).start();
    }

    private String getDescription() {
        return ((EditText) getActivity().findViewById(R.id.editDescriptionText)).getText().toString();
    }

    private String getFurniture() {
        return ((EditText) getActivity().findViewById(R.id.editFurnitureType)).getText().toString();
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setMapManipulator(MapManipulator mapManipulator) {
        this.mapManipulator = mapManipulator;
    }
}
