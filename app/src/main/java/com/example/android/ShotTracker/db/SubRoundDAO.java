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
import com.example.android.ShotTracker.objects.SubRound;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ewjensen on 1/14/15.
 */
public class SubRoundDAO extends ShotTrackerDBDAO {

    private static final String WHERE_SUBROUNDID_EQUALS = DataBaseHelper.SUBROUNDID_COLUMN
            + "=?";

    private static final String WHERE_SUBCOURSEID_EQUALS = DataBaseHelper.SUBCOURSEID_COLUMN
            + "=?";

    public SubRoundDAO(Context context) { super(context); }

    /**
     *
     * @param subRound
     * @return
     */
    public long createRound(SubRound subRound) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.SUBCOURSEID_COLUMN, subRound.getSubCourseID());

        values.put(DataBaseHelper.ROUNDID_COLUMN, subRound.getRoundID());

        return database.insert(DataBaseHelper.SUBROUND_TABLE, null, values);
    }

    /**
     *
     * @param subRound
     * @return
     */
    public long updateRound(SubRound subRound) {
        ContentValues values = new ContentValues();

        //make sure that we have an id to update
        if (subRound.getID() < 0) {
            throw new RuntimeException("Round ID not set in RoundDAO.updateRound()");
        }

        values.put(DataBaseHelper.SUBCOURSEID_COLUMN, subRound.getSubCourseID());

        values.put(DataBaseHelper.ROUNDID_COLUMN, subRound.getRoundID());

        return database.update(DataBaseHelper.SUBROUND_TABLE,
                values,
                WHERE_SUBROUNDID_EQUALS,
                new String[]{String.valueOf(subRound.getID())});
    }

    /**
     *
     * @param subRound
     * @return
     */
    public long deleteRound(SubRound subRound){
        if (subRound.getID() < 0) {
            throw new RuntimeException("Round ID not set in RoundDAO.deleteRound()");
        }

        return database.delete(DataBaseHelper.SUBROUND_TABLE,
                WHERE_SUBROUNDID_EQUALS,
                new String[]{String.valueOf(subRound.getID())});
    }

    /**
     *
     * @return
     */
    public List<SubRound> readListofSubRounds() {
        List<SubRound> subRounds = new ArrayList<SubRound>();

        Cursor cursor = database.query(DataBaseHelper.SUBROUND_TABLE,
                new String[] {DataBaseHelper.SUBROUNDID_COLUMN,
                              DataBaseHelper.SUBCOURSEID_COLUMN,
                              DataBaseHelper.ROUNDID_COLUMN},
                null,null,null,null,null);

        while (cursor.moveToNext()) {
            SubRound subRound = new SubRound();
            subRound.setID(cursor.getLong(0));
            SubCourse subCourse = new SubCourse();
            subCourse.setID(cursor.getLong(1));
            subRound.setSubCourseID(subCourse);
            Round round = new Round();
            round.setID(cursor.getLong(2));
            subRound.setRoundID(round);

            subRounds.add(subRound);
        }
        cursor.close();
        return subRounds;
    }

    /**
     *
     * @param subRound
     * @return
     */
    public SubRound readSubRound(SubRound subRound){
        if (subRound.getID() < 0 ){
            throw new RuntimeException("Round ID not set in RoundDAO::readRound()");
        }

        Cursor cursor = database.query(DataBaseHelper.SUBROUND_TABLE,
                new String[] {DataBaseHelper.SUBROUNDID_COLUMN,
                              DataBaseHelper.SUBCOURSEID_COLUMN,
                              DataBaseHelper.ROUNDID_COLUMN},
                WHERE_SUBROUNDID_EQUALS,
                new String[] {String.valueOf(subRound.getID())},
                null,null,null);

        //\todo need to add the while (cursor.movetonext()) or else doesn't work
        SubCourse subCourse = new SubCourse();
        subCourse.setID(cursor.getLong(1));
        subRound.setSubCourseID(subCourse);
        Round round = new Round();
        round.setID(cursor.getLong(2));
        subRound.setRoundID(round);

        cursor.close();

        return subRound;
    }

    public SubRound readSubRoundSubCourse (SubCourse subCourse){
        if (subCourse.getID() < 0 ){
            throw new RuntimeException("SubCourse ID not set in RoundDAO::readRoundSubCourse()");
        }

        Cursor cursor = database.query(DataBaseHelper.SUBROUND_TABLE,
                new String[] {DataBaseHelper.SUBROUNDID_COLUMN,
                DataBaseHelper.SUBCOURSEID_COLUMN,
                DataBaseHelper.ROUNDID_COLUMN},
                WHERE_SUBCOURSEID_EQUALS,
                new String[]{String.valueOf(subCourse.getID())},
                null,null,null);

        SubRound subRound = new SubRound();
        subRound.setID(cursor.getLong(0));
        //currently set to use the passed in SubCourse to assign the id to the returned round
        //could change to use what is spit out of the database. Probably should?
        subRound.setSubCourseID(subCourse);
        Round round = new Round();
        round.setID(cursor.getLong(2));
        subRound.setRoundID(round);
        cursor.close();

        return subRound;
    }
}























