package edu.msu.hlavaty1.fire.util;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import edu.msu.hlavaty1.fire.fire.Fire;

public class Cloud {
    private static final String MAGIC = "TechItHa6RuzeM8";

    private static final String SAVE_FIRE_URL = "http://webdev.cse.msu.edu/~chuppthe/cse476/fire/fire-save.php";
    private static final String GET_FIRES_URL = "http://webdev.cse.msu.edu/~chuppthe/cse476/fire/fire-load.php";
    private static final String REGISTER_DEVICE_URL = "http://webdev.cse.msu.edu/~chuppthe/cse476/fire/fire-register-device.php";
    private static final String SAVE_EXTINGUISHED_URL = "http://webdev.cse.msu.edu/~chuppthe/cse476/fire/fire-save-extinguished.php";

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
     * URL: fire-load.php
     * PARAMS:
     *      magic: Server Magic
     *
     * @return XmlPullParser of all fires in DB
     */
    public XmlPullParser loadFiresFromCloud() {
        String query = GET_FIRES_URL + "?magic=" + MAGIC + "&magic=" + MAGIC;

        InputStream stream = null;
        XmlPullParser reports = null;
        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            stream = conn.getInputStream();

            /**
             * Create an XML parser for the result
             */
            try {
                XmlPullParser xmlR = Xml.newPullParser();
                xmlR.setInput(stream, UTF8);

                xmlR.nextTag();      // Advance to first tag
                xmlR.require(XmlPullParser.START_TAG, null, "fire");

                String status = xmlR.getAttributeValue(null, "status");
                if (status.equals("no")) {
                    return null;
                }

                reports = xmlR;

            } catch (XmlPullParserException e) {
                return null;
            }
        } catch (MalformedURLException e) {
            // Should never happen
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Fail silently
                }
            }
        }

        return reports;
    }

    /**
     * Saves a fire to the cloud
     * This should run in a thread!!
     *
     * URL: fire-save.php
     * PARAMS:
     *      magic: Server Magic
     *      lat: latitude
     *      long: longitude
     *
     * @param fire the fire to save to the cloud
     * @return fireId if the save was successful
     */
    public String saveFireToCloud(Fire fire) {
        String fireId = null;

        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startTag(null, "report");

            fire.toXML(xml);

            xml.endTag(null, "report");

            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return null;
        }

        final String xmlStr = writer.toString();

        /*
         * Convert the XML into HTTP POST data
         */
        String postDataStr;
        try {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, UTF8);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();

        String query = SAVE_FIRE_URL + "&magic=" + MAGIC;

        InputStream stream = null;
        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);

            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            stream = conn.getInputStream();

            /**
             * Create an XML parser for the result
             */
            try {
                XmlPullParser xmlR = Xml.newPullParser();
                xmlR.setInput(stream, UTF8);

                xmlR.nextTag();      // Advance to first tag
                xmlR.require(XmlPullParser.START_TAG, null, "fire");

                String status = xmlR.getAttributeValue(null, "status");
                if (status.equals("no")) {
                    return null;
                }

                fireId = xmlR.getAttributeValue(null, "id");

                // We are done
            } catch (XmlPullParserException ex) {
                return null;
            } catch (IOException ex) {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Fail silently
                }
            }
        }

        return fireId;
    }

    /**
     * Register a device to the cloud.
     * This should be run in a thread
     *
     * URL: fire-register-device.php
     * PARAMS:
     *      magic: Server Magic
     *      device: the device token
     *
     * @param deviceToken device token to register
     * @return true if register is successful
     */
    public boolean registerDeviceToCloud(String deviceToken) {
        String query = REGISTER_DEVICE_URL + "?device=" + deviceToken + "&magic=" + MAGIC;

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
                xmlR.require(XmlPullParser.START_TAG, null, "fire");

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
     * Change extinguished state
     * This should be run in a thread
     *
     * URL: fire-save-extinguished.php
     * PARAMS:
     *      magic: Server Magic
     *      id: id of the fire
     *      status: (bool) if the fire is still lit
     *
     * @param fire fire to update
     * @return true if save is successful
     */
    public boolean updateExtinguishedToCloud(Fire fire) {
        String query = SAVE_EXTINGUISHED_URL + "?id=" + fire.getId() + "&extinguished=" + fire.isExtinguished() + "&magic=" + MAGIC;

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
                xmlR.require(XmlPullParser.START_TAG, null, "fire");

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
