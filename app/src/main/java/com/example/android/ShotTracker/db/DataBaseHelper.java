package com.example.android.ShotTracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Data base helper class to get db object and create/update tables
 * if necessary.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static String DATABASE_PATH = "/data/data/com.example.android.ShotTracker/databases/";
    private static String DATABASE_NAME = "shottracker.db";
    private static int DATABASE_VERSION = 1;
    private static String OLD_DATABASE_PATH = DATABASE_PATH + "old_" + DATABASE_NAME;

    private final Context myContext;

    private boolean createDatabase = false;
    private boolean upgradeDatabase = false;

    /**
     * Database Tables
     */

    public static final String COURSE_TABLE = "Course";
    public static final String SUBCOURSE_TABLE = "SubCourse";
    public static final String COURSEHOLE_TABLE = "CourseHole";
    public static final String COURSEHOLEINFO_TABLE = "CourseHoleInfo";

    public static final String ROUND_TABLE = "Round";
    public static final String SUBROUND_TABLE = "SubRound";
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
    public static final String COURSENAME_COLUMN = "CourseName";
    public static final String COURSELOCATION_COLUMN = "CourseLocation";

    public static final String SUBCOURSEID_COLUMN = "SubCourseID";
    public static final String SUBCOURSENAME_COLUMN = "SubCourseName";
    public static final String SUBCOURSERATING_COLUMN = "SubCourseRating";

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
     * Subround, RoundHole, Shot, ShotType, ShotLink Columns
     */
    public static final String ROUNDID_COLUMN = "RoundID";
    public static final String ROUNDDATE_COLUMN = "RoundDate";

    public static final String SUBROUNDID_COLUMN = "SubRoundID";

    public static final String ROUNDHOLEID_COLUMN = "RoundHoleID";
    public static final String SCORE_COLUMN = "Score";
    public static final String PUTTS_COLUMN = "Putts";
    public static final String PENALTIES_COLUMN = "Penalties";
    public static final String FAIRWAYS_COLUMN = "Fairways";
    public static final String CHIPS_COLUMN = "Chips";
    public static final String GIR_COLUMN = "GiR";

    public static final String SHOTID_COLUMN = "ShotID";
    public static final String YARDS_COLUMN = "Yards";
    public static final String SHOTSTARTLAT_COLUMN = "ShotStartLatitude";
    public static final String SHOTSTARTLONG_COLUMN = "ShotStartLongitude";
    public static final String SHOTENDLAT_COLUMN = "ShotEndLatitude";
    public static final String SHOTENDLONG_COLUMN = "ShotEndLongitude";

    public static final String SHOTTYPEID_COLUMN = "ShotTypeID";
    public static final String SHOTTYPE_COLUMN = "ShotType";
    public static final String SHOTISPRE_COLIMN = "ShotTypeIsPre";

    public static final String SHOTLINKID_COLUMN = "ShotLinkID";

    /**
     * Player Columns
     */
    public static final String PLAYERID_COLUMN = "PlayerID";
    public static final String PLAYERNAME_COLUMN = "PlayerName";
    public static final String USRDEF_COLUMN = "UserDefault";
    public static final String PLAYERHANDICAP_COLUMN = "PlayerHandicap";
    public static final String PLAYERNUMBER_COLUMN = "PlayerNumber";

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
            + SUBCOURSENAME_COLUMN + " TEXT NOT NULL, "
            + SUBCOURSERATING_COLUMN + " DOUBLE PRECISION"
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
            + BLUEYARD_COLUMN + " INT, "
            + WHITEYARD_COLUMN + " INT, "
            + REDYARD_COLUMN + " INT"
            + ");";

    private static final String CREATE_COURSEHOLEINFO_TABLE = " CREATE TABLE "
            + COURSEHOLEINFO_TABLE + "("
            + COURSEHOLEINFOID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COURSEHOLEID_COLUMN + " INTEGER NOT NULL, "
            + INFO_COLUMN + " TEXT NOT NULL, "
            + INFOLATITUDE_COLUMN + " DOUBLE PRECISION NOT NULL, "
            + INFOLONGITUDE_COLUMN + " DOUBLE PRECISION NOT NULL"
            + ");";

    private static final String CREATE_ROUND_TABLE = "CREATE TABLE "
            + ROUND_TABLE + "("
            + ROUNDID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ROUNDDATE_COLUMN + " DOUBLE PRECISION"
            + ");";

    private static final String CREATE_SUBROUND_TABLE = "CREATE TABLE "
            + SUBROUND_TABLE + "("
            + SUBROUNDID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SUBCOURSEID_COLUMN + " INTEGER NOT NULL, "
            + ROUNDID_COLUMN + " INTEGER NOT NULL"
            + ");";

    private static final String CREATE_ROUNDHOLE_TABLE = "CREATE TABLE "
            + ROUNDHOLE_TABLE + "("
            + ROUNDHOLEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SUBROUNDID_COLUMN + " INTEGER NOT NULL, "
            + PLAYERID_COLUMN + " INTEGER NOT NULL, "
            + PLAYERNUMBER_COLUMN + " INTEGER NOT NULL, "
            + COURSEHOLEID_COLUMN + " INTEGER NOT NULL, "
            + SCORE_COLUMN + " INT NOT NULL, "
            + PUTTS_COLUMN + " INT, "
            + PENALTIES_COLUMN + " INT, "
            + FAIRWAYS_COLUMN + " INT DEFAULT 0, "
            + CHIPS_COLUMN + " INT, "
            + GIR_COLUMN + " BOOLEAN NOT NULL DEFAULT 0"
            + ");";

    private static final String CREATE_SHOT_TABLE = "CREATE TABLE "
            + SHOT_TABLE + "("
            + SHOTID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ROUNDHOLEID_COLUMN + " INTEGER NOT NULL, "
            + CLUBID_COLUMN + " INTEGER, "
            + YARDS_COLUMN + " INTEGER, "
            + SHOTSTARTLAT_COLUMN + " DOUBLE PRECISION, "
            + SHOTSTARTLONG_COLUMN + " DOUBLE PRECISION, "
            + SHOTENDLAT_COLUMN + " DOUBLE PRECISION, "
            + SHOTENDLONG_COLUMN + " DOUBLE PRECISION"
            + ");";

    private static final String CREATE_SHOTTYPE_TABLE = "CREATE TABLE "
            + SHOTTYPE_TABLE + "("
            + SHOTTYPEID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SHOTTYPE_COLUMN + " TEXT NOT NULL, "
            + SHOTISPRE_COLIMN + " BOOLEAN NOT NULL DEFAULT 0"
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
            + PLAYERNAME_COLUMN + " TEXT NOT NULL UNIQUE, "
            + USRDEF_COLUMN + " BOOLEAN NOT NULL DEFAULT 0, "
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

    private static DataBaseHelper instance;

    public static synchronized DataBaseHelper getHelper (Context context) {
        if (instance == null)
            instance = new DataBaseHelper(context);
        return instance;
    }

    /**
     * Constructor: sets private myContext to the context of the current activity,
     * and updates DataBase_Path with the absolute path to the DB (should be the same
     * as hardcoded above?)
     * @param context
     */
    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext = context;
        DATABASE_PATH = myContext.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    /**
     * Upgrade the database in internal storage if it exists but is not current.
     * Create a new empty database in internal storage if it does not exist.
     */
    public void initializeDataBase() {
        /*
         * Creates or updates the database in internal storage if it is needed
         * before opening the database. In all cases opening the database copies
         * the database in internal storage to the cache.
         */
        getWritableDatabase();

        if (createDatabase) {
            /*
             * If the database is created by the copy method, then the creation
             * code needs to go here. This method consists of copying the new
             * database from assets into internal storage and then caching it.
             */
            try {
                /*
                 * Write over the empty data that was created in internal
                 * storage with the one in assets and then cache it.
                 */
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        } else if (upgradeDatabase) {
            /*
             * If the database is upgraded by the copy and reload method, then
             * the upgrade code needs to go here. This method consists of
             * renaming the old database in internal storage, create an empty
             * new database in internal storage, copying the database from
             * assets to the new database in internal storage, caching the new
             * database from internal storage, loading the data from the old
             * database into the new database in the cache and then deleting the
             * old database from internal storage.
             */
            try {
                FileHelper.copyFile(DATABASE_PATH, OLD_DATABASE_PATH);
                copyDataBase();
                SQLiteDatabase old_db = SQLiteDatabase.openDatabase(OLD_DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                SQLiteDatabase new_db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                /*
                 * Add code to load data into the new database from the old
                 * database and then delete the old database from internal
                 * storage after all data has been transferred.
                 */
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException {
        /*
         * Close SQLiteOpenHelper so it will commit the created empty database
         * to internal storage.
         */
        close();

        /*
         * Open the database in the assets folder as the input stream.
         */
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

        /*
         * Open the empty db in interal storage as the output stream.
         */
        OutputStream myOutput = new FileOutputStream(DATABASE_PATH);

        /*
         * Copy over the empty db in internal storage with the database in the
         * assets folder.
         */
        FileHelper.copyFile(myInput, myOutput);

        /*
         * Access the copied database so SQLiteHelper will cache it and mark it
         * as created.
         */
        getWritableDatabase().close();
    }

    /*
     * This is where the creation of tables and the initial population of the
     * tables should happen, if a database is being created from scratch instead
     * of being copied from the application package assets. Copying a database
     * from the application package assets to internal storage inside this
     * method will result in a corrupted database.
     * <P>
     * NOTE: This method is normally only called when a database has not already
     * been created. When the database has been copied, then this method is
     * called the first time a reference to the database is retrieved after the
     * database is copied since the database last cached by SQLiteOpenHelper is
     * different than the database in internal storage.
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * Signal that a new database needs to be copied. The copy process must
         * be performed after the database in the cache has been closed causing
         * it to be committed to internal storage. Otherwise the database in
         * internal storage will not have the same creation timestamp as the one
         * in the cache causing the database in internal storage to be marked as
         * corrupted.
         */
        createDatabase = true;

        /*
         * This will create by reading a sql file and executing the commands in
         * it.
         */
        // try {
        // InputStream is = myContext.getResources().getAssets().open(
        // "create_database.sql");
        //
        // String[] statements = FileHelper.parseSqlFile(is);
        //
        // for (String statement : statements) {
        // db.execSQL(statement);
        // }
        // } catch (Exception ex) {
        // ex.printStackTrace();
        // }
    }

    /**
     * Called only if version number was changed and the database has already
     * been created. Copying a database from the application package assets to
     * the internal data system inside this method will result in a corrupted
     * database in the internal data system.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * Signal that the database needs to be upgraded for the copy method of
         * creation. The copy process must be performed after the database has
         * been opened or the database will be corrupted.
         */
        upgradeDatabase = true;

        /*
         * Code to update the database via execution of sql statements goes
         * here.
         */

        /*
         * This will upgrade by reading a sql file and executing the commands in
         * it.
         */
        // try {
        // InputStream is = myContext.getResources().getAssets().open(
        // "upgrade_database.sql");
        //
        // String[] statements = FileHelper.parseSqlFile(is);
        //
        // for (String statement : statements) {
        // db.execSQL(statement);
        // }
        // } catch (Exception ex) {
        // ex.printStackTrace();
        // }
    }

    /**
     * Called everytime the database is opened by getReadableDatabase or
     * getWritableDatabase. This is called after onCreate or onUpgrade is
     * called.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    /*
     * Add your public helper methods to access and get content from the
     * database. You could return cursors by doing
     * "return myDataBase.query(....)" so it'd be easy to you to create adapters
     * for your views.
     */


    /**********************


    /**
     * D. McGlinchey 11/28/2014 - Still need to understand what the below functions are doing
     *  and how they should be setup for our case. currently following the tutorial at
     *  http://androidopentutorials.com/android-sqlite-join-multiple-tables-example/
     *//*
    private static DataBaseHelper instance;

    private final Context myContext;

    private SQLiteDatabase myDatabase;

    public static synchronized DataBaseHelper getHelper (Context context) {
        if (instance == null)
            instance = new DataBaseHelper(context);
        return instance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    public void createDatabase() throws IOException{

        boolean dbExist = checkDatabase();

        if (dbExist){
            //Database already exists
        } else {

            this.getReadableDatabase();

            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error ("Error copying database");
            }
        }
    }

    private boolean checkDatabase(){

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DATABASE_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e) {
            //database doesn't exist yet
            Log.e("DB","DB doesn't exist");
        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    private void copyDatabase() throws IOException{

        //Opens local db as input stream
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

        //Path to the created empty database
        String outFileName = DATABASE_PATH + DATABASE_NAME;

        //Open the empty database as output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer the bytes
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDatabase() throws SQLiteException{

        String myPath = DATABASE_PATH + DATABASE_NAME;
        myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close(){
        if (myDatabase != null) {
            myDatabase.close();
        }
        super.close();
    }

    *//**
     * \todo D. McGlinchey 11/28/2014: Need to understand onOpen() function
     *
     * @param db
     *//*
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints -- what??
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    *//**
     * Create all the database tables and input any default information
     *
     * @param db
     *//*
    @Override
    public void onCreate(SQLiteDatabase db) {
*//*        db.execSQL(CREATE_COURSE_TABLE);
        db.execSQL(CREATE_SUBCOURSE_TABLE);
        db.execSQL(CREATE_COURSEHOLE_TABLE);
        db.execSQL(CREATE_COURSEHOLEINFO_TABLE);
        db.execSQL(CREATE_ROUND_TABLE);
        db.execSQL(CREATE_SUBROUND_TABLE);
        db.execSQL(CREATE_ROUNDHOLE_TABLE);
        db.execSQL(CREATE_SHOT_TABLE);
        db.execSQL(CREATE_SHOTTYPE_TABLE);
        db.execSQL(CREATE_SHOTLINK_TABLE);
        db.execSQL(CREATE_PLAYER_TABLE);
        db.execSQL(CREATE_CLUB_TABLE);
        db.execSQL(CREATE_BAG_TABLE);
 *//*
    }

    *//**
     * Perform necessary actions for upgrading the database from oldVersion to newVersion
     *
     * @param db
     * @param oldVersion Old db version
     * @param newVersion New db version
     *//*
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
*//*        //for now just drop the old tables and re-create them?
        db.execSQL("DROP TABLE IF EXISTS" + COURSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SUBCOURSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + COURSEHOLE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + COURSEHOLEINFO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + ROUND_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SUBROUND_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + ROUNDHOLE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SHOT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SHOTTYPE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + SHOTLINK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + PLAYER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + CLUB_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + BAG_TABLE);

        onCreate(db);*//*
    }*/




}
