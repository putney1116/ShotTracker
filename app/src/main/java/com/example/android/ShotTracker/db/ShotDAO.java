package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Club;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.RoundHole;
import com.example.android.ShotTracker.objects.Shot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ewjensen on 2/7/15.
 */
public class ShotDAO extends ShotTrackerDBDAO {

    private static final String WHERE_SHOTID_EQUALS = DataBaseHelper.SHOTID_COLUMN
            + "=?";

    private static final String WHERE_ROUNDHOLEID_EQUALS = DataBaseHelper.ROUNDHOLEID_COLUMN
            + "=?";

    private static final String WHERE_CLUBID_EQUALS = DataBaseHelper.CLUBID_COLUMN
            + "=?";

    private static final String WHERE_PLAYERID_EQUALS = DataBaseHelper.PLAYERID_COLUMN
            + "=?";

    public ShotDAO(Context context) {
        super(context);
    }

    /**
     * create a shot entry in the database: every shot currently only requires
     * that it's associated with a roundholeID
     *
     * @param shot
     * @return
     */
    public long createShot(Shot shot) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.ROUNDHOLEID_COLUMN, shot.getRoundHoleID());
        if (shot.getClubID() > 0) {
            values.put(DataBaseHelper.CLUBID_COLUMN,shot.getClubID());
        }
        if (shot.getYards() > 0) {
            values.put(DataBaseHelper.YARDS_COLUMN, shot.getYards());
        }
        if (shot.getShotStartLat() > 0){
            values.put(DataBaseHelper.SHOTSTARTLAT_COLUMN, shot.getShotStartLat());
        }
        if (shot.getShotStartLong() > 0){
            values.put(DataBaseHelper.SHOTSTARTLONG_COLUMN, shot.getShotStartLong());
        }
        if (shot.getShotEndLat() > 0){
            values.put(DataBaseHelper.SHOTENDLAT_COLUMN, shot.getShotEndLat());
        }
        if (shot.getShotEndLong() > 0){
            values.put(DataBaseHelper.SHOTENDLONG_COLUMN, shot.getShotEndLong());
        }

        return database.insert(DataBaseHelper.SHOT_TABLE,null,values);
    }

    /**
     * update information for a shot entry in database given a shotID
     * @param shot
     * @return
     */
    public long updateShot(Shot shot){
        if (shot.getID() < 0){
            throw new RuntimeException("Shot ID not set in ShotDAO::updateShot()");
        }

        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.ROUNDHOLEID_COLUMN, shot.getRoundHoleID());
        if (shot.getClubID() > 0){
            values.put(DataBaseHelper.CLUBID_COLUMN, shot.getClubID());
        }
        if (shot.getYards() > 0) {
            values.put(DataBaseHelper.YARDS_COLUMN, shot.getYards());
        }
        if (shot.getShotStartLat() > 0){
            values.put(DataBaseHelper.SHOTSTARTLAT_COLUMN, shot.getShotStartLat());
        }
        if (shot.getShotStartLong() > 0){
            values.put(DataBaseHelper.SHOTSTARTLONG_COLUMN, shot.getShotStartLong());
        }
        if (shot.getShotEndLat() > 0){
            values.put(DataBaseHelper.SHOTENDLAT_COLUMN, shot.getShotEndLat());
        }
        if (shot.getShotEndLong() > 0){
            values.put(DataBaseHelper.SHOTENDLONG_COLUMN, shot.getShotEndLong());
        }

        return database.update(DataBaseHelper.SHOT_TABLE,
                values,
                WHERE_SHOTID_EQUALS,
                new String[]{String.valueOf(shot.getID())});
    }

    /**
     * delete shot given shotID
     * @param shot
     * @return
     */
    public long deleteShot(Shot shot){
        if (shot.getID() < 0){
            throw new RuntimeException("Shot ID not set in ShotDAO::deleteShot()");
        }

        return database.delete(DataBaseHelper.SHOT_TABLE,
                WHERE_SHOTID_EQUALS,
                new String[]{String.valueOf(shot.getID())});
    }

    /**
     * return shot given shotID
     * @param shot
     * @return
     */
    public Shot readShot(Shot shot){
        if (shot.getID() < 0){
            throw new RuntimeException("Shot ID not set in ShotDAO::readShot()");
        }

        Cursor cursor = database.query(DataBaseHelper.SHOT_TABLE,
                new String[]{DataBaseHelper.SHOTID_COLUMN,
                    DataBaseHelper.ROUNDHOLEID_COLUMN,
                    DataBaseHelper.CLUBID_COLUMN,
                    DataBaseHelper.YARDS_COLUMN,
                    DataBaseHelper.SHOTSTARTLAT_COLUMN,
                    DataBaseHelper.SHOTSTARTLONG_COLUMN,
                    DataBaseHelper.SHOTENDLAT_COLUMN,
                    DataBaseHelper.SHOTENDLONG_COLUMN},
                WHERE_SHOTID_EQUALS,
                new String[]{String.valueOf(shot.getID())},
                null,null,null);

        RoundHole roundHole = new RoundHole();
        roundHole.setID(cursor.getLong(1));
        shot.setRoundHoleID(roundHole);
        Club club = new Club();
        club.setID(cursor.getLong(2));
        shot.setClubID(club);
        shot.setYards(cursor.getInt(3));
        shot.setShotStartLat(cursor.getDouble(4));
        shot.setShotStartLong(cursor.getDouble(5));
        shot.setShotEndLat(cursor.getDouble(6));
        shot.setShotEndLong(cursor.getDouble(7));
        cursor.close();

        return shot;
    }

    /**
     * returns a list of all shots
     * @return List of Shot objects
     */
    public List<Shot> readListofShots(){
        List<Shot> shots = new ArrayList<Shot>();

        Cursor cursor = database.query(DataBaseHelper.SHOT_TABLE,
                new String[]{DataBaseHelper.SHOTID_COLUMN,
                        DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.CLUBID_COLUMN,
                        DataBaseHelper.YARDS_COLUMN,
                        DataBaseHelper.SHOTSTARTLAT_COLUMN,
                        DataBaseHelper.SHOTSTARTLONG_COLUMN,
                        DataBaseHelper.SHOTENDLAT_COLUMN,
                        DataBaseHelper.SHOTENDLONG_COLUMN},
                null,null,null,null,null);

        while (cursor.moveToNext()){
            Shot shot = new Shot();
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(1));
            shot.setRoundHoleID(roundHole);
            Club club = new Club();
            club.setID(cursor.getLong(2));
            shot.setClubID(club);
            shot.setYards(cursor.getInt(3));
            shot.setShotStartLat(cursor.getDouble(4));
            shot.setShotStartLong(cursor.getDouble(5));
            shot.setShotEndLat(cursor.getDouble(6));
            shot.setShotEndLong(cursor.getDouble(7));

            shots.add(shot);
        }
        cursor.close();

        return shots;
    }

    /**
     * returns a list of shots given a roundhole ID
     * @param roundHole
     * @return List of Shot objects
     */
    public List<Shot> readListofShots(RoundHole roundHole){
        if (roundHole.getID() < 0){
            throw new RuntimeException("RoundHole ID not set in "
                    + "ShotDAO::readListofShots(RoundHole)");
        }

        Cursor cursor = database.query(DataBaseHelper.SHOT_TABLE,
                new String[]{DataBaseHelper.SHOTID_COLUMN,
                        DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.CLUBID_COLUMN,
                        DataBaseHelper.YARDS_COLUMN,
                        DataBaseHelper.SHOTSTARTLAT_COLUMN,
                        DataBaseHelper.SHOTSTARTLONG_COLUMN,
                        DataBaseHelper.SHOTENDLAT_COLUMN,
                        DataBaseHelper.SHOTENDLONG_COLUMN},
                WHERE_ROUNDHOLEID_EQUALS,
                new String[]{String.valueOf(roundHole.getID())},
                null,null,null);

        List<Shot> shots = new ArrayList<Shot>();

        while (cursor.moveToNext()){
            Shot shot = new Shot();
            shot.setID(cursor.getLong(0));
            //I got lazy... used the passed in roundHole for its ID
            shot.setRoundHoleID(roundHole);
            Club club = new Club();
            club.setID(cursor.getLong(2));
            shot.setClubID(club);
            shot.setYards(cursor.getInt(3));
            shot.setShotStartLat(cursor.getDouble(4));
            shot.setShotStartLong(cursor.getDouble(5));
            shot.setShotEndLat(cursor.getDouble(6));
            shot.setShotEndLong(cursor.getDouble(7));

            shots.add(shot);
        }
        cursor.close();

        return shots;
    }

    /**
     * returns list of shots given a club ID
     * @param club
     * @return List of Shot objects
     */
    public List<Shot> readListofShots(Club club){
        if (club.getID() < 0){
            throw new RuntimeException("Club ID not set in "
                    + "ShotDAO::readListofShots(Club)");
        }

        Cursor cursor = database.query(DataBaseHelper.SHOT_TABLE,
                new String[]{DataBaseHelper.SHOTID_COLUMN,
                        DataBaseHelper.ROUNDHOLEID_COLUMN,
                        DataBaseHelper.CLUBID_COLUMN,
                        DataBaseHelper.YARDS_COLUMN,
                        DataBaseHelper.SHOTSTARTLAT_COLUMN,
                        DataBaseHelper.SHOTSTARTLONG_COLUMN,
                        DataBaseHelper.SHOTENDLAT_COLUMN,
                        DataBaseHelper.SHOTENDLONG_COLUMN},
                WHERE_CLUBID_EQUALS,
                new String[]{String.valueOf(club.getID())},
                null,null,null);

        List<Shot> shots = new ArrayList<Shot>();

        while (cursor.moveToNext()){
            Shot shot = new Shot();
            shot.setID(cursor.getLong(0));
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(1));
            shot.setRoundHoleID(roundHole);
            //I got lazy... used the passed in club for its ID
            shot.setClubID(club);
            shot.setYards(cursor.getInt(3));
            shot.setShotStartLat(cursor.getDouble(4));
            shot.setShotStartLong(cursor.getDouble(5));
            shot.setShotEndLat(cursor.getDouble(6));
            shot.setShotEndLong(cursor.getDouble(7));

            shots.add(shot);
        }
        cursor.close();

        return shots;
    }

    /**
     * Read a list of shots given a club and player
     * @param club
     * @param player
     * @return List of Shot objects
     */
    public List<Shot> readListofShots(Club club, Player player){
        if (club.getID() < 0){
            throw new RuntimeException("Club ID not set in "
                    + "ShotDAO::readListofShots(Club, Player)");
        }
        if (player.getID() < 0){
            throw new RuntimeException("Player ID not set in "
                    + "ShotDAO::readListofShots(Club, Player)");
        }

        String query = "SELECT "
                + DataBaseHelper.SHOTID_COLUMN + ", "
                + DataBaseHelper.ROUNDHOLEID_COLUMN + ", "
                + DataBaseHelper.CLUBID_COLUMN + ", "
                + DataBaseHelper.YARDS_COLUMN + ", "
                + DataBaseHelper.SHOTSTARTLAT_COLUMN + ", "
                + DataBaseHelper.SHOTSTARTLONG_COLUMN + ", "
                + DataBaseHelper.SHOTENDLAT_COLUMN + ", "
                + DataBaseHelper.SHOTENDLONG_COLUMN
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " WHERE "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID();

        Cursor cursor = database.rawQuery(query, null);

        List<Shot> shots = new ArrayList<Shot>();

        while (cursor.moveToNext()){
            Shot shot = new Shot();
            shot.setID(cursor.getLong(0));
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(1));
            shot.setRoundHoleID(roundHole);
            //I got lazy... used the passed in club for its ID
            shot.setClubID(club);
            shot.setYards(cursor.getInt(3));
            shot.setShotStartLat(cursor.getDouble(4));
            shot.setShotStartLong(cursor.getDouble(5));
            shot.setShotEndLat(cursor.getDouble(6));
            shot.setShotEndLong(cursor.getDouble(7));

            shots.add(shot);
        }
        cursor.close();

        return shots;
    }

    /**
     * Read a list of shots given a club, player, and course
     * @param club
     * @param player
     * @param course
     * @return List of Shot objects
     */
    public List<Shot> readListofShots(Club club, Player player, Course course){
        if (club.getID() < 0){
            throw new RuntimeException("Club ID not set in "
                    + "ShotDAO::readListofShots(Club, Player, Course)");
        }
        if (player.getID() < 0){
            throw new RuntimeException("Player ID not set in "
                    + "ShotDAO::readListofShots(Club, Player, Course)");
        }
        if (course.getID() < 0){
            throw new RuntimeException("Course ID not set in "
                    + "ShotDAO::readListofShots(Club, Player, Course)");
        }


        String query = "SELECT "
                + DataBaseHelper.SHOTID_COLUMN + ", "
                + DataBaseHelper.ROUNDHOLEID_COLUMN + ", "
                + DataBaseHelper.CLUBID_COLUMN + ", "
                + DataBaseHelper.YARDS_COLUMN + ", "
                + DataBaseHelper.SHOTSTARTLAT_COLUMN + ", "
                + DataBaseHelper.SHOTSTARTLONG_COLUMN + ", "
                + DataBaseHelper.SHOTENDLAT_COLUMN + ", "
                + DataBaseHelper.SHOTENDLONG_COLUMN
                + " FROM "
                + DataBaseHelper.SHOT_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.ROUNDHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.COURSEHOLE_TABLE
                + " NATURAL JOIN "
                + DataBaseHelper.SUBCOURSE_TABLE
                + " WHERE "
                + DataBaseHelper.CLUBID_COLUMN
                + "=" + club.getID()
                + " AND "
                + DataBaseHelper.PLAYERID_COLUMN
                + "=" + player.getID()
                + " AND "
                + DataBaseHelper.COURSEID_COLUMN
                + "=" + course.getID();

        Cursor cursor = database.rawQuery(query, null);

        List<Shot> shots = new ArrayList<Shot>();

        while (cursor.moveToNext()){
            Shot shot = new Shot();
            shot.setID(cursor.getLong(0));
            RoundHole roundHole = new RoundHole();
            roundHole.setID(cursor.getLong(1));
            shot.setRoundHoleID(roundHole);
            //I got lazy... used the passed in club for its ID
            shot.setClubID(club);
            shot.setYards(cursor.getInt(3));
            shot.setShotStartLat(cursor.getDouble(4));
            shot.setShotStartLong(cursor.getDouble(5));
            shot.setShotEndLat(cursor.getDouble(6));
            shot.setShotEndLong(cursor.getDouble(7));

            shots.add(shot);
        }
        cursor.close();

        return shots;
    }






}

































