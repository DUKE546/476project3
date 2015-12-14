package edu.msu.hlavaty1.fire.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.evanhlavaty.fire.gcm.registration.Registration;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import edu.msu.hlavaty1.fire.util.Cloud;

public class RegistrationTask extends AsyncTask<Void, Void, String> {
    private static Registration regService = null;
    private GoogleCloudMessaging gcm;
    private Context context;

    private static final String SENDER_ID = "829579373785";

    private Cloud cloud;

    public RegistrationTask(Context context) {
        this.context = context;
        this.cloud = new Cloud(context);
    }

    @Override
    protected String doInBackground(Void... params) {
        if (regService == null) {
            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl("https://fire-1159.appspot.com/_ah/api/");

            regService = builder.build();
        }

        String msg;
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            String deviceToken = gcm.register(SENDER_ID);
            msg = deviceToken;

            regService.register(deviceToken).execute();

        } catch (IOException ex) {
            ex.printStackTrace();
            msg = "Error: " + ex.getMessage();
        }
        return msg;
    }

    @Override
    protected void onPostExecute(final String token) {
        // TODO: uncomment when finished
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                cloud.registerDeviceToCloud(token);
//            }
//        }).start();
//
        Log.i("DEVICE TOKEN", token);
    }
}
