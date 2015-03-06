package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 1/7/15.
 */
public class CourseHoleInfoDAO extends ShotTrackerDBDAO {

    private static final String WHERE_COURSEHOLEINFOID_EQUALS =
            DataBaseHelper.COURSEHOLEINFOID_COLUMN + "=?";
    private static final String WHERE_COURSEHOLEID_EQUALS = DataBaseHelper.COURSEHOLEID_COLUMN
            + "=?";

    public CourseHoleInfoDAO(Context context){ super(context); }

    /**
     * Add a CourseHoleInfo to the DB
     * @param courseHoleInfo CourseHoleInfo to be added to DB
     * @return
     */
    public long createCourseHoleInfo(CourseHoleInfo courseHoleInfo) {
        ContentValues values = new ContentValues();

        //Log.d("Test", "F/M/B = " + courseHoleInfo.getCourseHoleID() + ", HoleNumber = " + holeNumber);
        //Log.d("test", "Last: "+ courseHoleInfo.getLongitude());

        //\todo check that courseHoleID is set before using
        values.put(DataBaseHelper.COURSEHOLEID_COLUMN, courseHoleInfo.getCourseHoleID());
        values.put(DataBaseHelper.INFO_COLUMN, courseHoleInfo.getInfo());
        values.put(DataBaseHelper.INFOLATITUDE_COLUMN, courseHoleInfo.getLatitude());
        values.put(DataBaseHelper.INFOLONGITUDE_COLUMN, courseHoleInfo.getLongitude());

        return database.insert(DataBaseHelper.COURSEHOLEINFO_TABLE, null, values);
    }

    /**
     * Updated CourseHoleInfo information in the DB
     * @param courseHoleInfo Info to be updated
     * @return
     */
    public long updateCourseHoleInfo(CourseHoleInfo courseHoleInfo) {
        //make sure courseHoleInfoID is set
        if (courseHoleInfo.getID() < 0) {
            throw new RuntimeException("CourseHoleInfoID is not set in"
                    + " CourseDAO.updateCourseHoleInfo()");
        }
        ContentValues values = new ContentValues();

        //\todo check that courseHoleID is set before using
        values.put(DataBaseHelper.COURSEHOLEID_COLUMN, courseHoleInfo.getCourseHoleID());
        values.put(DataBaseHelper.INFO_COLUMN, courseHoleInfo.getInfo());
        values.put(DataBaseHelper.INFOLATITUDE_COLUMN, courseHoleInfo.getLatitude());
        values.put(DataBaseHelper.INFOLONGITUDE_COLUMN, courseHoleInfo.getLongitude());

        return database.update(DataBaseHelper.COURSEHOLEINFO_TABLE,
                values,
                WHERE_COURSEHOLEINFOID_EQUALS,
                new String[] {String.valueOf(courseHoleInfo.getID())});
    }

    /**
     * Delete CourseHoleInfo from the DB
     * @param courseHoleInfo
     * @return
     */
    public long deleteCourseHoleInfo(CourseHoleInfo courseHoleInfo) {
        //make sure courseHoleInfoID is set
        if (courseHoleInfo.getID() < 0) {
            throw new RuntimeException("CourseHoleInfoID is not set in"
                    + " CourseDAO.deleteCourseHoleInfo()");
        }

        return database.delete(DataBaseHelper.COURSEHOLEINFO_TABLE,
                WHERE_COURSEHOLEINFOID_EQUALS,
                new String[] {String.valueOf(courseHoleInfo.getID())});
    }


    public List<CourseHoleInfo> readListofCourseHoleInfos(CourseHole courseHole) {
        //make sure courseHoleID is set
        if (courseHole.getID() < 0) {
            throw new RuntimeException("CourseHoleID not set in"
                    + " CourseDAO.readListofCourseHoleInfos()");
        }

        Cursor cursor = database.query(DataBaseHelper.COURSEHOLEINFO_TABLE,
                new String[] {DataBaseHelper.COURSEHOLEINFOID_COLUMN,
                        DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.INFO_COLUMN,
                        DataBaseHelper.INFOLATITUDE_COLUMN,
                        DataBaseHelper.INFOLONGITUDE_COLUMN},
                WHERE_COURSEHOLEID_EQUALS,
                new String[] {String.valueOf(courseHole.getID())},
                null, null, null, null);

        List<CourseHoleInfo> courseHoleInfos = new ArrayList<CourseHoleInfo>();
        while(cursor.moveToNext()) {
            CourseHoleInfo courseHoleInfo = new CourseHoleInfo();
            courseHoleInfo.setID(cursor.getLong(0));
            courseHoleInfo.setCourseHoleID(courseHole);
            courseHoleInfo.setInfo(cursor.getString(2));
            courseHoleInfo.setLatitude(cursor.getDouble(3));
            courseHoleInfo.setLongitude(cursor.getDouble(4));

            courseHoleInfos.add(courseHoleInfo);
        }
        cursor.close();

        return courseHoleInfos;
    }

}
