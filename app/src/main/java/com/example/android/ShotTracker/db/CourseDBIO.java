package com.example.android.ShotTracker.db;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Configuration;

import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;
import com.example.android.ShotTracker.objects.SubCourse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 1/14/15.
 * Deal with reading/writing course information from/to file formats
 */
public class CourseDBIO extends Activity {

    /**
     * Read all course information from a file, which is written under a certain format
     *
     * @param fileName
     * @return Course object
     */
    public Course fillCourseFromFile(String fileName) {

        //Opens the course info file
        AssetManager assetManager = getAssets();
        InputStream filereader = null;
        try {
            filereader = assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStreamReader inputreader = new InputStreamReader(filereader);
        BufferedReader bufferedreader = new BufferedReader(inputreader);

        //create a dummy structure
        // Assume:
        //  2 9 hole courses (front 9, back 9)
        //  4 CourseHoleInfo's per CourseHole
        //    green front, green middle, green back, tee
        Course course = new Course();

        for (int sc = 0; sc < 2; sc++) {
            SubCourse subCourse = new SubCourse();
            if (sc == 0)
                subCourse.setName("Front 9");
            else
                subCourse.setName("Back 9");

            //add course holes
            for (int ihole = 0; ihole < 9; ihole++) {
                CourseHole courseHole = new CourseHole();
                courseHole.setHoleNumber(9 * sc + ihole + 1);

                // add course hole information
                CourseHoleInfo c1 = new CourseHoleInfo();
                c1.setInfo("Green Front");
                courseHole.addCourseHoleInfo(c1);

                CourseHoleInfo c2 = new CourseHoleInfo();
                c2.setInfo("Green Middle");
                courseHole.addCourseHoleInfo(c2);

                CourseHoleInfo c3 = new CourseHoleInfo();
                c3.setInfo("Green Back");
                courseHole.addCourseHoleInfo(c3);

                CourseHoleInfo c4 = new CourseHoleInfo();
                c4.setInfo("Tee");
                courseHole.addCourseHoleInfo(c4);

                // add to the list
                subCourse.addCourseHole(courseHole);
            }

            course.addSubCourse(subCourse);

        }

        try {
            //Saves the official course name
            course.setName(bufferedreader.readLine());

            //par for each hole
            for (int x = 0; x < 19; x++) {
                int par = Integer.parseInt(bufferedreader.readLine());

                int sc = -1;
                if (x < 9) sc = 0; // Front 9
                else sc = 1; // Back 9

                course.getSubCourseList().
                        get(sc).
                        getCourseHoleList().
                        get(x).
                        setPar(par);

            }
            // blue tee yardage
            for (int x = 0; x < 19; x++) {
                int blue = Integer.parseInt(bufferedreader.readLine());

                int sc = -1;
                if (x < 9) sc = 0; // Front 9
                else sc = 1; // Back 9

                course.getSubCourseList().
                        get(sc).
                        getCourseHoleList().
                        get(x).
                        setBlueYardage(blue);
            }
            // white tee yardage
            for (int x = 0; x < 19; x++) {
                int white = Integer.parseInt(bufferedreader.readLine());

                int sc = -1;
                if (x < 9) sc = 0; // Front 9
                else sc = 1; // Back 9

                course.getSubCourseList().
                        get(sc).
                        getCourseHoleList().
                        get(x).
                        setWhiteYardage(white);
            }
            // red tee yardage
            for (int x = 0; x < 19; x++) {
                int red = Integer.parseInt(bufferedreader.readLine());

                int sc = -1;
                if (x < 9) sc = 0; // Front 9
                else sc = 1; // Back 9

                course.getSubCourseList().
                        get(sc).
                        getCourseHoleList().
                        get(x).
                        setRedYardage(red);
            }
            // men's handicap
            for (int x = 0; x < 19; x++) {
                int hc = Integer.parseInt(bufferedreader.readLine());

                int sc = -1;
                if (x < 9) sc = 0; // Front 9
                else sc = 1; // Back 9

                course.getSubCourseList().
                        get(sc).
                        getCourseHoleList().
                        get(x).
                        setMenHandicap(hc);
            }
            // women's handicap
            for (int x = 0; x < 19; x++) {
                int hc = Integer.parseInt(bufferedreader.readLine());

                int sc = -1;
                if (x < 9) sc = 0; // Front 9
                else sc = 1; // Back 9

                course.getSubCourseList().
                        get(sc).
                        getCourseHoleList().
                        get(x).
                        setWomenHandicap(hc);
            }

            //Saves the gps locations of the front, middle, and back of all 18 holes
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 19; z++) {
                        double latlong = Double.parseDouble(bufferedreader.readLine());

                        int sc = -1;
                        if (z < 9) sc = 0; // Front 9
                        else sc = 1; // Back 9

                        if (x == 0) { // Latitude
                            course.getSubCourseList().
                                    get(sc).
                                    getCourseHoleList().
                                    get(z).
                                    getCourseHoleInfoList().
                                    get(y).
                                    setLatitude((float) latlong);
                        } else { // Longitude
                            course.getSubCourseList().
                                    get(sc).
                                    getCourseHoleList().
                                    get(z).
                                    getCourseHoleInfoList().
                                    get(y).
                                    setLongitude((float) latlong);
                        }
                    }
                }
            }

            //Saves the gps locations of the tees
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 19; y++) {
                    double latlong = Double.parseDouble(bufferedreader.readLine());

                    int sc = -1;
                    if (y < 9) sc = 0; // Front 9
                    else sc = 1; // Back 9

                    if (x == 0) { // Latitude
                        course.getSubCourseList().
                                get(sc).
                                getCourseHoleList().
                                get(y).
                                getCourseHoleInfoList().
                                get(3).
                                setLatitude((float) latlong);
                    } else { // Longitude
                        course.getSubCourseList().
                                get(sc).
                                getCourseHoleList().
                                get(y).
                                getCourseHoleInfoList().
                                get(3).
                                setLongitude((float) latlong);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return course;
    }

    /**
     * Add course to the DB with all it's dependent SubCourse, CourseHole, and CourseHoleInfo
     * objects
     *
     * @param course
     * @return course ID in DB
     */
    public long createFullCourse(Course course) {

        //update to the DB in stages
        CourseDAO cdao = new CourseDAO(getApplicationContext());
        SubCourseDAO scdao = new SubCourseDAO(getApplicationContext());
        CourseHoleDAO chdao = new CourseHoleDAO(getApplicationContext());
        CourseHoleInfoDAO chidao = new CourseHoleInfoDAO(getApplicationContext());

        long courseID = cdao.createCourse(course);
        course.setID((int) courseID);
        //now set all the courseID's for the subcourses
        for (SubCourse subCourse : course.getSubCourseList()) {
            subCourse.setCourseID(course);
            long scID = scdao.createSubCourse(subCourse);
            subCourse.setID((int) scID);

            //now all the holes in the subcourse
            for (CourseHole courseHole : subCourse.getCourseHoleList()) {
                courseHole.setSubCourseID(subCourse);
                long chID = chdao.createCourseHole(courseHole);
                courseHole.setID((int) chID);

                //now all the course hole infos
                for (CourseHoleInfo courseHoleInfo : courseHole.getCourseHoleInfoList()) {
                    courseHoleInfo.setCourseHoleID(courseHole);
                    chidao.createCourseHoleInfo(courseHoleInfo);
                }
            }
        }

        return courseID;
    }

}
