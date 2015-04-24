package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Player;

/**
 * Created by damcglinchey on 4/7/15.
 */
public class StatistisDAO extends ShotTrackerDBDAO {

    public StatistisDAO(Context context){ super(context); }


    /**
     * Get the number of holes played given a playerID and par
     * @param par
     * @param player
     * @return
     */
    public int getNHolesPar(int par, Player player) {
        //build a query to get the total number of holes
        //player with a certain par
        String queryPar = "SELECT "
                + DataBaseHelper.PAR_COLUMN
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(queryPar, null);

        return cursor.getCount();
    }

    /**
     * Get the number of holes played given a playerID, par, and score diff
     * @param par
     * @param diff score - par
     * @param player
     * @return
     */
    public int getNHolesParScore(int par, int diff, Player player) {

        //build a query to get the total number of holes
        //player with a certain par
        String queryScore = "SELECT "
                + DataBaseHelper.PAR_COLUMN
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par
                + " AND "
                + DataBaseHelper.SCORE_COLUMN
                + "-"
                + DataBaseHelper.PAR_COLUMN
                + "=" + diff;

        Cursor cursor = database.rawQuery(queryScore, null);

        return cursor.getCount();
    }

}
