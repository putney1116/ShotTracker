package com.example.android.ShotTracker.objects;

/**
 * Class to hold Player information
 */
public class Player {

    private long mID = -1;
    private String mName = "";
    private int mHandicap = -1;

    /**
     * Default constructor
     */
    public Player() {

    }

    /**
     * Constructor
     *
     * @param id Unique ID number
     * @param name Player Name
     * @param handicap Player Handicap
     */
    public Player(long id, String name, int handicap) {
        this.mID = id;
        this.mName = name;
        this.mHandicap = handicap;
    }

    /**
     * Constructor
     *
     * @param name Player Name
     * @param handicap Player Handicap
     */
    public Player(String name, int handicap) {
        this.mName = name;
        this.mHandicap = handicap;
    }

    /**
     * Constructor
     *
     * @param name Player Name
     */
    public Player(String name) {
        this.mName = name;
    }

    /**
     * Set Methods
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setHandicap(int handicap) {
        this.mHandicap = handicap;
    }

    /**
     * Get Methods
     */
    public long getID() {
        return this.mID;
    }

    public String getName() {
        return this.mName;
    }

    public int getHandicap() {
        return this.mHandicap;
    }
}
