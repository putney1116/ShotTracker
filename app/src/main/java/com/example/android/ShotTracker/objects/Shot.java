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
    private int mYards = -1;
    private double mShotStartLat = -1;
    private double mShotStartLong = -1;
    private double mShotEndLat = -1;
    private double mShotEndLong = -1;

    /**
     * Default Constructor
     */
    public Shot() {
        mShotType = new Vector<ShotType>();
    }

    public Shot(int id, RoundHole roundhole, Club club,
                Vector<ShotType> shottype, int yds, double sslat,
                double sslong, double selat, double selong){
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
                Vector<ShotType> shottype, int yds, double sslat,
                double sslong, double selat, double selong){
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
     * Function to calculate the yards between the shots start and end points.
     * Requires lat,long of start and end points to be set.
     */
    public void calcYards() {
        /// \todo Add code for calculating the yards based on lat, long
    }
}
