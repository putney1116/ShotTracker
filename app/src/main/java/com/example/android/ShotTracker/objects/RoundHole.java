package com.example.android.ShotTracker.objects;

/**
 * Class for keeping information for a given hole in a round
 */
public class RoundHole {

    private int mID;
    private int mRoundID;
    private int mCourseHoleID;
    private int mPlayerID;
    private int mScore;
    private int mPutts;
    private int mPenalties;
    private int mFairways;

    /**
     * Constructors
     */
    public RoundHole() {}

    public RoundHole(int id, int rid, int hid, int pid,
                     int score, int putts, int penalties, int fairways) {
        this.mID = id;
        this.mRoundID = rid;
        this.mCourseHoleID = hid;
        this.mPlayerID = pid;
        this.mScore = score;
        this.mPutts = putts;
        this.mPenalties = penalties;
        this.mFairways = fairways;
    }

    public RoundHole(int rid, int hid, int pid,
                     int score, int putts, int penalties, int fairways) {
        this.mRoundID = rid;
        this.mCourseHoleID = hid;
        this.mPlayerID = pid;
        this.mScore = score;
        this.mPutts = putts;
        this.mPenalties = penalties;
        this.mFairways = fairways;
    }

    /**
     * Setters
     */
    public void setID(int id) {
        this.mID = id;
    }

    public void setRoundID(int id) {
        this.mRoundID = id;
    }

    public void setCourseHoleID(int id) {
        this.mCourseHoleID = id;
    }

    public void setPlayerID(int id) {
        this.mPlayerID = id;
    }

    public void setScore(int score) {
        this.mScore = score;
    }

    public void setPutts(int putts) {
        this.mPutts = putts;
    }

    public void setPenalties(int penalties) {
        this.mPenalties = penalties;
    }

    public void setFairways(int fairways) {
        this.mFairways = fairways;
    }

    /**
     * Getters
     */
    public int getID() {
        return this.mID;
    }

    public int getRoundID() {
        return this.mRoundID;
    }

    public int getCourseHoleID() {
        return this.mCourseHoleID;
    }

    public int getPlayerID() {
        return this.mPlayerID;
    }

    public int getScore() {
        return this.mScore;
    }

    public int getPutts() {
        return this.mPutts;
    }

    public int getPenalties() {
        return this.mPenalties;
    }

    public int getFairways() {
        return this.mFairways;
    }

}
