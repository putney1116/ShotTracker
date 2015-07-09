package com.example.android.ShotTracker.objects;

/**
 * Class to hold Club information
 */
public class Club {

    private long mID = -1;
    private String mClub = "";
    private float mAvgDist = 0;
    private float mAccuracy = 0;

    /**
     * Default Constructor
     */
    public Club() {}

    /**
     * Constructor
     *
     * @param id Unique Club ID
     * @param club Club name
     */
    public Club(long id, String club) {
        this.mID = id;
        this.mClub = club;
    }

    /**
     * Constructor
     *
     * @param club Club name
     */
    public Club(String club) {
        this.mClub = club;
    }

    /**
     * Setters
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setClub(String club) {
        this.mClub = club;
    }

    public void setAvgDist(float dist) { this.mAvgDist = dist; }

    public void setAccuracy(float acc) { this.mAccuracy = acc; }

    /**
     * Getters
     */
    public long getID() {
        return this.mID;
    }

    public String getClub() {
        return this.mClub;
    }

    public float getAvgDist() { return this.mAvgDist; }

    public float getAccuracy() { return this.mAccuracy; }
}
