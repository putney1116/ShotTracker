package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.SubCourse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 1/7/15.
 */
public class CourseHoleDAO extends ShotTrackerDBDAO {

    private static final String WHERE_COURSEHOLEID_EQUALS = DataBaseHelper.COURSEHOLEID_COLUMN
            + "=?";
    private static final String WHERE_SUBCOURSEID_EQUALS = DataBaseHelper.SUBCOURSEID_COLUMN
            + "=?";


    public CourseHoleDAO(Context context){ super(context); }

    /**
     * Add a CourseHole to the DB
     * @param courseHole Course Hole to be added
     * @return
     */
    public long createCourseHole(CourseHole courseHole) {
        ContentValues values = new ContentValues();

        if (courseHole.getSubCourseID() < 0){
            throw new RuntimeException("SubCourseID not set in CourseHoleDAO.createCourseHole()");
        }

        values.put(DataBaseHelper.SUBCOURSEID_COLUMN, courseHole.getSubCourseID());
        values.put(DataBaseHelper.COURSEHOLENUMBER_COLUMN, courseHole.getHoleNumber());
        values.put(DataBaseHelper.PAR_COLUMN, courseHole.getPar());

        if (courseHole.getWPar() > 0) {
            values.put(DataBaseHelper.WOMENPAR_COLUMN, courseHole.getWPar());
        }
        else {
            values.put(DataBaseHelper.WOMENPAR_COLUMN, courseHole.getPar());
        }

        if (courseHole.getMenHandicap() > 0) {
            values.put(DataBaseHelper.MENHANDICAP_COLUMN, courseHole.getMenHandicap());
        }
        if (courseHole.getWomenHandicap() > 0) {
            values.put(DataBaseHelper.WOMENHANDICAP_COLUMN, courseHole.getWomenHandicap());
        }
        if (courseHole.getBlueYardage() > 0) {
            values.put(DataBaseHelper.BLUEYARD_COLUMN, courseHole.getBlueYardage());
        }
        if (courseHole.getWhiteYardage() > 0) {
            values.put(DataBaseHelper.WHITEYARD_COLUMN, courseHole.getWhiteYardage());
        }
        if (courseHole.getRedYardage() > 0) {
            values.put(DataBaseHelper.REDYARD_COLUMN, courseHole.getRedYardage());
        }

        return database.insert(DataBaseHelper.COURSEHOLE_TABLE, null, values);
    }

    /**
     * Update a CourseHole in the DB. CourseHoleID must be set or a RuntimeException is thrown
     * @param courseHole CourseHole object containing the updated values
     * @return
     */
    public long updateCourseHole(CourseHole courseHole) {
        //First confirm that we have a courseHoleID to acces
        if (courseHole.getID() < 0) {
            throw new RuntimeException("CourseHoleID not set in CourseDAO.updateCourseHole()");
        }

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.SUBCOURSEID_COLUMN, courseHole.getSubCourseID());
        values.put(DataBaseHelper.COURSEHOLENUMBER_COLUMN, courseHole.getHoleNumber());
        values.put(DataBaseHelper.PAR_COLUMN, courseHole.getPar());

        if (courseHole.getWPar() > 0) {
            values.put(DataBaseHelper.WOMENPAR_COLUMN, courseHole.getWPar());
        }
        else {
            values.put(DataBaseHelper.WOMENPAR_COLUMN, courseHole.getPar());
        }

        if (courseHole.getMenHandicap() > 0) {
            values.put(DataBaseHelper.MENHANDICAP_COLUMN, courseHole.getMenHandicap());
        }
        if (courseHole.getWomenHandicap() > 0) {
            values.put(DataBaseHelper.WOMENHANDICAP_COLUMN, courseHole.getWomenHandicap());
        }
        if (courseHole.getBlueYardage() > 0) {
            values.put(DataBaseHelper.BLUEYARD_COLUMN, courseHole.getBlueYardage());
        }
        if (courseHole.getWhiteYardage() > 0) {
            values.put(DataBaseHelper.WHITEYARD_COLUMN, courseHole.getWhiteYardage());
        }
        if (courseHole.getRedYardage() > 0) {
            values.put(DataBaseHelper.REDYARD_COLUMN, courseHole.getRedYardage());
        }

        return database.update(DataBaseHelper.COURSEHOLE_TABLE,
                values,
                WHERE_COURSEHOLEID_EQUALS,
                new String[] {String.valueOf(courseHole.getID())});
    }

    /**
     * Delete a CourseHole from the DB. CourseHoleID must be set or a RuntimeException is thrown.
     * @param courseHole CourseHole to be deleted
     * @return
     */
    public long deleteCourseHole(CourseHole courseHole) {
        //First confirm that we have a courseHoleID to acces
        if (courseHole.getID() < 0) {
            throw new RuntimeException("CourseHoleID not set in CourseDAO.deleteCourseHole()");
        }

        return database.delete(DataBaseHelper.COURSEHOLE_TABLE,
                WHERE_COURSEHOLEID_EQUALS,
                new String[] {String.valueOf(courseHole.getID())});
    }

    /**
     * Read a list of CourseHole's associated with a given SubCourse from the DB
     * @param subCourse Get CourseHole's associated with subCourse
     * @return
     */
    public List<CourseHole> readListofCourseHoles(SubCourse subCourse) {
        // first check that the subcourseID is set
        if (subCourse.getID() < 0) {
            throw new RuntimeException("SubCourseID not set in CourseDAO.readListofCourseHoles()");
        }

        Cursor cursor = database.query(DataBaseHelper.COURSEHOLE_TABLE,
                new String[] {DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.SUBCOURSEID_COLUMN,
                        DataBaseHelper.COURSEHOLENUMBER_COLUMN,
                        DataBaseHelper.PAR_COLUMN,
                        DataBaseHelper.WOMENPAR_COLUMN,
                        DataBaseHelper.MENHANDICAP_COLUMN,
                        DataBaseHelper.WOMENHANDICAP_COLUMN,
                        DataBaseHelper.BLUEYARD_COLUMN,
                        DataBaseHelper.WHITEYARD_COLUMN,
                        DataBaseHelper.REDYARD_COLUMN},
                WHERE_SUBCOURSEID_EQUALS,
                new String[] {String.valueOf(subCourse.getID())},
                null, null, null, null);

        List<CourseHole> courseHoles = new ArrayList<CourseHole>();
        while (cursor.moveToNext()) {
            CourseHole courseHole = new CourseHole();
            courseHole.setID(cursor.getLong(0));
            courseHole.setSubCourseID(subCourse);
            courseHole.setHoleNumber(cursor.getInt(2));
            courseHole.setPar(cursor.getInt(3));
            courseHole.setWPar(cursor.getInt(4));
            courseHole.setMenHandicap(cursor.getInt(5));
            courseHole.setWomenHandicap(cursor.getInt(6));
            courseHole.setBlueYardage(cursor.getInt(7));
            courseHole.setWhiteYardage(cursor.getInt(8));
            courseHole.setRedYardage(cursor.getInt(9));

            courseHoles.add(courseHole);
        }
        cursor.close();

        return courseHoles;
    }

    /**
     *
     * @param courseHoleID
     * @return
     */
    public CourseHole readCourseHoleFromID(long courseHoleID){

        Cursor cursor = database.query(DataBaseHelper.COURSEHOLE_TABLE,
                new String[] {DataBaseHelper.COURSEHOLEID_COLUMN,
                        DataBaseHelper.SUBCOURSEID_COLUMN,
                        DataBaseHelper.COURSEHOLENUMBER_COLUMN,
                        DataBaseHelper.PAR_COLUMN,
                        DataBaseHelper.WOMENPAR_COLUMN,
                        DataBaseHelper.MENHANDICAP_COLUMN,
                        DataBaseHelper.WOMENHANDICAP_COLUMN,
                        DataBaseHelper.BLUEYARD_COLUMN,
                        DataBaseHelper.WHITEYARD_COLUMN,
                        DataBaseHelper.REDYARD_COLUMN},
                WHERE_COURSEHOLEID_EQUALS,
                new String[] {Long.toString(courseHoleID)},
                null,null,null);

        CourseHole courseHole = new CourseHole();
        while (cursor.moveToNext()) {
            courseHole.setID(cursor.getLong(0));
            courseHole.setSubCourseIDFromID(cursor.getInt(1));
            courseHole.setHoleNumber(cursor.getInt(2));
            courseHole.setPar(cursor.getInt(3));
            courseHole.setWPar(cursor.getInt(4));
            courseHole.setMenHandicap(cursor.getInt(5));
            courseHole.setWomenHandicap(cursor.getInt(6));
            courseHole.setBlueYardage(cursor.getInt(7));
            courseHole.setWhiteYardage(cursor.getInt(8));
            courseHole.setRedYardage(cursor.getInt(9));
        }
        cursor.close();

        return courseHole;
    }

}
