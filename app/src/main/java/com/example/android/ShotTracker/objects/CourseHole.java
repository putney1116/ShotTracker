package com.example.android.ShotTracker.objects;

/**
 * Class to hold hole information for a given course
 */
public class CourseHole {

    private int mID = -1;
    private SubCourse mSubCourse;
    private int mHoleNum = -1;
    private int mPar = -1;
    private int mWPar = -1;
    private int mMenHandicap = -1;
    private int mWomenHandicap = -1;
    private int mBlueYardage = -1;
    private int mWhiteYardage = -1;
    private int mRedYardage = -1;

    //\todo Add private member for list of CourseHoleInfo objects and setters, getters, constructors

    /**
     * Constructors
     */
    public CourseHole() {}

    public CourseHole(int id, SubCourse subcourse, int hnum, int par, int wpar,
                      int mhandicap, int whandicap, int blueyardage,
                      int whiteyardage, int redyardage)
    {
        this.mID = id;
        this.mSubCourse = subcourse;
        this.mHoleNum = hnum;
        this.mPar = par;
        this.mWPar = wpar;
        this.mMenHandicap = mhandicap;
        this.mWomenHandicap = whandicap;
        this.mBlueYardage = blueyardage;
        this.mWhiteYardage = whiteyardage;
        this.mRedYardage = redyardage;
    }

    public CourseHole(SubCourse subcourse, int hnum, int par)
    {
        this.mSubCourse = subcourse;
        this.mHoleNum = hnum;
        this.mPar = par;
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
        return this.mSubCourse.getID();
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
