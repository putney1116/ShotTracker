package com.example.android.ShotTracker.objects;

/**
 * Class to hold hole information for a given course
 */
public class CourseHole {

    private int mID;
    private int mSubCourseID;
    private int mHoleNum;
    private int mPar;
    private float mMenHandicap;
    private float mWomenHandicap;

    ///\todo add list of CourseHoleInfo objects

    /**
     * Constructors
     */
    public CourseHole() {}

    public CourseHole(int id, int cid, int hnum, int par, float mhandicap, float whandicap)
    {
        this.mID = id;
        this.mSubCourseID = cid;
        this.mHoleNum = hnum;
        this.mPar = par;
        this.mMenHandicap = mhandicap;
        this.mWomenHandicap = whandicap;
    }

    public CourseHole(int cid, int hnum, int par)
    {
        this.mSubCourseID = cid;
        this.mHoleNum = hnum;
        this.mPar = par;
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

    public void setHoleNumber(int hnum) {
        this.mHoleNum = hnum;
    }

    public void setPar(int par) {
        this.mPar = par;
    }

    public void setMenHandicap(int mhandicap) {
        this.mMenHandicap = mhandicap;
    }

    public void setWomenHandicap(int whandicap) {
        this.mWomenHandicap = whandicap;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public int getSubCourseID() {
        return this.mSubCourseID;
    }

    public int getHoleNumber() {
        return this.mHoleNum;
    }

    public int getPar() {
        return this.mPar;
    }

    public float getMenHandicap() {
        return this.mMenHandicap;
    }

    public float getWomenHandicap() {
        return this.mWomenHandicap;
    }
}
