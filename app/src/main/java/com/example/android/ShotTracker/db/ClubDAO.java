package com.example.android.ShotTracker.db;

import java.util.List;
import java.util.ArrayList;

import com.example.android.ShotTracker.objects.Club;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Player;

/**
 * Database CRUD methods for Club objects
 */
public class ClubDAO extends ShotTrackerDBDAO {

    private static final String WHERE_ID_EQUALS = DataBaseHelper.CLUBID_COLUMN
            + " =?";

    public ClubDAO(Context context) {
        super(context);
    }

    /**
     * Add Club to DB
     *
     * @param club Club to be added to DB
     * @return
     */
    public long create(Club club) {
        ///\todo check against duplicate entries in db? goes here?
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.CLUBNAME_COLUMN, club.getClub());

        return database.insert(DataBaseHelper.CLUB_TABLE, null, values);
    }

    ///\todo update() and delete() methods shouldn't be needed, but could add them for completeness

    /**
     * Get a list of all Clubs in the database
     *
     * @return List of Club objects
     */
    public List<Club> readListofClubs() {
        List<Club> clubs = new ArrayList<Club>();

        Cursor cursor = database.query(DataBaseHelper.CLUB_TABLE,
                new String[] {DataBaseHelper.CLUBID_COLUMN, DataBaseHelper.CLUBNAME_COLUMN},
                null, null, null, null, null);

        while ( cursor.moveToNext() ) {
            Club club = new Club();
            club.setID(cursor.getInt(0));
            club.setClub(cursor.getString(1));

            clubs.add(club);
        }

        return clubs;
    }

    /**
     * Get club information from an id value
     * @param clubID
     * @return
     */
    public Club readClub(int clubID) {
        Cursor cursor = database.query(DataBaseHelper.CLUB_TABLE,
                new String[] {DataBaseHelper.CLUBID_COLUMN, DataBaseHelper.CLUBNAME_COLUMN},
                WHERE_ID_EQUALS,
                new String[] {String.valueOf(clubID)},
                null, null, null, null);

        Club club = new Club();
        club.setID(cursor.getInt(0));
        club.setClub(cursor.getString(1));

        return club;
    }

}
