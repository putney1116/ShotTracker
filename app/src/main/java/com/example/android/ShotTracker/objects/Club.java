package com.example.android.ShotTracker.objects;

/**
 * Class to hold Club information
 */
public class Club {

    private int mID = -1;
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
    public Club(int id, String club) {
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
    public void setID(int id) {
        this.mID = id;
    }

    public void setClub(String club) {
        this.mClub = club;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public String getClub() {
        return this.mClub;
    }
}
