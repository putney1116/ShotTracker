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

    public long deleteRound(Round round){
        if (round.getID() < 0) {
            throw new RuntimeException("Round ID not set in RoundDAO.deleteRound()");
        }

        return database.delete(DataBaseHelper.ROUND_TABLE,
                WHERE_ROUNDID_EQUALS,
                new String[]{String.valueOf(round.getID())});
    }

    public List<Round> readListofRounds() {
        List<Round> rounds = new ArrayList<Round>();

        Cursor cursor = database.query(DataBaseHelper.ROUND_TABLE,
                new String[] {DataBaseHelper.ROUNDID_COLUMN,
                              DataBaseHelper.SUBCOURSEID_COLUMN,
                              DataBaseHelper.ROUNDDATE_COLUMN},
                null,null,null,null,null);

        while (cursor.moveToNext()) {
            Round round = new Round();
            round.setID(cursor.getInt(0));
            SubCourse subCourse = new SubCourse();
            subCourse.setID(cursor.getInt(1));
            round.setSubCourseID(subCourse);
            round.setDate(new Date(cursor.getLong(2)));

            rounds.add(round);
        }

        return rounds;
    }
}























