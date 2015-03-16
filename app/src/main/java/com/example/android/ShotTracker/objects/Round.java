package com.example.android.ShotTracker.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class containing Round information
 */
public class Round {

    private long mID = -1;
    private Date mDate;
    private List<SubRound> mSubRoundList = null;

    /**
     * Constructors
     */
    public Round() {}

    public Round(long id, Date date) {
        this.mID = id;
        this.mDate = date;
    }

    public Round(Date date) {
        this.mDate = date;
    }

    /**
     * Setters
     */
    public void setID(long id) {
        this.mID = id;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    /**
     * Getters
     */
    public long getID() {
        return this.mID;
    }

    public Date getDate() {
        return this.mDate;
    }

    /**
     * List of RoundHoles
     */
    public void addSubRound(SubRound subRound) {
        if (mSubRoundList == null) {
            mSubRoundList = new ArrayList<SubRound>();
        }
        mSubRoundList.add(subRound);
    }

    public List<SubRound> getSubRoundList() { return this.mSubRoundList; }
}
