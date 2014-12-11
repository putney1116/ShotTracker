package com.example.android.ShotTracker.objects;

/**
 * Class to hold hole information for a given course
 */
public class CourseHole {

    private int mID;
    private int mSubCourseID;
    private int mHoleNum;
    private int mPar;
    private int mWPar;
    private int mMenHandicap;
    private int mWomenHandicap;
    private int mBlueYardage;
    private int mWhiteYardage;
    private int mRedYardage;

    //\todo Add private member for list of CourseHoleInfo objects and setters, getters, constructors

    /**
     * Constructors
     */
    public CourseHole() {}

    public CourseHole(int id, int cid, int hnum, int par, int wpar,
                      int mhandicap, int whandicap, int blueyardage,
                      int whiteyardage, int redyardage)
    {
        this.mID = id;
        this.mSubCourseID = cid;
        this.mHoleNum = hnum;
        this.mPar = par;
        this.mWPar = wpar;
        this.mMenHandicap = mhandicap;
        this.mWomenHandicap = whandicap;
        this.mBlueYardage = blueyardage;
        this.mWhiteYardage = whiteyardage;
        this.mRedYardage = redyardage;
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

    public void setWPar(int wpar){
        this.mWPar = wpar;
    }

    public void setMenHandicap(int mhandicap) {
        this.mMenHandicap = mhandicap;
    }

    public void setWomenHandicap(int whandicap) {
        this.mWomenHandicap = whandicap;
    }

    public void setBlueYardage(int blueyardage) {
        this.mBlueYardage = blueyardage;
    }

    public void setWhiteYardage(int whiteyardage) {
        this.mWhiteYardage = whiteyardage;
    }

    public void setRedYardage(int redyardage) {
        this.mRedYardage = redyardage;
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

    public int getWPar() {
        return this.mWPar;
    }

    public int getMenHandicap() {
        return this.mMenHandicap;
    }

    public int getWomenHandicap() {
        return this.mWomenHandicap;
    }

    public int getBlueYardage() {
        return this.mBlueYardage;
    }

    public int getWhiteYardage() {
        return this.mWhiteYardage;
    }

    public int getRedYardage() {
        return this.mRedYardage;
    }
}
