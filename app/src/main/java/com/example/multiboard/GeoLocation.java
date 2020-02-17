package com.example.multiboard;

//hold the locations to impute into GeoFence
public class GeoLocation {
    private String name;
    private double lat;
    private double lon;
    private double radius;

    public GeoLocation(String name, double lat, double lon, double radius){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getRadius(){
        return radius;
    }
}
