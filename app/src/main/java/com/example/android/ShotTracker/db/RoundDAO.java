package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;
import com.example.android.ShotTracker.objects.Shot;
import com.example.android.ShotTracker.objects.ShotType;
import com.example.android.ShotTracker.objects.SubCourse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ewjensen on 1/14/15.
 */
public class RoundDAO extends ShotTrackerDBDAO {

    private static final String WHERE_ROUNDID_EQUALS = DataBaseHelper.ROUNDID_COLUMN
            + "=?";

    private static final String WHERE_SUBCOURSEID_EQUALS = DataBaseHelper.SUBCOURSEID_COLUMN
            + "=?";

    public RoundDAO(Context context) { super(context); }

    /**
     *
     * @param round
     * @return
     */
    public long createRound(Round round) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.SUBCOURSEID_COLUMN, round.getSubCourseID());

        values.put(DataBaseHelper.ROUNDDATE_COLUMN, round.getDate().getTime());

        return database.insert(DataBaseHelper.ROUND_TABLE, null, values);
    }

    /**
     *
     * @param round
     * @return
     */
    public long updateRound(Round round) {
        ContentValues values = new ContentValues();

        //make sure that we have an id to update
        if (round.getID() < 0) {
            throw new RuntimeException("Round ID not set in RoundDAO.updateRound()");
        }

        values.put(DataBaseHelper.SUBCOURSEID_COLUMN, round.getSubCourseID());

        values.put(DataBaseHelper.ROUNDDATE_COLUMN, round.getDate().getTime());

        return database.update(DataBaseHelper.ROUND_TABLE,
                values,
                WHERE_ROUNDID_EQUALS,
                new String[]{String.valueOf(round.getID())});
    }

    /**
     *
     * @param round
     * @return
     */
    public long deleteRound(Round round){
        if (round.getID() < 0) {
            throw new RuntimeException("Round ID not set in RoundDAO.deleteRound()");
        }

        return database.delete(DataBaseHelper.ROUND_TABLE,
                WHERE_ROUNDID_EQUALS,
                new String[]{String.valueOf(round.getID())});
    }

    /**
     *
     * @return
     */
    public List<Round> readListofRounds() {
        List<Round> rounds = new ArrayList<Round>();

        Cursor cursor = database.query(DataBaseHelper.ROUND_TABLE,
                new String[] {DataBaseHelper.ROUNDID_COLUMN,
                              DataBaseHelper.SUBCOURSEID_COLUMN,
                              DataBaseHelper.ROUNDDATE_COLUMN},
                null,null,null,null,null);

        while (cursor.moveToNext()) {
            Round round = new Round();
            round.setID(cursor.getLong(0));
            SubCourse subCourse = new SubCourse();
            subCourse.setID(cursor.getLong(1));
            round.setSubCourseID(subCourse);
            round.setDate(new Date(cursor.getLong(2)));

            rounds.add(round);
        }
        cursor.close();
        return rounds;
    }

    /**
     *
     * @param round
     * @return
     */
    public Round readRound(Round round){
        if (round.getID() < 0 ){
            throw new RuntimeException("Round ID not set in RoundDAO::readRound()");
        }

        Cursor cursor = database.query(DataBaseHelper.ROUND_TABLE,
                new String[] {DataBaseHelper.ROUNDID_COLUMN,
                              DataBaseHelper.SUBCOURSEID_COLUMN,
                              DataBaseHelper.ROUNDDATE_COLUMN},
                WHERE_ROUNDID_EQUALS,
                new String[] {String.valueOf(round.getID())},
                null,null,null);

        //\todo need to add the while (cursor.movetonext()) or else doesn't work
        SubCourse subCourse = new SubCourse();
        subCourse.setID(cursor.getLong(1));
        round.setSubCourseID(subCourse);
        round.setDate(new Date(cursor.getLong(2)));
        cursor.close();

        return round;
    }

    public Round readRoundSubCourse (SubCourse subCourse){
        if (subCourse.getID() < 0 ){
            throw new RuntimeException("SubCourse ID not set in RoundDAO::readRoundSubCourse()");
        }

        Cursor cursor = database.query(DataBaseHelper.ROUND_TABLE,
                new String[] {DataBaseHelper.ROUNDID_COLUMN,
                DataBaseHelper.SUBCOURSEID_COLUMN,
                DataBaseHelper.ROUNDDATE_COLUMN},
                WHERE_SUBCOURSEID_EQUALS,
                new String[]{String.valueOf(subCourse.getID())},
                null,null,null);

        Round round = new Round();
        round.setID(cursor.getLong(0));
        //currently set to use the passed in SubCourse to assign the id to the returned round
        //could change to use what is spit out of the database. Probably should?
        round.setSubCourseID(subCourse);
        round.setDate(new Date(cursor.getLong(2)));
        cursor.close();

        return round;
    }
}























