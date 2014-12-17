package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;

import com.example.android.ShotTracker.objects.Course;

/**
 * Created by damcglinchey on 12/16/14.
 */
public class CourseDAO extends ShotTrackerDBDAO {

    private static final String WHERE_COURSEID_EQUALS = DataBaseHelper.COURSEID_COLUMN
            + "=?";
    private static final String WHERE_SUBCOURSEID_EQUALS = DataBaseHelper.SUBCOURSEID_COLUMN
            + "=?";
    private static final String WHERE_COURSHOLEID_EQUALS = DataBaseHelper.COURSEHOLEID_COLUMN
            + "=?";
    private static final String WHERE_COURSEHOLEINFOID_EQUALS =
            DataBaseHelper.COURSEHOLEINFOID_COLUMN + "=?";


    public CourseDAO(Context context) {
        super(context);
    }

    /**
     * Add a course to the DB
     * @param course
     * @return
     */
    public long createCourse(Course course) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.COURSENAME_COLUMN, course.getName());

        if (course.getLocation() != "") {
            values.put(DataBaseHelper.COURSELOCATION_COLUMN, course.getLocation());
        }

        return database.insert(DataBaseHelper.COURSE_TABLE, null, values);
    }

    /**
     * Update course information
     * @param course Course to be updated. ID must be set.
     * @return
     */
    public long updateCourse(Course course) {
        ContentValues values = new ContentValues();

        // make sure that we have an id to update
        if (course.getID() < 0) {
            throw new RuntimeException("Course ID not set in CourseDAO.updateCourse()");
        }

        values.put(DataBaseHelper.COURSENAME_COLUMN, course.getName());

        if (course.getLocation() != "") {
            values.put(DataBaseHelper.COURSELOCATION_COLUMN, course.getLocation());
        }

        return database.update(DataBaseHelper.COURSE_TABLE,
                values,
                WHERE_COURSEID_EQUALS,
                new String[] {String.valueOf(course.getID())});
    }

    /**
     * Delete a course from the DB
     * @param course Course to be deleted.
     * @return
     */
    public long deleteCourse(Course course) {
        // check that course ID is set, otherwise can't delete
        if (course.getID() < 0) {
            throw new RuntimeException("Course ID not set in CourseDAO.deleteCourse()");
        }

        return database.delete(DataBaseHelper.COURSE_TABLE,
                WHERE_COURSEID_EQUALS,
                new String[] {String.valueOf(course.getID())});
    }



}
