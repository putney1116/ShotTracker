package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
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
            subCourse.setID(cursor.getInt(0));
            //ewj - I have it set up to take in the course object directly
            //check out SubCourse.java
            //not sure if I did it "correctly", but it should work
            subCourse.setCourseID(course);
            subCourse.setName(cursor.getString(1));
            subCourse.setRating(cursor.getFloat(2));

            subCourses.add(subCourse);
        }

        return subCourses;
    }

    /**
     * Add a CourseHole to the DB
     * @param courseHole Course Hole to be added
     * @return
     */
    public long createCourseHole(CourseHole courseHole) {
        ContentValues values = new ContentValues();

        //\todo check that CourseHole.getSubCourseID() is set before using it.

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
                WHERE_COURSHOLEID_EQUALS,
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
                WHERE_COURSHOLEID_EQUALS,
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
            courseHole.setID(cursor.getInt(0));
            courseHole.setSubCourseID(subCourse);
            courseHole.setHoleNumber(cursor.getInt(2));
            courseHole.setWPar(cursor.getInt(3));
            courseHole.setMenHandicap(cursor.getInt(4));
            courseHole.setWomenHandicap(cursor.getInt(5));
            courseHole.setBlueYardage(cursor.getInt(6));
            courseHole.setWhiteYardage(cursor.getInt(7));
            courseHole.setRedYardage(cursor.getInt(8));

            courseHoles.add(courseHole);
        }

        return courseHoles;
    }
}
