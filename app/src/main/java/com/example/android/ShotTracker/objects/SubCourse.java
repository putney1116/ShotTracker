package com.example.android.ShotTracker.objects;

/**
 * Class to hold SubCourse information
 */
public class SubCourse {

    private int mID = -1;
    private int mCourseID = -1;
    private String mName = "";
    private float mRating = -1;

    //\todo Add private member for list of CourseHole objects and setters, getters, constructors

    /**
     * Constructors
     */
    public SubCourse() {}

    public SubCourse(int id, Course course, String name, float rating) {
        this.mID = id;
        this.mCourseID = course.getID();
        this.mName = name;
        this.mRating = rating;
    }

    public SubCourse(Course course, String name, float rating) {
        this.mCourseID = course.getID();
        this.mName = name;
        this.mRating = rating;
    }

    public SubCourse(Course course, String name) {
        this.mCourseID = course.getID();
        this.mName = name;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setCourseID(Course course) {
        this.mCourseID = course.getID();
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setRating(float rating) { this.mRating = rating; }

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

    public float getRating() { return this.mRating; }
}
