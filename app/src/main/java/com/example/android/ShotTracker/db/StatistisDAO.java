package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.android.ShotTracker.objects.Club;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;

import java.util.List;

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
     * Get the total number of holes with Par=par played by a player in a given round
     * @param par
     * @param player
     * @param round
     * @return
     */
    public int getNHolesPar(int par, Player player, Round round) {
        //build a query to get the total number of holes
        //player with a certain par
        String queryPar = "SELECT "
                + DataBaseHelper.PAR_COLUMN
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "=" + round.getID()
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

    /**
     * Get the number of holes played by a player in a given round
     * Where the score is diff and the par is par
     * @param par
     * @param diff
     * @param player
     * @param round
     * @return
     */
    public int getNHolesParScore(int par, int diff, Player player, Round round) {
        String queryScore = "SELECT "
                + DataBaseHelper.PAR_COLUMN
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "=" + round.getID()
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

    /**
     * Get the number of holes played by a given player for a given course
     * where the score is diff + par and par is par
     * @param par
     * @param diff
     * @param player
     * @param course
     * @return
     */
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

    /**
     * Get the number of chips for the given player
     * @param player
     * @return
     */
    public int getNumofChips(Player player) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.CHIPS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToNext()){
            numChips = cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @return
     */
    public int getNumofChips(int par, Player player) {

        int numChips = -1;

        String queryPar = "SELECT SUM("
                + DataBaseHelper.CHIPS_COLUMN
                + ") FROM "
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

        if(cursor.moveToNext()){
            numChips = cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @param round
     * @return
     */
    public int getNumofChips(Player player, Round round){

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.CHIPS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.ROUNDID_COLUMN
                + "="
                + round.getID()
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @param round
     * @return
     */
    public int getNumofChips(int par, Player player, Round round){

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.CHIPS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "="
                + round.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @param course
     * @return
     */
    public int getNumofChips(Player player, Course course) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.CHIPS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + "="
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + "="
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "="
                + course.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @param course
     * @return
     */
    public int getNumofChips(int par, Player player, Course course) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.CHIPS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + "="
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + "="
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "="
                + course.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @return
     */
    public int getNumofPutts(Player player) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PUTTS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToNext()){
            numChips = cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @return
     */
    public int getNumofPutts(int par, Player player) {

        int numChips = -1;

        String queryPar = "SELECT SUM("
                + DataBaseHelper.PUTTS_COLUMN
                + ") FROM "
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

        if(cursor.moveToNext()){
            numChips = cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @param round
     * @return
     */
    public int getNumofPutts(Player player, Round round){

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PUTTS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.ROUNDID_COLUMN
                + "="
                + round.getID()
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @param round
     * @return
     */
    public int getNumofPutts(int par, Player player, Round round){

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PUTTS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "="
                + round.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @param course
     * @return
     */
    public int getNumofPutts(Player player, Course course) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PUTTS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + "="
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + "="
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "="
                + course.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @param course
     * @return
     */
    public int getNumofPutts(int par, Player player, Course course) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PUTTS_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + "="
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + "="
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "="
                + course.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @return
     */
    public int getNumofPenalties(Player player) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PENALTIES_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToNext()){
            numChips = cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @return
     */
    public int getNumofPenalties(int par, Player player) {

        int numChips = -1;

        String queryPar = "SELECT SUM("
                + DataBaseHelper.PENALTIES_COLUMN
                + ") FROM "
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

        if(cursor.moveToNext()){
            numChips = cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @param round
     * @return
     */
    public int getNumofPenalties(Player player, Round round){

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PENALTIES_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.ROUNDID_COLUMN
                + "="
                + round.getID()
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID();


        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @param round
     * @return
     */
    public int getNumofPenalties(int par, Player player, Round round){

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PENALTIES_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "="
                + round.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param player
     * @param course
     * @return
     */
    public int getNumofPenalties(Player player, Course course) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PENALTIES_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + "="
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + "="
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "="
                + course.getID();

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     *
     * @param par
     * @param player
     * @param course
     * @return
     */
    public int getNumofPenalties(int par, Player player, Course course) {

        int numChips = -1;

        String query = "SELECT SUM("
                + DataBaseHelper.PENALTIES_COLUMN
                + ") FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " ON "
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + "="
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.COURSEHOLEID_COLUMN
                + " LEFT JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " ON "
                + DataBaseHelper.COURSEHOLE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + "="
                + DataBaseHelper.SUBCOURSE_TABLE + "." + DataBaseHelper.SUBCOURSEID_COLUMN
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "="
                + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "="
                + course.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par;

        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            numChips=cursor.getInt(0);
        }

        cursor.close();

        return numChips;
    }

    /**
     * Count the number of Fairways hit by given player
     * Note that this is a total sum, not per round or hole
     * @param player
     * @return
     */
    public int getNFairways(Player player) {

        int nFairways = -1;
        String query = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.FAIRWAYS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst())
            nFairways = cursor.getInt(0);
        cursor.close();
        return nFairways;
    }

    /**
     * Get the number of fairways for a given par and player
     * @param par
     * @param player
     * @return
     */
    public int getNFairways(int par, Player player) {

        int nFairways = -1;
        String queryPar = "SELECT count(*)"
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
                + DataBaseHelper.FAIRWAYS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(queryPar, null);
        if (cursor.moveToFirst())
            nFairways = cursor.getInt(0);
        cursor.close();
        return nFairways;

    }

    /**
     * Get the number of fairways hit by a given player
     * for a given course
     * @param player
     * @param course
     * @return
     */
    public int getNFairways(Player player, Course course) {

        int nFairways = -1;
        String query = "SELECT count(*)"
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
                + DataBaseHelper.PLAYERID_COLUMN
                + " = " + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + " = " + course.getID()
                + " AND "
                + DataBaseHelper.FAIRWAYS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(query, null);
        if ( cursor.moveToFirst() )
            nFairways = cursor.getInt(0);
        cursor.close();
        return nFairways;
    }

    /**
     * Get the number of fairways hit by a player for a given par on a given course
     * @param par
     * @param player
     * @param course
     * @return
     */
    public int getNFairways(int par, Player player, Course course) {

        int nFairways = -1;
        String queryPar = "SELECT count(*)"
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
                + DataBaseHelper.FAIRWAYS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(queryPar, null);
        if ( cursor.moveToFirst() )
            nFairways = cursor.getInt(0);
        cursor.close();
        return nFairways;
    }

    /**
     * Get the number of fairways hit by the player in the given round
     * @param player
     * @param round
     * @return
     */
    public int getNFairways(Player player, Round round) {
        int nFairways = -1;
        String queryScore = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "=" + round.getID()
                + " AND "
                + DataBaseHelper.FAIRWAYS_COLUMN
                + ">0";
        Cursor cursor = database.rawQuery(queryScore, null);
        if ( cursor.moveToFirst() )
            nFairways = cursor.getInt(0);
        cursor.close();
        return nFairways;
    }

    /**
     * Get the number of fairways hit by a player in a given round for a given par
     * @param par
     * @param player
     * @param round
     * @return
     */
    public int getNFairways(int par, Player player, Round round) {
        int nFairways = -1;
        String queryScore = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "=" + round.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par
                + " AND "
                + DataBaseHelper.FAIRWAYS_COLUMN
                + ">0";
        Cursor cursor = database.rawQuery(queryScore, null);
        if ( cursor.moveToFirst() )
            nFairways = cursor.getInt(0);
        cursor.close();
        return nFairways;
    }




    /**
     * Get the number of greens in regulation for the given player
     * @param player
     * @return
     */
    public int getNGiR(Player player) {

        int nGiR = -1;
        String query = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.GIR_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst())
            nGiR = cursor.getInt(0);
        cursor.close();
        return nGiR;
    }

    /**
     * Get the greens in regulation for a given player and par
     * @param par
     * @param player
     * @return
     */
    public int getNGiR(int par, Player player) {

        int nGiR = -1;
        String queryPar = "SELECT count(*)"
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
                + DataBaseHelper.GIR_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(queryPar, null);
        if (cursor.moveToFirst())
            nGiR = cursor.getInt(0);
        cursor.close();
        return nGiR;
    }

    /**
     * Get the number of greens in regulation for the given player and course
     * @param player
     * @param course
     * @return
     */
    public int getNGiR(Player player, Course course) {

        int nGiR = -1;
        String query = "SELECT count(*)"
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
                + DataBaseHelper.PLAYERID_COLUMN
                + " = " + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + " = " + course.getID()
                + " AND "
                + DataBaseHelper.GIR_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(query, null);
        if ( cursor.moveToFirst() )
            nGiR = cursor.getInt(0);
        cursor.close();
        return nGiR;
    }

    /**
     * Get the number of greens in regulation for the given par, player, and course
     * @param par
     * @param player
     * @param course
     * @return
     */
    public int getNGiR(int par, Player player, Course course) {

        int nGiR = -1;
        String queryPar = "SELECT count(*)"
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
                + DataBaseHelper.GIR_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(queryPar, null);
        if ( cursor.moveToFirst() )
            nGiR = cursor.getInt(0);
        cursor.close();
        return nGiR;
    }

    /**
     * Get the number of greens in regulation for the given player and round
     * @param player
     * @param round
     * @return
     */
    public int getNGiR(Player player, Round round) {
        int nGiR = -1;
        String queryScore = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "=" + round.getID()
                + " AND "
                + DataBaseHelper.GIR_COLUMN
                + ">0";
        Cursor cursor = database.rawQuery(queryScore, null);
        if ( cursor.moveToFirst() )
            nGiR = cursor.getInt(0);
        cursor.close();
        return nGiR;
    }

    /**
     * Get the number of greens in regulation for the given player, par, and round
     * @param par
     * @param player
     * @param round
     * @return
     */
    public int getNGiR(int par, Player player, Round round) {
        int nGiR = -1;
        String queryScore = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBROUND_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.ROUNDID_COLUMN
                + "=" + round.getID()
                + " AND "
                + DataBaseHelper.PAR_COLUMN
                + "=" + par
                + " AND "
                + DataBaseHelper.GIR_COLUMN
                + ">0";
        Cursor cursor = database.rawQuery(queryScore, null);
        if ( cursor.moveToFirst() )
            nGiR = cursor.getInt(0);
        cursor.close();
        return nGiR;
    }

    /**
     * Get the average distance for a given player and club
     * @param player
     * @param club
     * @return
     */
    public float getClubAvgDist(Player player, Club club) {
        float avg = 0;
        String query = "SELECT AVG("
                + DataBaseHelper.YARDS_COLUMN + ")"
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(query, null);
        if ( cursor.moveToFirst() )
            avg = cursor.getFloat(0);
        cursor.close();
        return avg;
    }

    /**
     * Get the average shot distance given a player, club, and course
     * @param player
     * @param club
     * @param course
     * @return
     */
    public float getClubAvgDist(Player player, Club club, Course course) {
        float avg = 0;
        String query = "SELECT AVG("
                + DataBaseHelper.YARDS_COLUMN + ")"
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " ON "
                + DataBaseHelper.SHOT_TABLE + "." + DataBaseHelper.ROUNDHOLEID_COLUMN
                + "="
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.ROUNDHOLEID_COLUMN
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
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "=" + course.getID()
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(query, null);
        if ( cursor.moveToFirst() )
            avg = cursor.getFloat(0);
        cursor.close();
        return avg;
    }

    /**
     * Get the percentage of shots that are within +/- "dist" of the average shot yardage
     * for a given player and club
     * @param player
     * @param club
     * @return
     */
    public float getClubAccuracy(Player player, Club club, float dist) {
        int nshot = 0;
        int ngood = 0;

        String querynshot = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(querynshot, null);
        if ( cursor.moveToFirst() )
            nshot = cursor.getInt(0);
        cursor.close();

        float lyards = club.getAvgDist() - dist;
        float hyards = club.getAvgDist() + dist;
        String queryngood = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + ">" + lyards
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + "<" + hyards;

        cursor = database.rawQuery(queryngood, null);
        if ( cursor.moveToFirst() )
            ngood = cursor.getInt(0);
        cursor.close();

        if (nshot > 0) {
            return (float)ngood / (float)nshot * (float)100.;
        }
        else {
            return 0;
        }
    }

    /**
     * Get the percentage of shots that are within +/- "dist" of the average shot yardage
     * for a given player, club, and course
     * @param player
     * @param club
     * @param course
     * @return
     */
    public float getClubAccuracy(Player player, Club club, Course course, float dist) {
        int nshot = 0;
        int ngood = 0;

        String querynshot = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " ON "
                + DataBaseHelper.SHOT_TABLE + "." + DataBaseHelper.ROUNDHOLEID_COLUMN
                + "="
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.ROUNDHOLEID_COLUMN
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
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "=" + course.getID()
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + ">0";

        Cursor cursor = database.rawQuery(querynshot, null);
        if ( cursor.moveToFirst() )
            nshot = cursor.getInt(0);
        cursor.close();

        float lyards = club.getAvgDist() - dist;
        float hyards = club.getAvgDist() + dist;
        String queryngood = "SELECT count(*)"
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " LEFT JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " ON "
                + DataBaseHelper.SHOT_TABLE + "." + DataBaseHelper.ROUNDHOLEID_COLUMN
                + "="
                + DataBaseHelper.ROUNDHOLE_TABLE + "." + DataBaseHelper.ROUNDHOLEID_COLUMN
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
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "=" + course.getID()
                + " AND "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + ">" + lyards
                + " AND "
                + DataBaseHelper.YARDS_COLUMN
                + "<" + hyards;

        cursor = database.rawQuery(queryngood, null);
        if ( cursor.moveToFirst() )
            ngood = cursor.getInt(0);
        cursor.close();

        if (nshot > 0) {
            return (float)ngood / (float)nshot * (float)100.;
        }
        else {
            return 0;
        }
    }

}
