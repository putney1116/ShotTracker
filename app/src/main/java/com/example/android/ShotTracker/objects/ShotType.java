package com.example.android.ShotTracker.objects;

/**
 * Class to hold ShotType information
 */
public class ShotType {

    private int mID;
    private String mType;

    /**
     * Default Constructor
     */
    public ShotType() {}

    /**
     * Constructor
     *
     * @param id Unique ShotType ID
     * @param type ShotType explanation
     */
    public ShotType(int id, String type) {
        this.mID = id;
        this.mType = type;
    }

    /**
     * Constructor
     *
     * @param type ShotType explanation
     */
    public ShotType(String type) {
        this.mType = type;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setType(String type) {
        this.mType = type;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public String getType() {
        return this.mType;
    }

}
