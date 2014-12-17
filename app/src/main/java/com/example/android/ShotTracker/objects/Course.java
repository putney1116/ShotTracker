package com.example.android.ShotTracker.objects;

/**
 * Class holding Course information
 */
public class Course {

    private int mID = -1;
    private String mName = "";
    private String mLocation = "";

    //\todo Add private member for list of SubCourse objects and setters, getters, constructors

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
}
