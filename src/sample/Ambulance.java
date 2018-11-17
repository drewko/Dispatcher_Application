package sample;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class Ambulance {
    private String id;
    private double longtitude;
    private double latitude;
    private boolean isFree;
    private double distance;
    private int time;



    public Ambulance(String id) {
        this.id = id;
        this.longtitude = 0.0;
        this.latitude = 0.0;
        this.isFree = false;
        this.distance=0.0;
    }

    public Ambulance(String id, double latitude, double longtitude) {
        this.id = id;
        this.longtitude = longtitude;
        this.latitude = latitude;

    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(Double longtitude) {
        this.longtitude = longtitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ambulance ambulance = (Ambulance) o;
        return id == ambulance.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public static String convertTime(int seconds)
    {
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        return p2 + ":" + p3 + ":" + p1;
    }

    @Override
    public String toString() {
        return "ID: " + id + " Distance: " + distance+" km  ETA: "+ convertTime(time);
    }
}
