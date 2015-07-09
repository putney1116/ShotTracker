package com.example.android.ShotTracker.objects;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding Shot information
 */
public class Shot {

    private long mID = -1;
    private long mRoundHoleID = -1;
    private long mClubID = -1;
    private List<ShotType> mShotTypePre;
    private List<ShotType> mShotTypePost;
    private int mYards = -1;
    private double mShotStartLat = -1;
    private double mShotStartLong = -1;
    private double mShotEndLat = -1;
    private double mShotEndLong = -1;

    /**
     * Default Constructor
     */
    public Shot() {
        mShotTypePre = new ArrayList<ShotType>();
        mShotTypePost = new ArrayList<ShotType>();
    }

    /**
     * Setters
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setRoundHoleID(RoundHole roundhole) {
        this.mRoundHoleID = roundhole.getID();
    }

    public void setClubID(Club club) {
        this.mClubID = club.getID();
    }

    public void setYards(int yards) {
        this.mYards = yards;
    }

    public void setShotStartLat(double lat) {
        this.mShotStartLat = lat;
    }

    public void setShotStartLong(double longitude) {
        this.mShotStartLong = longitude;
    }

    public void setShotEndLat(double lat) {
        this.mShotEndLat = lat;
    }

    public void setShotEndLong(double longitude) {
        this.mShotEndLong = longitude;
    }

    public void setShotStartLatLong(double latitude, double longitude) {
        this.mShotStartLat = latitude;
        this.mShotStartLong = longitude;
    }

    public void setShotEndLatLong(double latitude, double longitude) {
        this.mShotEndLat = latitude;
        this.mShotEndLong = longitude;
    }

    public void addShotTypePre(ShotType shottypepre) { this.mShotTypePre.add(shottypepre); }

    public void addShotTypePost(ShotType shottypepost) {
        this.mShotTypePost.add(shottypepost);
    }

    /**
     * Getters
     */
    public long getID() {
        return this.mID;
    }

    public long getRoundHoleID() {
        return this.mRoundHoleID;
    }

    public long getClubID() {
        return this.mClubID;
    }

    public List<ShotType> getShotTypePreList() { return this.mShotTypePre; }

    public List<ShotType> getShotTypePostList() {
        return this.mShotTypePost;
    }

    public int getYards() {
        return this.mYards;
    }

    public double getShotStartLat() {
        return this.mShotStartLat;
    }

    public double getShotStartLong() {
        return this.mShotStartLong;
    }

    public double getShotEndLat() {
        return this.mShotEndLat;
    }

    public double getShotEndLong() {
        return this.mShotEndLong;
    }

    /**
     * clear the list of shot types
     */
    public void clearShotTypesPre() { mShotTypePre.clear(); }

    public void clearShotTypesPost() { mShotTypePost.clear(); }

    public Location getShotStartLocation() {
        Location location = new Location("");
        location.setLatitude(mShotStartLat);
        location.setLongitude(mShotStartLong);

        return location;
    }
    /**
     * Remove a shottype from the list
     * @param idx
     */
    public void removeShotTypePre(int idx) {
        //\todo add bounds checking
        mShotTypePre.remove(idx);
    }

    public void removeShotTypePost(int idx) {
        //\todo add bounds checking
        mShotTypePost.remove(idx);
    }

}
