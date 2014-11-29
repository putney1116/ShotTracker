package com.example.android.ShotTracker.db;

import java.util.List;
import java.util.ArrayList;

import com.example.android.ShotTracker.objects.Club;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


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
    public long save(Club club) {
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
    public List<Club> getClubs() {
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
     * Add a Club to a Players bag using PlayerID.
     * \todo Could use Player object instead of playerID, think about it
     *
     * @param playerID Player ID Club will be associated with
     * @param club Club to be added to players bag
     * @return
     */
    public long addClubToBag(int playerID, Club club) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.PLAYERID_COLUMN, playerID);
        values.put(DataBaseHelper.CLUBID_COLUMN, club.getID());

        return database.insert(DataBaseHelper.BAG_TABLE, null, values);
    }

    public List<Club> getClubsInBag(int playerID) {
        List<Club> clubs = new ArrayList<Club>();

        /// Build multi-table query using NATURAL JOIN
        ///\todo look into SQLiteQueryBuilder
        String query = "SELECT "
                + DataBaseHelper.CLUBID_COLUMN + ", "
                + DataBaseHelper.CLUBNAME_COLUMN
                + " FROM "
                + DataBaseHelper.BAG_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.CLUB_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + playerID;

        Cursor cursor = database.rawQuery(query, null);

        while ( cursor.moveToNext() ) {
            Club club = new Club();
            club.setID(cursor.getInt(0));
            club.setClub(cursor.getString(1));

            clubs.add(club);
        }

        return clubs;
    }

    /**
     * For testing, add a set of clubs to the DB
     */
    public void loadClubs() {
        List<Club> clubs = new ArrayList<Club>();
        clubs.add(new Club("Driver"));
        clubs.add(new Club("3 Wood"));
        clubs.add(new Club("5 Wood"));
        clubs.add(new Club("3 Iron"));
        clubs.add(new Club("4 Iron"));
        clubs.add(new Club("5 Iron"));
        clubs.add(new Club("6 Iron"));
        clubs.add(new Club("7 Iron"));
        clubs.add(new Club("8 Iron"));
        clubs.add(new Club("9 Iron"));
        clubs.add(new Club("PW"));
        clubs.add(new Club("SW"));

        for (Club club : clubs) save(club);

    }
}
