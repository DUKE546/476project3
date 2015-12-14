package edu.msu.hlavaty1.fire;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class GCMIntentService extends IntentService {

    // Cases that match notification title
    public static final String REFRESH_CASE = "refresh_fires";
    public static final String ACTION_KEY = "action";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                String title = extras.getString("title");
                if (title == null) {
                    return;
                }

                switch (title) {
                    case REFRESH_CASE:
                        showToast(extras.getString("message"));
                        Intent refreshIntent = new Intent(MapsActivity.RECEIVE);
                        refreshIntent.putExtra(ACTION_KEY, REFRESH_CASE);
                        sendBroadcast(refreshIntent);
                        break;
                    default:
                        showToast("Unrecognized Notification");
                        break;
                }
            }
        }
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
