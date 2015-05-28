package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Club;
import com.example.android.ShotTracker.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 1/7/15.
 */
public class BagDAO extends ShotTrackerDBDAO {

    private static final String WHERE_IDS_EQUAL = DataBaseHelper.PLAYERID_COLUMN
            + "=? AND "
            + DataBaseHelper.CLUBID_COLUMN + "=?";

    public BagDAO(Context context){ super(context); }

    /**
     * Add a Club to a Players bag using PlayerID.
     *
     * @param player Player ID Club will be associated with
     * @param club Club to be added to players bag
     * @return
     */
    public long createClubToBag(Player player, Club club) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.PLAYERID_COLUMN, player.getID());
        values.put(DataBaseHelper.CLUBID_COLUMN, club.getID());

        return database.insert(DataBaseHelper.BAG_TABLE, null, values);
    }

    /**
     * Delete a club from a players bag
     * @param player
     * @param club
     * @return
     */
    public int deleteClubFromBag(Player player, Club club) {
        // check that playerID is set, otherwise we can't delete
        if (player.getID() < 0)
            throw new RuntimeException("player ID not set in BagDAO.deleteClubFromBag()");

        // check that clubID is set, otherwise we can't delete
        if (club.getID() < 0)
            throw new RuntimeException("club ID not set in BagDAO.deleteClubFromBag()");

        return database.delete(DataBaseHelper.BAG_TABLE,
                WHERE_IDS_EQUAL, new String[] {player.getID() + "", club.getID() + ""});
    }

    /**
     * read all clubs in a players bag
     * @param player
     * @return
     */
    public List<Club> readClubsInBag(Player player) {
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
                + "=" + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        while ( cursor.moveToNext() ) {
            Club club = new Club();
            club.setID(cursor.getLong(0));
            club.setClub(cursor.getString(1));

            clubs.add(club);
        }
        cursor.close();

        return clubs;
    }


}
