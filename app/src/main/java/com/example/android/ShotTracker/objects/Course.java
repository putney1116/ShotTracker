package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding Course information
 */
public class Course {

    private int mID = -1;
    private String mName = "";
    private String mLocation = "";
    private List<SubCourse> mSubCourseList = null;

    /**
     * Constructors
     */
    public Course() {}

    public Course(int id, String name, String location) {
        this.mID = id;
        this.mName = name;
        this.mLocation = location;
    }

    public Course(String name, String location) {
        this.mName = name;
        this.mLocation = location;
    }

    public Course(String name) {
        this.mName = name;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public String getName() {
        return this.mName;
    }

    public String getLocation() {
        return this.mLocation;
    }

    /**
     * SubCourse list
     */
    public void addSubCourse(SubCourse subCourse) {
        if (mSubCourseList == null) {
            mSubCourseList = new ArrayList<SubCourse>();
        }
        mSubCourseList.add(subCourse);
    }

    public List<SubCourse> getSubCourseList() { return this.mSubCourseList; }

}
