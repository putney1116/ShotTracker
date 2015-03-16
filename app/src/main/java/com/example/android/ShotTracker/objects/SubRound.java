package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class containing Round information
 */
public class SubRound {

    private long mID = -1;
    private long mSubCourseID = -1;
    private long mRoundID = -1;
    private List<RoundHole> mRoundHoleList = null;

    /**
     * Constructors
     */
    public SubRound() {}

    public SubRound(long id, SubCourse subcourse, Round round) {
        this.mID = id;
        this.mSubCourseID = subcourse.getID();
        this.mRoundID = round.getID();
    }

    public SubRound(SubCourse subcourse, Round round) {
        this.mSubCourseID = subcourse.getID();
        this.mRoundID = round.getID();
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

    public void setRoundID(Round round) {
        this.mRoundID = round.getID();
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

    public long getRoundID() {
        return this.mRoundID;
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
