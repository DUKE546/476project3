package edu.msu.hlavaty1.fire;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Cloud {
    private static final String MAGIC = "TechItHa6RuzeM8";

    private static final String SEND_LOCATION_URL = "";
    private static final String GET_LOCATIONS_URL = "";
    private static final String REGISTER_DEVICE_URL = "";


    private static final String UTF8 = "UTF-8";

    private Context context;

    public Cloud(Context context) {
        this.context = context;
    }

    /**
     * Skip the XML parser to the end tag for whatever
     * tag we are currently within.
     *
     * @param xml the parser
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void skipToEndTag(XmlPullParser xml) throws IOException, XmlPullParserException {
        int tag;
        do {
            tag = xml.next();
            if (tag == XmlPullParser.START_TAG) {
                // Recurse over any start tag
                skipToEndTag(xml);
            }
        } while (tag != XmlPullParser.END_TAG &&
                tag != XmlPullParser.END_DOCUMENT);
    }


    /**
     * Register user to the cloud.
     * This should run in a thread
     *
     * @return true if register is successful
     */
    public boolean getLocationsFromCloud() {
        String query = GET_LOCATIONS_URL + "&magic=" + MAGIC;

        InputStream stream = null;
        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            stream = conn.getInputStream();

            /**
             * Create an XML parser for the result
             */
            try {
                XmlPullParser xmlR = Xml.newPullParser();
                xmlR.setInput(stream, UTF8);

                xmlR.nextTag();      // Advance to first tag
                xmlR.require(XmlPullParser.START_TAG, null, "steam");

                String status = xmlR.getAttributeValue(null, "status");
                if (status.equals("no")) {
                    return false;
                }
            } catch (XmlPullParserException e) {
                return false;
            }
        } catch (MalformedURLException e) {
            // Should never happen
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Fail silently
                }
            }
        }

        return true;
    }

    /**
     * Send location to cloud
     * This should run in a thread
     *
     * @return true if register is successful
     */
    public boolean sendLocationToCloud(Fire fire) {
        String query = SEND_LOCATION_URL + "?fire=" + fire.toXML();

        InputStream stream = null;
        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            stream = conn.getInputStream();

            /**
             * Create an XML parser for the result
             */
            try {
                XmlPullParser xmlR = Xml.newPullParser();
                xmlR.setInput(stream, UTF8);

                xmlR.nextTag();      // Advance to first tag
                xmlR.require(XmlPullParser.START_TAG, null, "steam");

                String status = xmlR.getAttributeValue(null, "status");
                if (status.equals("no")) {
                    return false;
                }
            } catch (XmlPullParserException e) {
                return false;
            }
        } catch (MalformedURLException e) {
            // Should never happen
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Fail silently
                }
            }
        }

        return true;
    }

    /**
     * Register a device to the cloud.
     * This should be run in a thread
     *
     * @param deviceToken device token to register
     * @return true if register is successful
     */
    public boolean registerDeviceToCloud(String deviceToken) {
        String query = REGISTER_DEVICE_URL + "?device=" + deviceToken;

        InputStream stream = null;
        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            stream = conn.getInputStream();

            /**
             * Create an XML parser for the result
             */
            try {
                XmlPullParser xmlR = Xml.newPullParser();
                xmlR.setInput(stream, UTF8);

                xmlR.nextTag();      // Advance to first tag
                xmlR.require(XmlPullParser.START_TAG, null, "steam");

                String status = xmlR.getAttributeValue(null, "status");
                if (status.equals("no")) {
                    return false;
                }
            } catch (XmlPullParserException e) {
                return false;
            }
        } catch (MalformedURLException e) {
            // Should never happen
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Fail silently
                }
            }
        }

        return true;
    }
}
