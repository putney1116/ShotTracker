package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class containing Round information
 */
public class Round {

    private long mID = -1;
    private long mSubCourseID = -1;
    private Date mDate;
    private List<RoundHole> mRoundHoleList = null;

    /**
     * Constructors
     */
    public Round() {}

    public Round(long id, SubCourse subcourse, Date date) {
        this.mID = id;
        this.mSubCourseID = subcourse.getID();
        this.mDate = date;
    }

    public Round(SubCourse subcourse, Date date) {
        this.mSubCourseID = subcourse.getID();
        this.mDate = date;
    }

    /**
     * Setters
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setSubCourseID(SubCourse subcourse) {
        this.mSubCourseID = subcourse.getID();
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    /**
     * Getters
     */
    public long getID() {
        return this.mID;
    }

    public long getSubCourseID() {
        return this.mSubCourseID;
    }

    public Date getDate() {
        return this.mDate;
    }

    /**
     * List of RoundHoles
     */
    public void addRoundHole(RoundHole roundHole) {
        if (mRoundHoleList == null) {
            mRoundHoleList = new ArrayList<RoundHole>();
        }
        mRoundHoleList.add(roundHole);
    }

    public List<RoundHole> getRoundHoleList() { return this.mRoundHoleList; }
}
