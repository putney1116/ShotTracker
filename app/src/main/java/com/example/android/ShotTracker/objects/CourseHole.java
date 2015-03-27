package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold hole information for a given course
 */
public class CourseHole {

    private long mID = -1;
    private long mSubCourseID = -1;
    private int mHoleNum = -1;
    private int mPar = -1;
    private int mWPar = -1;
    private int mMenHandicap = -1;
    private int mWomenHandicap = -1;
    private int mBlueYardage = -1;
    private int mWhiteYardage = -1;
    private int mRedYardage = -1;
    private List<CourseHoleInfo> mCourseHoleInfoList = null;

    /**
     * Constructors
     */
    public CourseHole() {}

    public CourseHole(long id, SubCourse subcourse, int hnum, int par, int wpar,
                      int mhandicap, int whandicap, int blueyardage,
                      int whiteyardage, int redyardage)
    {
        this.mID = id;
        this.mSubCourseID = subcourse.getID();
        this.mHoleNum = hnum;
        this.mPar = par;
        this.mWPar = wpar;
        this.mMenHandicap = mhandicap;
        this.mWomenHandicap = whandicap;
        this.mBlueYardage = blueyardage;
        this.mWhiteYardage = whiteyardage;
        this.mRedYardage = redyardage;
    }

    public CourseHole(SubCourse subcourse, int hnum, int par, int wpar,
                      int mhandicap, int whandicap, int blueyardage,
                      int whiteyardage, int redyardage)
    {
        this.mSubCourseID = subcourse.getID();
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
        this.mSubCourseID = subcourse.getID();
        this.mHoleNum = hnum;
        this.mPar = par;
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
    public void setSubCourseIDFromID(long id) {
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

    public void setCourseHoleInfoList(List<CourseHoleInfo> courseHoleInfos){
        this.mCourseHoleInfoList = courseHoleInfos;
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

    /**
     * List of CourseHoleInfo objects
     */
    public void addCourseHoleInfo(CourseHoleInfo courseHoleInfo) {
        if (mCourseHoleInfoList == null) {
            mCourseHoleInfoList = new ArrayList<CourseHoleInfo>();
        }
        mCourseHoleInfoList.add(courseHoleInfo);
    }

    public List<CourseHoleInfo> getCourseHoleInfoList() { return this.mCourseHoleInfoList; }
}
