package com.example.android.ShotTracker.objects;

/**
 * Class for keeping information for a given hole in a round
 */
public class RoundHole {

    private int mID = -1;
    private Round mRound;
    private CourseHole mCourseHole;
    private Player mPlayer;
    private int mScore = -1;
    private int mPutts = -1;
    private int mPenalties = -1;
    private int mFairways = -1;

    //\todo Add private member for list of Shot objects and setters, getters, constructors

    /**
     * Constructors
     */
    public RoundHole() {}

    public RoundHole(int id, Round round, CourseHole coursehole, Player player,
                     int score, int putts, int penalties, int fairways) {
        this.mID = id;
        this.mRound = round;
        this.mCourseHole = coursehole;
        this.mPlayer = player;
        this.mScore = score;
        this.mPutts = putts;
        this.mPenalties = penalties;
        this.mFairways = fairways;
    }

    public RoundHole(Round round, CourseHole coursehole, Player player,
                     int score, int putts, int penalties, int fairways) {
        this.mRound = round;
        this.mCourseHole = coursehole;
        this.mPlayer = player;
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

    public void setRoundID(Round round) {
        this.mRound.setID(round.getID());
    }

    public void setCourseHoleID(CourseHole coursehole) {
        this.mCourseHole.setID(coursehole.getID());
    }

    public void setPlayerID(Player player) {
        this.mPlayer.setID(player.getID());
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
        return this.mRound.getID();
    }

    public int getCourseHoleID() {
        return this.mCourseHole.getID();
    }

    public int getPlayerID() {
        return this.mPlayer.getID();
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
