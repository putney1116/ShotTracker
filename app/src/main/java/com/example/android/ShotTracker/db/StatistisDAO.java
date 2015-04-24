package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.android.ShotTracker.objects.Course;
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
     * Get the number of holes played with a given par by selecting on par, player, and course
     * @param par
     * @param player
     * @param course
     * @return
     */
    public int getNHolesPar(int par, Player player, Course course) {
        //build a query to get the total number of holes
        //player with a certain par, given a course ID

        String queryPar = "SELECT *"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " = "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " = "
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PAR_COLUMN
                + " = " + par
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + " = " + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + " = " + course.getID();

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

    public int getNHolesParScore(int par, int diff, Player player, Course course) {
        //build a query to get the total number of holes
        //player with a certain par, given a course ID

        String queryPar = "SELECT *"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " = "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " = "
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PAR_COLUMN
                + " = " + par
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + " = " + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + " = " + course.getID()
                + " AND "
                + DataBaseHelper.SCORE_COLUMN
                + "-"
                + DataBaseHelper.PAR_COLUMN
                + "=" + diff;

        Cursor cursor = database.rawQuery(queryPar, null);

        return cursor.getCount();
    }



}
