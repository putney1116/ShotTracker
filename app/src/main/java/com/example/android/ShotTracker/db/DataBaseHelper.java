package com.example.android.ShotTracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Data base helper class to get db object and create/update tables
 * if necessary.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shottracker.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Database Tables
     */

    public static final String COURSE_TABLE = "Course";
    public static final String SUBCOURSE_TABLE = "SubCourse";
    public static final String COURSEHOLE_TABLE = "CourseHole";
    public static final String COURSEHOLEINFO_TABLE = "CourseHoleInfo";

    public static final String ROUND_TABLE = "Round";
    public static final String ROUNDHOLE_TABLE = "RoundHole";
    public static final String SHOT_TABLE = "Shot";
    public static final String SHOTTYPE_TABLE = "ShotType";
    public static final String SHOTLINK_TABLE = "ShotLink";

    public static final String PLAYER_TABLE = "Player";

    public static final String CLUB_TABLE = "Club";

    public static final String BAG_TABLE = "Bag";

    /**
     * Course, SubCourse, CourseHole, CourseHoleInfo Columns
     */
    public static final String COURSEID_COLUMN = "CourseID";
    public static final String COURSENAME_COLUMN = "CourseHame";
    public static final String COURSELOCATION_COLUMN = "CourseLocation";

    public static final String SUBCOURSEID_COLUMN = "SubCourseID";
    public static final String SUBCOURSENAME_COLUMN = "SubCourseName";

    public static final String COURSEHOLEID_COLUMN = "CourseHoleID";
    public static final String COURSEHOLENUMBER_COLUMN = "CourseHoleNumber";
    public static final String PAR_COLUMN = "Par";
    public static final String WOMENPAR_COLUMN = "WomenPar";
    public static final String MENHANDICAP_COLUMN = "MenHandicap";
    public static final String WOMENHANDICAP_COLUMN = "WomenHandicap";
    public static final String BLUEYARD_COLUMN = "BlueTeeYards";
    public static final String WHITEYARD_COLUMN = "WhiteTeeYards";
    public static final String REDYARD_COLUMN = "RedTeeYards";

    public static final String COURSEHOLEINFOID_COLUMN = "CourseHoleInfoID";
    public static final String INFO_COLUMN = "Info";
    public static final String INFOLATITUDE_COLUMN = "InfoLatitude";
    public static final String INFOLONGITUDE_COLUMN = "InfoLongitude";

    /**
     * Round, RoundHole, Shot, ShotType, ShotLink Columns
     */
    public static final String ROUNDID_COLUMN = "RoundID";
    public static final String ROUNDDATE_COLUMN = "RoundDate";

    public static final String ROUNDHOLEID_COLUMN = "RoundHoleID";
    public static final String SCORE_COLUMN = "Score";
    public static final String PUTTS_COLUMN = "Putts";
    public static final String PENALTIES_COLUMN = "Penalties";
    public static final String FAIRWAYS_COLUMN = "Fairways";

    public static final String SHOTID_COLUMN = "ShotID";
    public static final String YARDS_COLUMN = "Yards";
    public static final String SHOTSTARTLAT_COLUMN = "ShotStartLatitude";
    public static final String SHOTSTARTLONG_COLUMN = "ShotStartLongitude";
    public static final String SHOTENDLAT_COLUMN = "ShotEndLatitude";
    public static final String SHOTENDLONG_COLUMN = "ShotEndLongitude";

    public static final String SHOTTYPEID_COLUMN = "ShotTypeID";
    public static final String SHOTTYPE_COLUMN = "ShotType";

    public static final String SHOTLINKID_COLUMN = "ShotLinkID";

    /**
     * Player Columns
     */
    public static final String PLAYERID_COLUMN = "PlayerID";
    public static final String PLAYERNAME_COLUMN = "PlayerName";
    public static final String PLAYERHANDICAP_COLUMN = "PlayerHandicap";

    /**
     * Club, Bag Columns
     */
    public static final String CLUBID_COLUMN = "ClubID";
    public static final String CLUBNAME_COLUMN = "ClubName";

    public static final String BAGID_COLUMN = "BagID";

    /**
     * Table creation statements
     */
    private static final String CREATE_COURSE_TABLE = "CREATE TABLE "
            + COURSE_TABLE + "("
            + COURSEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COURSENAME_COLUMN + " TEXT NOT NULL, "
            + COURSELOCATION_COLUMN + " TEXT"
            + ");";

    private static final String CREATE_SUBCOURSE_TABLE = "CREATE TABLE "
            + SUBCOURSE_TABLE + "("
            + SUBCOURSEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COURSEID_COLUMN + " INTEGER NOT NULL, "
            + SUBCOURSENAME_COLUMN + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_COURSEHOLE_TABLE = "CREATE TABLE "
            + COURSEHOLE_TABLE + "("
            + COURSEHOLEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SUBCOURSEID_COLUMN + " INTEGER NOT NULL, "
            + COURSEHOLENUMBER_COLUMN + " INT NOT NULL, "
            + PAR_COLUMN + " INT NOT NULL, "
            + WOMENPAR_COLUMN + " INT, "
            + MENHANDICAP_COLUMN + " INT, "
            + WOMENHANDICAP_COLUMN + " INT, "
            + BLUEYARD_COLUMN + " NUMERIC, "
            + WHITEYARD_COLUMN + " NUMERIC, "
            + REDYARD_COLUMN + " NUMERIC"
            + ");";

    private static final String CREATE_COURSEHOLEINFO_TABLE = " CREATE TABLE "
            + COURSEHOLEINFO_TABLE + "("
            + COURSEHOLEINFOID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + INFO_COLUMN + " TEXT NOT NULL, "
            + INFOLATITUDE_COLUMN + " NUMERIC NOT NULL, "
            + INFOLONGITUDE_COLUMN + " NUMERIC NOT NULL"
            + ");";

    private static final String CREATE_ROUND_TABLE = "CREATE TABLE "
            + ROUND_TABLE + "("
            + ROUNDID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SUBCOURSEID_COLUMN + " INTEGER NOT NULL, "
            + ROUNDDATE_COLUMN + " DATE"
            +");";

    private static final String CREATE_ROUNDHOLE_TABLE = "CREATE TABLE "
            + ROUNDHOLE_TABLE + "("
            + ROUNDHOLEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ROUNDID_COLUMN + " INTEGER NOT NULL, "
            + PLAYERID_COLUMN + " INTEGER NOT NULL, "
            + SCORE_COLUMN + " INT NOT NULL, "
            + PUTTS_COLUMN + " INT, "
            + PENALTIES_COLUMN + " INT, "
            + FAIRWAYS_COLUMN + " INT"
            + ");";

    private static final String CREATE_SHOT_TABLE = "CREATE TABLE "
            + SHOT_TABLE + "("
            + SHOTID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ROUNDHOLEID_COLUMN + " INTEGER NOT NULL, "
            + CLUBID_COLUMN + " INTEGER, "
            + YARDS_COLUMN + " NUMERIC, "
            + SHOTSTARTLAT_COLUMN + " NUMERIC, "
            + SHOTSTARTLONG_COLUMN + " NUMERIC, "
            + SHOTENDLAT_COLUMN + " NUMERIC, "
            + SHOTENDLONG_COLUMN + " NUMERIC"
            + ");";

    private static final String CREATE_SHOTTYPE_TABLE = "CREATE TABLE "
            + SHOTTYPE_TABLE + "("
            + SHOTTYPEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SHOTTYPE_COLUMN + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_SHOTLINK_TABLE = "CREATE TABLE "
            + SHOTLINK_TABLE + "("
            + SHOTLINKID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SHOTID_COLUMN + " INTEGER NOT NULL, "
            + SHOTTYPEID_COLUMN + " INTEGER NOT NULL"
            + ");";

    private static final String CREATE_PLAYER_TABLE = "CREATE TABLE "
            + PLAYER_TABLE + "("
            + PLAYERID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PLAYERNAME_COLUMN + " TEXT NOT NULL, "
            + PLAYERHANDICAP_COLUMN + " INT"
            +");";

    private static final String CREATE_CLUB_TABLE = "CREATE TABLE "
            + CLUB_TABLE + "("
            + CLUBID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CLUBNAME_COLUMN + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_BAG_TABLE = "CREATE TABLE "
            + BAG_TABLE + "("
            + BAGID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PLAYERID_COLUMN + " INTEGER NOT NULL, "
            + CLUBID_COLUMN + " INTEGER NOT NULL"
            + ");";


    /**
     * D. McGlinchey 11/28/2014 - Still need to understand what the below functions are doing
     *  and how they should be setup for our case. currently following the tutorial at
     *  http://androidopentutorials.com/android-sqlite-join-multiple-tables-example/
     */
    private static DataBaseHelper instance;

    public static synchronized DataBaseHelper getHelper (Context context) {
        if (instance == null)
            instance = new DataBaseHelper(context);
        return instance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * \todo D. McGlinchey 11/28/2014: Need to understand onOpen() function
     *
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints -- what??
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * Create all the database tables and input any default information
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COURSE_TABLE);
        db.execSQL(CREATE_SUBCOURSE_TABLE);
        db.execSQL(CREATE_COURSEHOLE_TABLE);
        db.execSQL(CREATE_COURSEHOLEINFO_TABLE);
        db.execSQL(CREATE_ROUND_TABLE);
        db.execSQL(CREATE_ROUNDHOLE_TABLE);
        db.execSQL(CREATE_SHOT_TABLE);
        db.execSQL(CREATE_SHOTTYPE_TABLE);
        db.execSQL(CREATE_SHOTLINK_TABLE);
        db.execSQL(CREATE_PLAYER_TABLE);
        db.execSQL(CREATE_CLUB_TABLE);
        db.execSQL(CREATE_BAG_TABLE);
    }

    /**
     * Perform necessary actions for upgrading the database from oldVersion to newVersion
     *
     * @param db
     * @param oldVersion Old db version
     * @param newVersion New db version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //for now just drop the old tables and re-create them?
        db.execSQL("DROP TABLE IF EXISTS" + COURSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SUBCOURSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + COURSEHOLE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + COURSEHOLEINFO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + ROUND_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + ROUNDHOLE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SHOT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SHOTTYPE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SHOTLINK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + PLAYER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + CLUB_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + BAG_TABLE);

        onCreate(db);
    }




}
