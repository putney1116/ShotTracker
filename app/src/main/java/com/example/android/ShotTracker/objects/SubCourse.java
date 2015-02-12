package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold SubCourse information
 */
public class SubCourse {

    private int mID = -1;
    private int mCourseID = -1;
    private String mName = "";
    private double mRating = -1;
    private List<CourseHole> mCourseHoleList = null;

    /**
     * Constructors
     */
    public SubCourse() {}

    public SubCourse(int id, Course course, String name, double rating) {
        this.mID = id;
        this.mCourseID = course.getID();
        this.mName = name;
        this.mRating = rating;
    }

    public SubCourse(Course course, String name, double rating) {
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

    public void setRating(double rating) { this.mRating = rating; }

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

    public double getRating() { return this.mRating; }

    /**
     * List of CourseHole objects
     */
    public void addCourseHole(CourseHole courseHole) {
        if (mCourseHoleList == null) {
            mCourseHoleList = new ArrayList<CourseHole>();
        }
        mCourseHoleList.add(courseHole);
    }

    public List<CourseHole> getCourseHoleList() { return this.mCourseHoleList; }
}
