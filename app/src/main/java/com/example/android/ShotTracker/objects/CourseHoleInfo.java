package com.example.android.ShotTracker.objects;

/**
 * Class to hold (lat, long) point of interest for a CourseHole
 */
public class CourseHoleInfo {

    private int mID = -1;
    private CourseHole mCourseHole;
    private String mInfo = "";
    private float mLat = -1;
    private float mLong = -1;

    /**
     * Constructors
     */
    public CourseHoleInfo () {}

    public CourseHoleInfo(int id, CourseHole coursehole, String info,
                          float latitude, float longitude) {
        this.mID = id;
        this.mCourseHole = coursehole;
        this.mInfo = info;
        this.mLat = latitude;
        this.mLong = longitude;
    }

    public CourseHoleInfo(CourseHole coursehole, String info,
                          float latitude, float longitude) {
        this.mCourseHole = coursehole;
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

    public void setCourseHoleID(CourseHole coursehole) {
        this.mCourseHole.setID(coursehole.getID());
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
        return this.mCourseHole.getID();
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
