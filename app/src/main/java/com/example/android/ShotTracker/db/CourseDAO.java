package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 12/16/14.
 */
public class CourseDAO extends ShotTrackerDBDAO {

    private static final String WHERE_COURSEID_EQUALS = DataBaseHelper.COURSEID_COLUMN
            + "=?";


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
     *
     * @param courseID
     * @return
     */
    public Course readCourseFromID(long courseID){

        Cursor cursor = database.query(DataBaseHelper.COURSE_TABLE,
                new String[] {DataBaseHelper.COURSEID_COLUMN,
                              DataBaseHelper.COURSENAME_COLUMN,
                              DataBaseHelper.COURSELOCATION_COLUMN},
                WHERE_COURSEID_EQUALS,
                new String[] {Long.toString(courseID)},
                null,null,null);

        Course course = new Course();
        while (cursor.moveToNext()) {
            course.setID(cursor.getLong(0));
            course.setName(cursor.getString(1));
            course.setLocation(cursor.getString(2));
        }
        cursor.close();

        return course;
    }

    /**
     * Get a course name given an course ID
     * @param courseID
     * @return
     */
    public String readCourseNameFromID(long courseID){
        String courseName = null;

        Cursor cursor = database.query(DataBaseHelper.COURSE_TABLE,
                new String[] {DataBaseHelper.COURSENAME_COLUMN,},
                WHERE_COURSEID_EQUALS,
                new String[] {Long.toString(courseID)},
                null,null,null);

        while (cursor.moveToNext()) {
            courseName = cursor.getString(0);
        }
        cursor.close();

        return courseName;
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
            course.setID(cursor.getLong(0));
            course.setName(cursor.getString(1));
            course.setLocation(cursor.getString(2));

            courses.add(course);
        }
        cursor.close();

        return courses;
    }

}
