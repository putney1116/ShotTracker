package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.CourseHoleDAO;
import com.example.android.ShotTracker.db.CourseHoleInfoDAO;
import com.example.android.ShotTracker.db.DataBaseHelper;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.db.ClubDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.objects.Club;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.SubCourse;

/**
 * Activity for testing the database
 */
public class TestDatabase extends Activity {
    private AlertDialog.Builder builder;

    private static final String TAG = "TestDatabase";

    /** Called when the activity is first created */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testdatabase);
//        setContentView(R.layout.homescreen);

        //db = new DatabaseHelper(getApplicationContext());

        Log.d(TAG, "Begin testing DB------");

        // load stuff into the DB
        loadPlayers();
        loadClubs();
        loadCourses();
        // D. McGlinchey - Something about SubCourses is broken ...
        loadSubCourses();

        // make DAO objects
        PlayerDAO pd = new PlayerDAO(getApplicationContext());
        ClubDAO cd = new ClubDAO(getApplicationContext());
        CourseDAO courseDAO = new CourseDAO(getApplicationContext());
        SubCourseDAO subcourseDAO = new SubCourseDAO(getApplicationContext());
        CourseHoleDAO courseholeDAO = new CourseHoleDAO(getApplicationContext());
        CourseHoleInfoDAO courseHoleInfoDAO = new CourseHoleInfoDAO(getApplicationContext());

        // Get all the players
        List<Player> players = pd.readListofPlayers();

        Log.e(TAG, "Number of Players = " + players.size());

        for (Player player : players){
            Log.d(TAG, player.getName());
        }

        // Get all the clubs
        List<Club> clubs = cd.readListofClubs();

        Log.e(TAG, "Number of Clubs = " + clubs.size());

        for (Club club : clubs) {
            Log.d(TAG, club.getClub());
        }

        // Add the first 3 clubs to the first players bag
        Player p0 = players.get(0);
        Log.d(TAG, "Adding clubs to " + p0.getID() + ": " + p0.getName() + "'s bag...");

        for (int i = 0; i < 3; i++) {
            cd.createClubToBag(p0, clubs.get(i));
            Log.d(TAG, "Added " + clubs.get(i).getClub());
        }

        List<Club> clubsInBag = cd.readClubsInBag(p0);
        Log.d(TAG, "Number of clubs in bag = " + clubsInBag.size());

        for (Club club : clubsInBag) {
            Log.d(TAG, club.getClub() + " in " + p0.getName() + "'s bag");
        }

        // Get all the courses
        List<Course> courses = courseDAO.readListofCourses();

        Log.d(TAG, "Number of Courses = " + courses.size());

        for (Course course : courses) {
            Log.d(TAG, course.getID() + ": " + course.getName() + " - " + course.getLocation());

            // get subcourses
            List<SubCourse> subCourses = subcourseDAO.readListofSubCourses(course);
            Log.d(TAG, "  Number of SubCourses = " + subCourses.size());

            for (SubCourse subCourse : subCourses) {
                Log.d(TAG, "   " + subCourse.getID() + ": "
                + subCourse.getName() + " - Rating = " + subCourse.getRating());
            }
        }


        Log.d(TAG, "Finish------");
    }


    /**
     * For testing, add a set of default players.
     */
    public void loadPlayers() {
        List<Player> players = new ArrayList<Player>();
        players.add(new Player("Eric Putney"));
        players.add(new Player("Erik Jensen"));
        players.add(new Player("Darren"));
        players.add(new Player("Justin"));

        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        for (Player player : players) playerDAO.create(player);
    }

    /**
     * For testing, add a set of clubs to the DB
     */
    public void loadClubs() {
        List<Club> clubs = new ArrayList<Club>();
        clubs.add(new Club("Driver"));
        clubs.add(new Club("3 Wood"));
        clubs.add(new Club("5 Wood"));
        clubs.add(new Club("3 Iron"));
        clubs.add(new Club("4 Iron"));
        clubs.add(new Club("5 Iron"));
        clubs.add(new Club("6 Iron"));
        clubs.add(new Club("7 Iron"));
        clubs.add(new Club("8 Iron"));
        clubs.add(new Club("9 Iron"));
        clubs.add(new Club("PW"));
        clubs.add(new Club("SW"));

        ClubDAO clubDAO = new ClubDAO(getApplicationContext());
        for (Club club : clubs) clubDAO.create(club);

    }

    /**
     * For testing, add a set of Courses and SubCourses to the DB
     */
    public void loadCourses() {
        List<Course> courses = new ArrayList<Course>();
        courses.add(new Course("Pine Valley Golf Club", "Clementon, New Jersey"));
        courses.add(new Course("Cypress Point Gold Club", "Pebble Beach, California"));
        courses.add(new Course("Pebble Beach Golf Links", "Pebble Beach, California"));

        CourseDAO courseDAO = new CourseDAO(getApplicationContext());
        for (Course course : courses) {
            courseDAO.createCourse(course);
        }
    }

    public void loadSubCourses() {
        CourseDAO courseDAO = new CourseDAO(getApplicationContext());
        SubCourseDAO subCourseDAO = new SubCourseDAO(getApplicationContext());

        List<Course> courses = courseDAO.readListofCourses();

        for (Course course : courses) {
            List<SubCourse> subCourses = new ArrayList<SubCourse>();
            subCourses.add(new SubCourse(course,"Front 9",course.getID()+2));
            subCourses.add(new SubCourse(course,"Back 9",course.getID()+3));

            for (SubCourse subCourse : subCourses) {
                subCourseDAO.createSubCourse(subCourse);
            }
        }

    }
}
