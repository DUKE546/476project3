package edu.msu.hlavaty1.fire;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by evanhlavaty on 12/13/15.
 */
public class Fire {
    private int id;
    private String furniture;
    private String description;
    private LatLng latLng;
    private boolean extinqished;

    public String toXML() {
        return "";
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

    public boolean isExtinqished() {
        return extinqished;
    }

    public void setExtinqished(boolean extinqished) {
        this.extinqished = extinqished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
