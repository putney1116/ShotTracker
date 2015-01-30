package com.example.android.ShotTracker.db;

import java.util.List;
import java.util.ArrayList;
import java.lang.Throwable;

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
     * Insert Player to the database
     * @param player Player object to insert into DB
     * @return
     */
    public long create(Player player) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.PLAYERNAME_COLUMN, player.getName());

        if (player.getHandicap() > 0)
            values.put(DataBaseHelper.PLAYERHANDICAP_COLUMN, player.getHandicap());

        return database.insert(DataBaseHelper.PLAYER_TABLE, null, values);
    }

    /**
     * Update Player information
     * @param player Player information to be updated. Player ID must be set.
     * @return
     */
    public long update(Player player) {
        ContentValues values = new ContentValues();

        // check that playerID is set, otherwise we can't update
        if (player.getID() < 0)
            throw new RuntimeException("player ID not set in PlayerDAO.update()");

        values.put(DataBaseHelper.PLAYERNAME_COLUMN, player.getName());

        if (player.getHandicap() > 0)
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
        // check that playerID is set, otherwise we can't delete
        if (player.getID() < 0)
            throw new RuntimeException("player ID not set in PlayerDAO.delete()");

        return database.delete(DataBaseHelper.PLAYER_TABLE,
                WHERE_ID_EQUALS, new String[] { player.getID() + ""});
    }

    /**
     * Get a list of all Players in the DB
     * @return
     */
    public List<Player> readListofPlayers() {
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
        cursor.close();

        return players;
    }

    /**
     * Get the players name and handicap given an ID
     * @param player Player object. ID must be filled
     * @return player Filled Player object
     */
    public Player readPlayer(Player player) {
        if (player.getID() < 0) {
            throw new RuntimeException("Player ID not set in PlayerDAO::readPlayer()");
        }

        Cursor cursor = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] { DataBaseHelper.PLAYERID_COLUMN,
                        DataBaseHelper.PLAYERNAME_COLUMN,
                        DataBaseHelper.PLAYERHANDICAP_COLUMN},
                WHERE_ID_EQUALS,
                new String[] {String.valueOf(player.getID())},
                null, null, null);

        if (cursor.getCount() != 1) {
            throw new RuntimeException("DB Query returned " +
                    cursor.getCount() +
            ", expected 1. In PlayerDBAO::readPlayer()");
        }

        player.setName(cursor.getString(1));
        player.setHandicap(cursor.getInt(2));
        cursor.close();

        return player;
    }

}
