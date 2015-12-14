package edu.msu.hlavaty1.fire.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import edu.msu.hlavaty1.fire.R;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class SubmitLoadingDlg extends DialogFragment {

    /**
     * Create the dialog box
     */
    @Override
    public Dialog onCreateDialog(Bundle bundle) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle(R.string.send_report);

        // Create the dialog box
        final AlertDialog dlg = builder.create();
        return dlg;
    }
}
