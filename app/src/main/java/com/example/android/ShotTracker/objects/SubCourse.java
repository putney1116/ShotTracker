package com.example.android.ShotTracker.objects;

/**
 * Class to hold SubCourse information
 */
public class SubCourse {

    private int mID = -1;
    private Course mCourse;
    private String mName = "";
    private float mRating = -1;

    //\todo Add private member for list of CourseHole objects and setters, getters, constructors

    /**
     * Constructors
     */
    public SubCourse() {}

    public SubCourse(int id, Course course, String name, float rating) {
        this.mID = id;
        this.mCourse = course;
        this.mName = name;
        this.mRating = rating;
    }

    public SubCourse(Course course, String name, float rating) {
        this.mCourse = course;
        this.mName = name;
        this.mRating = rating;
    }

    public SubCourse(Course course, String name) {
        this.mCourse = course;
        this.mName = name;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setCourseID(Course course) {
        this.mCourse.setID(course.getID());
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
        return this.mCourse.getID();
    }

    public String getName() {
        return this.mName;
    }

    public float getRating() { return this.mRating; }
}
