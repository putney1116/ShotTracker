package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.SubCourse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 1/7/15.
 */
public class SubCourseDAO extends ShotTrackerDBDAO {

    private static final String WHERE_SUBCOURSEID_EQUALS = DataBaseHelper.SUBCOURSEID_COLUMN
            + "=?";
    private static final String WHERE_COURSEID_EQUALS = DataBaseHelper.COURSEID_COLUMN
            + "=?";

    public SubCourseDAO(Context context){ super(context); }

    /**
     * Add a subcourse to the DB
     * @param subCourse SubCourse to be added. SubCourse.getCourseID() must be set
     * @return
     */
    public long createSubCourse(SubCourse subCourse) {
        ContentValues values = new ContentValues();

        //\todo check that subCourse.getCourseID() is set before using it.

        values.put(DataBaseHelper.COURSEID_COLUMN, subCourse.getCourseID());
        values.put(DataBaseHelper.SUBCOURSENAME_COLUMN, subCourse.getName());

        if (subCourse.getRating() > 0) {
            values.put(DataBaseHelper.SUBCOURSERATING_COLUMN, subCourse.getRating());
        }

        return database.insert(DataBaseHelper.SUBCOURSE_TABLE, null, values);
    }

    /**
     * Update SubCourse information in DB
     * @param subCourse SubCourse to be updated. SubCourse.getID() must be set.
     * @return
     */
    public long updateSubCourse(SubCourse subCourse) {
        ContentValues values = new ContentValues();

        if (subCourse.getID() < 0) {
            throw new RuntimeException("SubCourse ID not set in CourseDAO.updateSubCourse()");
        }

        values.put(DataBaseHelper.COURSEID_COLUMN, subCourse.getCourseID());
        values.put(DataBaseHelper.SUBCOURSENAME_COLUMN, subCourse.getName());

        if (subCourse.getRating() > 0) {
            values.put(DataBaseHelper.SUBCOURSERATING_COLUMN, subCourse.getRating());
        }

        return database.update(DataBaseHelper.SUBCOURSE_TABLE,
                values,
                WHERE_SUBCOURSEID_EQUALS,
                new String[] {String.valueOf(subCourse.getID())});
    }

    /**
     * Delete a SubCourse from the DB
     * @param subCourse SubCourse to be deleted.
     * @return
     */
    public long deleteSubCourse(SubCourse subCourse) {
        if (subCourse.getID() < 0) {
            throw new RuntimeException("SubCourse ID not set in CourseDAO.deleteSubCourse");
        }

        return database.delete(DataBaseHelper.SUBCOURSE_TABLE,
                WHERE_SUBCOURSEID_EQUALS,
                new String[] {String.valueOf(subCourse.getID())});
    }

    /**
     * Get a list of SubCourses for a given Course
     * @param course Course desired
     * @return List of SubCourse objects
     */
    public List<SubCourse> readListofSubCourses(Course course) {
        if (course.getID() < 0) {
            throw new RuntimeException("Course ID not set in CourseDAO.readListofSubCourses()");
        }

        List<SubCourse> subCourses = new ArrayList<SubCourse>();

        //\todo check for nulls

        Cursor cursor = database.query(DataBaseHelper.SUBCOURSE_TABLE,
                new String[] {DataBaseHelper.SUBCOURSEID_COLUMN,
                        DataBaseHelper.SUBCOURSENAME_COLUMN,
                        DataBaseHelper.SUBCOURSERATING_COLUMN},
                WHERE_COURSEID_EQUALS,
                new String[] {String.valueOf(course.getID())},
                null, null, null);

        while (cursor.moveToNext()) {
            SubCourse subCourse = new SubCourse();
            subCourse.setID(cursor.getLong(0));
            //ewj - I have it set up to take in the course object directly
            //check out SubCourse.java
            //not sure if I did it "correctly", but it should work
            subCourse.setCourseID(course);
            subCourse.setName(cursor.getString(1));
            subCourse.setRating(cursor.getDouble(2));

            subCourses.add(subCourse);
        }
         cursor.close();

        return subCourses;
    }


}
