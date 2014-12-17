package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.SubCourse;

import java.util.ArrayList;
import java.util.List;

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
                new String[]{String.valueOf(course.getID())});
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
                new String[]{String.valueOf(course.getID())});
    }

    /**
     * Get a list of all Course's in the DB
     * @return A list of Course objects
     */
    public List<Course> readListofCourses() {
        List<Course> courses = new ArrayList<Course>();

        //\todo check nulls

        Cursor cursor = database.query(DataBaseHelper.COURSE_TABLE,
                new String[] {DataBaseHelper.COURSEID_COLUMN,
                              DataBaseHelper.COURSENAME_COLUMN,
                              DataBaseHelper.COURSELOCATION_COLUMN},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            Course course = new Course();
            course.setID(cursor.getInt(0));
            course.setName(cursor.getString(1));
            course.setLocation(cursor.getString(2));

            courses.add(course);
        }

        return courses;
    }

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
                new string[] {String.valueOf(subCourse.getID())});
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

        List<SubCourse> subCourses = new List<SubCourse>();

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
            subCourse.setID(cursor.getInt(0));
            subCourse.setCourseID(course.getID());
            subCourse.setName(cursor.getString(1));
            subCourse.setRating(cursor.getFloat(2));

            subCourses.add(subCourse);
        }

        return subCourses;
    }
}
