package com.example.igoalone_mapboxapi_training.DAO;

import androidx.annotation.NonNull;

public class Cctv {

    String road_name;
    double latitude;
    double longitude;

    public String getRoad_name() {
        return road_name;
    }

    public void setRoad_name(String road_name) {
        this.road_name = road_name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return "latitude : " + latitude + ", longtitude : " + longitude;
    }
}
