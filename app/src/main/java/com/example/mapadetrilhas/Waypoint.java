package com.example.mapadetrilhas;

import android.location.Location;

public class Waypoint {
    private long id;
    private int idTrilha;
    private double latitude;
    private double longitude;

    public Waypoint() { }

    public Waypoint(int idTrilha, double latitude, double longitude) {
        this.idTrilha = idTrilha;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getIdTrilha() { return idTrilha; }
    public void setIdTrilha(int idTrilha) { this.idTrilha = idTrilha; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}