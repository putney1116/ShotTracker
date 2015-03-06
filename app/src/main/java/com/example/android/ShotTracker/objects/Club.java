package com.example.android.ShotTracker.objects;

/**
 * Class to hold Club information
 */
public class Club {

    private long mID = -1;
    private String mClub = "";

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

    /**
     * Getters
     */
    public long getID() {
        return this.mID;
    }

    public String getClub() {
        return this.mClub;
    }
}
