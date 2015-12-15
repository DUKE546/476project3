package edu.msu.hlavaty1.fire.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import edu.msu.hlavaty1.fire.fire.Fire;
import edu.msu.hlavaty1.fire.fire.MapManipulator;
import edu.msu.hlavaty1.fire.R;
import edu.msu.hlavaty1.fire.util.Cloud;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class NewReportDlg extends DialogFragment {

    private AlertDialog dlg;
    private LatLng location;
    private View view;
    private FrameLayout callingView;

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
        view = inflater.inflate(R.layout.new_fire_report, null);
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
        fire.setExtinguished(false);
        fire.setLatLng(location);

        final LoadingDlg loadingDlg = new LoadingDlg();
        loadingDlg.show(getActivity().getFragmentManager(), "submitting");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud(view.getContext());
                final String id = cloud.saveFireToCloud(fire);
                if (id == null) {
                    callingView.post(new Runnable() {
                        @Override
                        public void run() {
                            // If we fail to register, display a toast
                            Toast.makeText(view.getContext(), R.string.report_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    fire.setId(Integer.parseInt(id));

                    callingView.post(new Runnable() {
                        @Override
                        public void run() {
                            mapManipulator.addMarkerAndMove(location, fire);
                            Toast.makeText(view.getContext(), R.string.report_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                loadingDlg.dismiss();
            }
        }).start();
    }

    private String getDescription() {
        return ((EditText) view.findViewById(R.id.editDescriptionText)).getText().toString();
    }

    private String getFurniture() {
        return ((EditText) view.findViewById(R.id.editFurnitureType)).getText().toString();
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setMapManipulator(MapManipulator mapManipulator) {
        this.mapManipulator = mapManipulator;
    }

    public void setCallingView(FrameLayout view) { this.callingView = view; }
}
