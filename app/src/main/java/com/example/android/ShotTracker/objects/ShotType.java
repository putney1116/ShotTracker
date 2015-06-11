package com.example.android.ShotTracker.objects;

/**
 * Class to hold ShotType information
 */
public class ShotType {

    private long mID = -1;
    private String mType = "";
    private boolean mIsPre = false;

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
    public ShotType(long id, String type, boolean isPre) {
        this.mID = id;
        this.mType = type;
        this.mIsPre = isPre;
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
     * Constructor
     *
     * @param type ShotType explanation
     */
    public ShotType(String type, boolean isPre) {
        this.mType = type;
        this.mIsPre = isPre;
    }

    /**
     * Setters
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setIsPre(boolean isPre) { this.mIsPre = isPre; }

    /**
     * Getters
     */
    public long getID() {
        return this.mID;
    }

    public String getType() {
        return this.mType;
    }

    public boolean getIsPre() { return this.mIsPre; }

}
