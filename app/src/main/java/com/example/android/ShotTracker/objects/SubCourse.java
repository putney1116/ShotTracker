package com.example.android.ShotTracker.objects;

/**
 * Class to hold SubCourse information
 */
public class SubCourse {

    private int mID;
    private int mCourseID;
    private String mName;

    /**
     * Constructors
     */
    public SubCourse() {}

    public SubCourse(int id, int courseid, String name) {
        this.mID = id;
        this.mCourseID = courseid;
        this.mName = name;
    }

    public SubCourse(int courseid, String name) {
        this.mCourseID = courseid;
        this.mName = name;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setCourseID(int id) {
        this.mCourseID = id;
    }

    public void setName(String name) {
        this.mName = name;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public int getCourseID() {
        return this.mCourseID;
    }

    public String getName() {
        return this.mName;
    }
}
