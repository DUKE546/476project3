package edu.msu.hlavaty1.fire.map;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class Fire {
    private Integer id;
    private String furniture;
    private String description;
    private LatLng latLng;
    private Boolean extinguished;

    public static Fire fromXML(XmlPullParser xml) {
        Fire fire = new Fire();

        fire.setId(Integer.parseInt(xml.getAttributeValue(null, "id")));
        fire.setFurniture(xml.getAttributeValue(null, "furniture"));
        fire.setDescription(xml.getAttributeValue(null, "description"));
        fire.setLatLng(new LatLng(Double.parseDouble(xml.getAttributeValue(null, "lat")),
                Double.parseDouble(xml.getAttributeValue(null, "lng"))));

        if (xml.getAttributeValue(null, "extinguished").equals("false")) {
            fire.setExtinguished(false);
        } else {
            fire.setExtinguished(true);
        }

        return fire;
    }

    public void toXML(XmlSerializer xml) throws IOException {
        xml.attribute(null, "furniture", furniture);
        xml.attribute(null, "description", description);
        xml.attribute(null, "lat", Double.toString(latLng.latitude));
        xml.attribute(null, "lng", Double.toString(latLng.longitude));
        xml.attribute(null, "extinguished", extinguished.toString());
    }

    public String getFurniture() {
        return furniture;
    }

    public void setFurniture(String furniture) {
        this.furniture = furniture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public boolean isExtinguished() {
        return extinguished;
    }

    public void setExtinguished(boolean extinguished) {
        this.extinguished = extinguished;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
