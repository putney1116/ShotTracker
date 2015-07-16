package com.example.android.ShotTracker.db;

import java.util.List;
import java.util.ArrayList;

import com.example.android.ShotTracker.objects.Club;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;

/**
 * Database CRUD methods for Club objects
 */
public class ClubDAO extends ShotTrackerDBDAO {

    private static final String WHERE_CLUBID_EQUALS = DataBaseHelper.CLUBID_COLUMN
            + " =?";

    private Context mContext = null;

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

    /**
     *
     * @param club
     * @return
     */
    public long update(Club club) {
        ContentValues values = new ContentValues();
        
        if (club.getID() < 0 ) {
            throw new RuntimeException("ClubID not set in ClubDAO.update()");
        }
        
        values.put(DataBaseHelper.CLUBNAME_COLUMN, club.getClub());
        
        return database.update(DataBaseHelper.CLUB_TABLE,
                values,
                WHERE_CLUBID_EQUALS,
                new String[]{String.valueOf(club.getID())});
    }

    /**
     *
     * @param club
     * @return
     */
    public long delete(Club club) {
        ContentValues values = new ContentValues();

        if (club.getID() < 0 ){
            throw new RuntimeException("ClubID not set in ClubDAO.delete()");
        }

        return database.delete(DataBaseHelper.CLUB_TABLE,
                WHERE_CLUBID_EQUALS,
                new String[]{String.valueOf(club.getID())});
    }

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
            club.setID(cursor.getLong(0));
            club.setClub(cursor.getString(1));

            clubs.add(club);
        }
        cursor.close();
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
                WHERE_CLUBID_EQUALS,
                new String[] {String.valueOf(clubID)},
                null, null, null, null);

        Club club = new Club();
        while ( cursor.moveToNext() ) {
            club.setID(cursor.getLong(0));
            club.setClub(cursor.getString(1));
        }

        cursor.close();

        return club;
    }

}
