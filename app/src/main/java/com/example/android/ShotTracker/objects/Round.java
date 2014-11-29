package com.example.android.ShotTracker.objects;

import java.util.Date;

/**
 * Class containing Round information
 */
public class Round {

    private int mID;
    private int mSubCourseID;
    private Date mDate;

    /**
     * Constructors
     */
    public Round() {}

    public Round(int id, int cid, Date date) {
        this.mID = id;
        this.mSubCourseID = cid;
        this.mDate = date;
    }

    public Round(int cid, Date date) {
        this.mSubCourseID = cid;
        this.mDate = date;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setSubCourseID(int id) {
        this.mSubCourseID = id;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    /** Getters
     *
     */
    public int getID() {
        return this.mID;
    }

    public int getSubCourseID() {
        return this.mSubCourseID;
    }

    public Date getDate() {
        return this.mDate;
    }
}
