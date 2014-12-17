package com.example.android.ShotTracker.objects;

import java.util.Date;

/**
 * Class containing Round information
 */
public class Round {

    private int mID = -1;
    private SubCourse mSubCourse;
    private Date mDate;

    //\todo Add private member for list of RoundHole objects and setters, getters, constructors

    /**
     * Constructors
     */
    public Round() {}

    public Round(int id, SubCourse subcourse, Date date) {
        this.mID = id;
        this.mSubCourse = subcourse;
        this.mDate = date;
    }

    public Round(SubCourse subcourse, Date date) {
        this.mSubCourse = subcourse;
        this.mDate = date;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setSubCourseID(SubCourse subcourse) {
        this.mSubCourse.setID(subcourse.getID());
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public int getSubCourseID() {
        return this.mSubCourse.getID();
    }

    public Date getDate() {
        return this.mDate;
    }
}
