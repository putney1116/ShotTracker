package com.example.android.ShotTracker.objects;

import java.util.Vector;

/**
 * Class holding Shot information
 */
public class Shot {

    private int mID = -1;
    private int mRoundHoleID = -1;
    private int mClubID = -1;
    private Vector<ShotType> mShotType;
    private float mYards = -1;
    private float mShotStartLat = -1;
    private float mShotStartLong = -1;
    private float mShotEndLat = -1;
    private float mShotEndLong = -1;

    /**
     * Default Constructor
     */
    public Shot() {
        mShotType = new Vector<ShotType>();
    }

    public Shot(int id, RoundHole roundhole, Club club,
                Vector<ShotType> shottype, float yds, float sslat,
                float sslong, float selat, float selong){
        this.mID = id;
        this.mRoundHoleID = roundhole.getID();
        this.mClubID = club.getID();
        this.mShotType = shottype;
        this.mYards = yds;
        this.mShotStartLat = sslat;
        this.mShotStartLong = sslong;
        this.mShotEndLat = selat;
        this.mShotEndLong = selong;
    }

    public Shot(RoundHole roundhole, Club club,
                Vector<ShotType> shottype, float yds, float sslat,
                float sslong, float selat, float selong){
        this.mRoundHoleID = roundhole.getID();
        this.mClubID = club.getID();
        this.mShotType = shottype;
        this.mYards = yds;
        this.mShotStartLat = sslat;
        this.mShotStartLong = sslong;
        this.mShotEndLat = selat;
        this.mShotEndLong = selong;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setRoundHoleID(RoundHole roundhole) {
        this.mRoundHoleID = roundhole.getID();
    }

    public void setClubID(Club club) {
        this.mClubID = club.getID();
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

    public void setShotEndLat(float lat) {
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

    public void addShotType(ShotType shottype) {
        this.mShotType.addElement(shottype);
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

    public Vector<ShotType> getShotTypeList() {
        return this.mShotType;
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
