package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ewjensen on 1/20/15.
 */
public class RoundHoleDAO extends ShotTrackerDBDAO {

    private static final String WHERE_ROUNDHOLEID_EQUALS = DataBaseHelper.ROUNDHOLEID_COLUMN
            + "=?";

    private static final String WHERE_PLAYERID_EQUALS = DataBaseHelper.PLAYERID_COLUMN
            + "=?";

    private static final String WHERE_ROUNDID_EQUALS = DataBaseHelper.ROUNDID_COLUMN
            + "=?";

    private static final String WHERE_COURSEHOLEID_EQUALS = DataBaseHelper.COURSEHOLEID_COLUMN
            + "=?";

    public RoundHoleDAO(Context context) {
        super(context);
    }

    /**
     * Add RoundHole to the DB
     *
     * @param roundhole
     * @return
     */
    public long createRoundHole(RoundHole roundhole) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.ROUNDID_COLUMN, roundhole.getRoundID());
        values.put(DataBaseHelper.COURSEHOLEID_COLUMN, roundhole.getCourseHoleID());
        values.put(DataBaseHelper.PLAYERID_COLUMN, roundhole.getPlayerID());

        if (roundhole.getScore() > 0) {
            values.put(DataBaseHelper.SCORE_COLUMN, roundhole.getScore());
        }
        if (roundhole.getPutts() > 0) {
            values.put(DataBaseHelper.PUTTS_COLUMN, roundhole.getPutts());
        }
        //\todo change score_column to penalties
        if (roundhole.getPenalties() > 0) {
            values.put(DataBaseHelper.SCORE_COLUMN, roundhole.getPenalties());
        }
        if (roundhole.getFairways() > 0) {
            values.put(DataBaseHelper.PUTTS_COLUMN, roundhole.getFairways());
        }

        return database.insert(DataBaseHelper.ROUNDHOLE_TABLE, null, values);
    }

    /**
     * @param roundhole
     * @return
     */
    public long updateRoundHole(RoundHole roundhole) {
        if (roundhole.getID() < 0) {
            throw new RuntimeException("RoundHoleID not set in RoundHoldDAO.updateRoundHole()");
        }

        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.ROUNDID_COLUMN, roundhole.getRoundID());
        values.put(DataBaseHelper.COURSEHOLEID_COLUMN, roundhole.getCourseHoleID());
        values.put(DataBaseHelper.PLAYERID_COLUMN, roundhole.getPlayerID());

        if (roundhole.getScore() > 0) {
            values.put(DataBaseHelper.SCORE_COLUMN, roundhole.getScore());
        }
        if (roundhole.getPutts() > 0) {
            values.put(DataBaseHelper.PUTTS_COLUMN, roundhole.getPutts());
        }
        if (roundhole.getPenalties() > 0) {
            values.put(DataBaseHelper.SCORE_COLUMN, roundhole.getPenalties());
        }
        if (roundhole.getFairways() > 0) {
            values.put(DataBaseHelper.PUTTS_COLUMN, roundhole.getFairways());
        }

        return database.update(DataBaseHelper.ROUNDHOLE_TABLE,
                values,
                WHERE_ROUNDHOLEID_EQUALS,
                new String[]{String.valueOf(roundhole.getID())});
    }

    /**
     * @param roundhole
     * @return
     */
    public long deleteRoundHole(RoundHole roundhole) {
        if (roundhole.getID() < 0) {
            throw new RuntimeException("RoundHoleID not set in RoundHoleDAO.deleteRoundHole()");
        }

        return database.delete(DataBaseHelper.ROUNDHOLE_TABLE,
                WHERE_ROUNDHOLEID_EQUALS,
                new String[]{String.valueOf(roundhole.getID())});
    }

    /**
     * @param player
     * @return
     */
    public List<RoundHole> readListofRoundHolePlayer(Player player) {
        if (player.getID() < 0) {
            throw new RuntimeException("PlayerID not set in RoundHoleDAO.readPlayerRounds()");
        }

        Cursor cursor = database.query(DataBaseHelper.ROUNDHOLE_TABLE,
                new String[]{DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.ROUNDID_COLUMN,
                        DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.PLAYERID_COLUMN,
                        DataBaseHelper.SCORE_COLUMN,
                        DataBaseHelper.PUTTS_COLUMN,
                        DataBaseHelper.PENALTIES_COLUMN,
                        DataBaseHelper.FAIRWAYS_COLUMN},
                WHERE_PLAYERID_EQUALS,
                new String[]{String.valueOf(player.getID())},
                null, null, null, null);

        List<RoundHole> roundHoles = new ArrayList<RoundHole>();
        while (cursor.moveToNext()) {
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(0));
            Round round = new Round();
            round.setID(cursor.getLong(1));
            roundHole.setRoundID(round);
            CourseHole courseHole = new CourseHole();
            courseHole.setID(cursor.getLong(2));
            roundHole.setCourseHoleID(courseHole);
            Player playernew = new Player();
            playernew.setID(cursor.getLong(3));
            roundHole.setPlayerID(playernew);
            roundHole.setScore(cursor.getInt(4));
            roundHole.setPutts(cursor.getInt(5));
            roundHole.setPenalties(cursor.getInt(6));
            roundHole.setFairways(cursor.getInt(7));

            roundHoles.add(roundHole);
        }
        cursor.close();
        return roundHoles;
    }

    /**
     *
     * @param round
     * @return
     */
    public List<RoundHole> readListofRoundHoleRound(Round round) {
        if (round.getID() < 0) {
            throw new RuntimeException("RoundID not set in RoundHoleDAO.readPlayerRounds()");
        }

        Cursor cursor = database.query(DataBaseHelper.ROUNDHOLE_TABLE,
                new String[]{DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.ROUNDID_COLUMN,
                        DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.PLAYERID_COLUMN,
                        DataBaseHelper.SCORE_COLUMN,
                        DataBaseHelper.PUTTS_COLUMN,
                        DataBaseHelper.PENALTIES_COLUMN,
                        DataBaseHelper.FAIRWAYS_COLUMN},
                WHERE_ROUNDID_EQUALS,
                new String[]{String.valueOf(round.getID())},
                null, null, null, null);

        List<RoundHole> roundHoles = new ArrayList<RoundHole>();
        while (cursor.moveToNext()) {
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(0));
            Round roundnew = new Round();
            roundnew.setID(cursor.getLong(1));
            roundHole.setRoundID(roundnew);
            CourseHole courseHole = new CourseHole();
            courseHole.setID(cursor.getLong(2));
            roundHole.setCourseHoleID(courseHole);
            Player player = new Player();
            player.setID(cursor.getLong(3));
            roundHole.setPlayerID(player);
            roundHole.setScore(cursor.getInt(4));
            roundHole.setPutts(cursor.getInt(5));
            roundHole.setPenalties(cursor.getInt(6));
            roundHole.setFairways(cursor.getInt(7));

            roundHoles.add(roundHole);
        }
        cursor.close();
        return roundHoles;
    }

    /**
     *
     * @param courseHole
     * @return
     */
    public List<RoundHole> readListofRoundHoleCourseHole(CourseHole courseHole) {
        if (courseHole.getID() < 0) {
            throw new RuntimeException("CourseHoleID not set in RoundHoleDAO.readPlayerRounds()");
        }

        Cursor cursor = database.query(DataBaseHelper.ROUNDHOLE_TABLE,
                new String[]{DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.ROUNDID_COLUMN,
                        DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.PLAYERID_COLUMN,
                        DataBaseHelper.SCORE_COLUMN,
                        DataBaseHelper.PUTTS_COLUMN,
                        DataBaseHelper.PENALTIES_COLUMN,
                        DataBaseHelper.FAIRWAYS_COLUMN},
                WHERE_COURSEHOLEID_EQUALS,
                new String[]{String.valueOf(courseHole.getID())},
                null, null, null, null);

        List<RoundHole> roundHoles = new ArrayList<RoundHole>();
        while (cursor.moveToNext()) {
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(0));
            Round round = new Round();
            round.setID(cursor.getLong(1));
            roundHole.setRoundID(round);
            CourseHole courseHolenew = new CourseHole();
            courseHolenew.setID(cursor.getLong(2));
            roundHole.setCourseHoleID(courseHolenew);
            Player player = new Player();
            player.setID(cursor.getLong(3));
            roundHole.setPlayerID(player);
            roundHole.setScore(cursor.getInt(4));
            roundHole.setPutts(cursor.getInt(5));
            roundHole.setPenalties(cursor.getInt(6));
            roundHole.setFairways(cursor.getInt(7));

            roundHoles.add(roundHole);
        }
        cursor.close();
        return roundHoles;
    }

    /**
     *
     * @param round
     * @param player
     * @return
     */
    public List<RoundHole> readListofRoundHoleRoundPlayer(Round round, Player player){
        if (round.getID() < 0 || player.getID() < 0){
            throw new RuntimeException("RoundID or playerID not set in readListofRoundHoleRoundPlayer()");
        }

        Cursor cursor = database.query(DataBaseHelper.ROUND_TABLE,
                new String[]{DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.ROUNDID_COLUMN,
                        DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.PLAYERID_COLUMN,
                        DataBaseHelper.SCORE_COLUMN,
                        DataBaseHelper.PUTTS_COLUMN,
                        DataBaseHelper.PENALTIES_COLUMN,
                        DataBaseHelper.FAIRWAYS_COLUMN},
                WHERE_ROUNDID_EQUALS + " AND " + WHERE_PLAYERID_EQUALS,
                new String[]{String.valueOf(round.getID()),String.valueOf(player.getID())},
                null, null, null, null);

        List<RoundHole> roundHoles = new ArrayList<RoundHole>();
        while (cursor.moveToNext()) {
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(0));
            Round roundnew = new Round();
            roundnew.setID(cursor.getLong(1));
            roundHole.setRoundID(roundnew);
            CourseHole courseHole = new CourseHole();
            courseHole.setID(cursor.getLong(2));
            roundHole.setCourseHoleID(courseHole);
            Player playernew = new Player();
            playernew.setID(cursor.getLong(3));
            roundHole.setPlayerID(playernew);
            roundHole.setScore(cursor.getInt(4));
            roundHole.setPutts(cursor.getInt(5));
            roundHole.setPenalties(cursor.getInt(6));
            roundHole.setFairways(cursor.getInt(7));

            roundHoles.add(roundHole);
        }
        cursor.close();
        return roundHoles;
    }

    //\todo add given CourseHoleID and PlayerID return list of RoundHoles

    //\todo add given RoundID and CourseHoleID return list of RoundHoles
}






















