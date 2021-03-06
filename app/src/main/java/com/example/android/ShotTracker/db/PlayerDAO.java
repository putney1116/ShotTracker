package com.example.android.ShotTracker.db;

import java.sql.SQLException;
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

    private static final String WHERE_NAME_EQUALS = DataBaseHelper.PLAYERNAME_COLUMN
            + " =?";

    private static final String WHERE_USERDEF_EQUALS = DataBaseHelper.USRDEF_COLUMN
            + " =?";

    public PlayerDAO(Context context) {
        super(context);
    }

    /**
     * Insert Player to the database
     * @param player Player object to insert into DB
     * @return
     */
    public long create(Player player) throws SQLException {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.PLAYERNAME_COLUMN, player.getName());

        values.put(DataBaseHelper.USRDEF_COLUMN, player.getUserDefault() ? 1 : 0);

        if (player.getHandicap() > 0)
            values.put(DataBaseHelper.PLAYERHANDICAP_COLUMN, player.getHandicap());

        long id = database.insertOrThrow(DataBaseHelper.PLAYER_TABLE, null, values);

        return id;
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
        values.put(DataBaseHelper.USRDEF_COLUMN, player.getUserDefault() ? 1 : 0);

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
                        DataBaseHelper.PLAYERNAME_COLUMN,
                        DataBaseHelper.USRDEF_COLUMN,
                        DataBaseHelper.PLAYERHANDICAP_COLUMN},
                null, null, null, null, null);

        while ( cursor.moveToNext() ) {
            Player player = new Player();
            player.setID(cursor.getLong(0));
            player.setName(cursor.getString(1));
            player.setUserDefault(cursor.getInt(2) == 1);
            player.setHandicap(cursor.getInt(3));

            players.add(player);
        }
        cursor.close();

        return players;
    }

    /**
     * Get a list of all Player names in the DB
     * @return
     */
    public List<String> readListofPlayerNames() {
        List<String> players = new ArrayList<String>();

        ///\todo Check nulls
        Cursor cursor = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] {DataBaseHelper.PLAYERNAME_COLUMN},
                null, null, null, null, null);

        while ( cursor.moveToNext() ) {
            players.add(cursor.getString(0));
        }
        cursor.close();

        return players;
    }

    public List<String> readListofPlayerNameswDefaultFirst() {
        List<String> players = new ArrayList<String>();

        // Get the default player first
        Cursor cursordef = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] {DataBaseHelper.PLAYERNAME_COLUMN},
                WHERE_USERDEF_EQUALS,
                new String[] {String.valueOf(1)},
                null, null, null);

        if (cursordef.getCount() != 1) {
            throw new RuntimeException("DB Query returned " +
                    cursordef.getCount() +
                    ", expected 1. In PlayerDBAO::readUserDefaultPlayer()");
        }
        cursordef.moveToNext();
        players.add(cursordef.getString(0));
        cursordef.close();


        //Get all remaining players
        Cursor cursor = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] {DataBaseHelper.PLAYERNAME_COLUMN},
                WHERE_USERDEF_EQUALS,
                new String[] {String.valueOf(0)},
                null, null, null);

        while (cursor.moveToNext()) {
            players.add(cursor.getString(0));
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
                        DataBaseHelper.USRDEF_COLUMN,
                        DataBaseHelper.PLAYERHANDICAP_COLUMN},
                WHERE_ID_EQUALS,
                new String[] {String.valueOf(player.getID())},
                null, null, null);

        if (cursor.getCount() != 1) {
            throw new RuntimeException("DB Query returned " +
                    cursor.getCount() +
            ", expected 1. In PlayerDBAO::readPlayer()");
        }

        cursor.moveToNext();
        player.setName(cursor.getString(1));
        player.setUserDefault(cursor.getInt(2) == 1);
        player.setHandicap(cursor.getInt(3));
        cursor.close();

        return player;
    }

    /**
     * Get the players ID given the name
     * @param player_name name
     * @return player ID
     */
    public long readIDFromName(String player_name) {
        Cursor cursor = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] { DataBaseHelper.PLAYERID_COLUMN},
                WHERE_NAME_EQUALS,
                new String[] {player_name},
                null, null, null);

        /*if (cursor.getCount() != 1) {
            throw new RuntimeException("DB Query returned " +
                    cursor.getCount() +
                    ", expected 1. In PlayerDBAO::readPlayer()");
        }*/

        long playerID = -1;

        while ( cursor.moveToNext() ) {
            playerID = cursor.getLong(0);

        }

        cursor.close();
        return playerID;
    }

    /**
     * Get the default player from the DB
     * @return
     */
    public Player readUserDefaultPlayer() {

        Cursor cursor = database.query(DataBaseHelper.PLAYER_TABLE,
                new String[] { DataBaseHelper.PLAYERID_COLUMN,
                        DataBaseHelper.PLAYERNAME_COLUMN,
                        DataBaseHelper.USRDEF_COLUMN,
                        DataBaseHelper.PLAYERHANDICAP_COLUMN},
                WHERE_USERDEF_EQUALS,
                new String[] {String.valueOf(1)},
                null, null, null);

        if (cursor.getCount() != 1) {
            throw new RuntimeException("DB Query returned " +
                    cursor.getCount() +
                    ", expected 1. In PlayerDBAO::readUserDefaultPlayer()");
        }

        cursor.moveToNext();
        Player player = new Player();
        player.setID(cursor.getLong(0));
        player.setName(cursor.getString(1));
        player.setUserDefault(cursor.getInt(2) == 1);
        player.setHandicap(cursor.getInt(3));
        cursor.close();

        return player;
    }
}
