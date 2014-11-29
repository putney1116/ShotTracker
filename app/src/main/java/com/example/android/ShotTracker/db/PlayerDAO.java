package com.example.android.ShotTracker.db;

import java.util.List;
import java.util.ArrayList;

import com.example.android.ShotTracker.objects.Player;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


/**
 * Database access object for Players
 */
public class PlayerDAO extends ShotTrackerDBDAO {

    private static final String WHERE_ID_EQUALS = DataBaseHelper.PLAYERID_COLUMN
            + " =?";

    public PlayerDAO(Context context) {
        super(context);
    }

    /**
     * Save Player to the database
     * @param player Player object to insert into DB
     * @return
     */
    public long save(Player player) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.PLAYERNAME_COLUMN, player.getName());
        ///\todo Test if player handicap exists before inserting.
        values.put(DataBaseHelper.PLAYERHANDICAP_COLUMN, player.getHandicap());

        return database.insert(DataBaseHelper.PLAYER_TABLE, null, values);
    }

    /**
     * Update Player information
     * @param player Player information to be updated
     * @return
     */
    public long update(Player player) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.PLAYERNAME_COLUMN, player.getName());
        ///\todo Test if player handicap exists before inserting.
        values.put(DataBaseHelper.PLAYERHANDICAP_COLUMN, player.getHandicap());

        long result = database.update(DataBaseHelper.PLAYER_TABLE, values,
                WHERE_ID_EQUALS,
                new String[] { String.valueOf(player.getID())});
        return result;
    }

    /**
     * Delete Player from the DB
     * \todo This has far-reaching consequences in other tables, should do something about that here
     * @param player Player to be deleted from DB
     * @return
     */
    public int deletePlayer(Player player) {
        return database.delete(DataBaseHelper.PLAYER_TABLE,
                WHERE_ID_EQUALS, new String[] { player.getID() + ""});
    }

    /**
     * Get a list of all Players in the DB
     * @return
     */
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<Player>();

        ///\todo Check nulls
        Cursor cursor = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] { DataBaseHelper.PLAYERID_COLUMN,
                DataBaseHelper.PLAYERNAME_COLUMN, DataBaseHelper.PLAYERHANDICAP_COLUMN},
                null, null, null, null, null);

        while ( cursor.moveToNext() ) {
            Player player = new Player();
            player.setID(cursor.getInt(0));
            player.setName(cursor.getString(1));
            player.setHandicap(cursor.getInt(2));

            players.add(player);
        }

        return players;
    }

    /**
     * For testing, add a set of default players.
     */
    public void loadPlayers() {
        List<Player> players = new ArrayList<Player>();
        players.add(new Player("Eric Putney"));
        players.add(new Player("Erik Jensen"));
        players.add(new Player("Darren"));
        players.add(new Player("Justin"));

        for (Player player : players) save(player);
    }

}
