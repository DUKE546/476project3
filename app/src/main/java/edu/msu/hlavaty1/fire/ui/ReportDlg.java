package edu.msu.hlavaty1.fire.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import edu.msu.hlavaty1.fire.fire.Fire;
import edu.msu.hlavaty1.fire.R;
import edu.msu.hlavaty1.fire.util.Cloud;

/**
 * Created by evanhlavaty on 12/14/15.
 */
public class ReportDlg extends DialogFragment {

    private AlertDialog dlg;
    private Fire fire;
    private View view;

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
        view = inflater.inflate(R.layout.fire_report, null);
        builder.setView(view);

        // Add a cancel button
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Boolean checked = ((CheckBox) view.findViewById(R.id.checkExtinguished)).isChecked();
                        if (fire.isExtinguished() != checked) {
                            if (!new Cloud(view.getContext()).updateExtinguishedToCloud(fire)) {
                                view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(view.getContext(), R.string.extinguished_failed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                fire.setExtinguished(checked);
                            }
                        }
                    }
                }).start();
            }
        });

        dlg = builder.create();

        return dlg;
    }

    public void populate(Fire fire) {
        this.fire = fire;

        TextView furnitureText = (TextView) view.findViewById(R.id.textFurnitureType);
        furnitureText.setText(fire.getFurniture());

        TextView descriptionText = (TextView) view.findViewById(R.id.textDescription);
        descriptionText.setText(fire.getDescription());

        CheckBox extinguishedCheck = (CheckBox) view.findViewById(R.id.checkExtinguished);
        extinguishedCheck.setChecked(fire.isExtinguished());
    }
}