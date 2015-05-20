package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for keeping information for a given hole in a round
 */
public class RoundHole {

    private long mID = -1;
    private long mSubRoundID = -1;
    private long mCourseHoleID =-1;
    private long mPlayerID =-1;
    private long mPlayerNum = -1;
    private int mScore = -1;
    private int mPutts = -1;
    private int mPenalties = -1;
    private int mFairways = -1;
    private List<Shot> mShotList = null;

    /**
     * Constructors
     */
    public RoundHole() {}

    public RoundHole(long id, SubRound subRound, CourseHole coursehole, Player player, long pnum,
                     int score, int putts, int penalties, int fairways) {
        this.mID = id;
        this.mSubRoundID = subRound.getID();
        this.mCourseHoleID = coursehole.getID();
        this.mPlayerID = player.getID();
        this.mPlayerNum = pnum;
        this.mScore = score;
        this.mPutts = putts;
        this.mPenalties = penalties;
        this.mFairways = fairways;
    }

    public RoundHole(SubRound subRound, CourseHole coursehole, Player player, long pnum,
                     int score, int putts, int penalties, int fairways) {
        this.mSubRoundID = subRound.getID();
        this.mCourseHoleID = coursehole.getID();
        this.mPlayerID = player.getID();
        this.mPlayerNum = pnum;
        this.mScore = score;
        this.mPutts = putts;
        this.mPenalties = penalties;
        this.mFairways = fairways;
    }

    /**
     * Setters
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setSubRoundID(SubRound subRound) {
        this.mSubRoundID = subRound.getID();
    }

    public void setCourseHoleID(CourseHole coursehole) {
        this.mCourseHoleID = coursehole.getID();
    }

    public void setPlayerID(Player player) {
        this.mPlayerID = player.getID();
    }

    public void setPlayerNumber(long pnum) { this.mPlayerNum = pnum; }

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
    public long getID() {
        return this.mID;
    }

    public long getSubRoundID() {
        return this.mSubRoundID;
    }

    public long getCourseHoleID() {
        return this.mCourseHoleID;
    }

    public long getPlayerID() {
        return this.mPlayerID;
    }

    public long getPlayerNumber() { return this.mPlayerNum; }

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

    /**
     * List of Shot's
     */
    public void addShot(Shot shot) {
        if (mShotList == null) {
            mShotList = new ArrayList<Shot>();
        }
        this.mShotList.add(shot);
    }

    public List<Shot> getShotList() { return this.mShotList; }

}
