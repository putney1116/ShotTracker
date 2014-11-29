package com.example.android.ShotTracker.objects;

/**
 * Class to hold (lat, long) point of interest for a CourseHole
 */
public class CourseHoleInfo {

    private int mID;
    private int mCourseHoleID;
    private String mInfo;
    private float mLat;
    private float mLong;

    /**
     * Constructors
     */
    public CourseHoleInfo () {}

    public CourseHoleInfo(int id, int hid, String info, float latitude, float longitude) {
        this.mID = id;
        this.mCourseHoleID = hid;
        this.mInfo = info;
        this.mLat = latitude;
        this.mLong = longitude;
    }

    public CourseHoleInfo(int hid, String info, float latitude, float longitude) {
        this.mCourseHoleID = hid;
        this.mInfo = info;
        this.mLat = latitude;
        this.mLong = longitude;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setCourseHoleID(int id) {
        this.mCourseHoleID = id;
    }

    public void setInfo(String info) {
        this.mInfo = info;
    }

    public void setLatitude(float latitude) {
        this.mLat = latitude;
    }

    public void setLongitude(float longitude) {
        this.mLong = longitude;
    }

    public void setLatLong(float latitude, float longitude) {
        this.mLat = latitude;
        this.mLong = longitude;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public int getCourseHoleID() {
        return this.mCourseHoleID;
    }

    public String getInfo() {
        return this.mInfo;
    }

    public float getLatitude() {
        return this.mLat;
    }

    public float getLongitude() {
        return this.mLong;
    }

}
