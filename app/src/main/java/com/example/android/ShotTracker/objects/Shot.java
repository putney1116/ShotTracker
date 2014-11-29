package com.example.android.ShotTracker.objects;

import java.util.Vector;

/**
 * Class holding Shot information
 */
public class Shot {

    private int mID;
    private int mRoundHoleID;
    private int mClubID;
    private Vector<Integer> mShotTypeID;
    private float mYards;
    private float mShotStartLat;
    private float mShotStartLong;
    private float mShotEndLat;
    private float mShotEndLong;

    /**
     * Default Constructor
     */
    public Shot() {
        mShotTypeID = new Vector<Integer>();
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setRoundHoleID(int id) {
        this.mRoundHoleID = id;
    }

    public void setClubID(int id) {
        this.mClubID = id;
    }

    public void setYards(float yards) {
        this.mYards = yards;
    }

    public void setShotStartLat(float lat) {
        this.mShotStartLat = lat;
    }

    public void setShotStartLong(float longitude) {
        this.mShotStartLong = longitude;
    }

    public void seShotEndLat(float lat) {
        this.mShotEndLat = lat;
    }

    public void setShotEndLong(float longitude) {
        this.mShotEndLong = longitude;
    }

    public void setShotStartLatLong(float latitude, float longitude) {
        this.mShotStartLat = latitude;
        this.mShotStartLong = longitude;
    }

    public void setShotEndLatLong(float latitude, float longitude) {
        this.mShotEndLat = latitude;
        this.mShotEndLong = longitude;
    }

    public void addShotType(int id) {
        this.mShotTypeID.addElement(new Integer(id));
    }
    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public int getRoundHoleID() {
        return this.mRoundHoleID;
    }

    public int getClubID() {
        return this.mClubID;
    }

    public Vector<Integer> getShotTypeIDs() {
        return this.mShotTypeID;
    }

    public float getYards() {
        return this.mYards;
    }

    public float getShotStartLat() {
        return this.mShotStartLat;
    }

    public float getShotStartLong() {
        return this.mShotStartLong;
    }

    public float getShotEndLat() {
        return this.mShotEndLat;
    }

    public float getShotEndLong() {
        return this.mShotEndLong;
    }


    /**
     * Function to calculate the yards between the shots start and end points.
     * Requires lat,long of start and end points to be set.
     */
    public void calcYards() {
        /// \todo Add code for calculating the yards based on lat, long
    }
}
