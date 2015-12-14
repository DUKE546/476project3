package edu.msu.hlavaty1.fire;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class Fire {
    private int id;
    private String furniture;
    private String description;
    private LatLng latLng;
    private Boolean extinguished;

    public void toXML(XmlSerializer xml) throws IOException {
        xml.attribute(null, "furniture", furniture);
        xml.attribute(null, "description", description);
        xml.attribute(null, "latLng", latLng.toString());
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
