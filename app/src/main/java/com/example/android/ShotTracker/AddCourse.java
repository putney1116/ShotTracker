package com.example.android.ShotTracker;

import java.lang.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.CourseHoleDAO;
import com.example.android.ShotTracker.db.CourseHoleInfoDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;
import com.example.android.ShotTracker.objects.SubCourse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static java.lang.Character.toUpperCase;


public class AddCourse extends Activity implements OnClickListener, OnMapClickListener{

    private Fragment frag;
    private CourseDAO savedCourseDAO = null;
    private SubCourseDAO savedSubCourseDAO = null;
    private CourseHoleDAO savedCourseHoleDAO = null;
    private CourseHoleInfoDAO savedCourseHoleInfoDAO = null;

    private Course savedCourse = null;
    private SubCourse savedSubCourse = null;
    private CourseHole savedCourseHole = null;
    private CourseHoleInfo savedCourseHoleInfo = null;
    private long courseID;
    private long subCourseID;
    private long courseHoleID;
    private long courseHoleInfoID;




    private AlertDialog.Builder builder;
    private AlertDialog.Builder finishBuilder;
    private AlertDialog.Builder nextNineBuilder;

    private String courseName = "";
    private String courseZipCode = "";
    private int numberOf9s = 0;

    private EditText input = null;
    private TextView inputText = null;

    private int[][] blueHole = null;
    private int[][] whiteHole = null;
    private int[][] redHole = null;
    private int[][] parHole = null;
    private int[][] handicapHole = null;
    private int[][] handicapWomanHole = null;
    private int[][] parWomanHole = null;
    private int[][] holenumbers = null;

    private String GC = "golf course";
    private String CC = "country club";
    private String CR = "course";
    private String GF = "golf";
    private String LK = "links";

    private String[] locationInfo = new String[] {"Green Front","Green Middle","Green Back","Tee"};
    private String[] subCourseNameHint = new String[] {"Front Nine","Back Nine","Third Nine","Fourth Nine","Fifth Nine"};
    private String[] subCourseName = new String[] {"","","","",""};

    private TextView instr;

    private GoogleMap map;
    private Marker clubhouseMarker;
    private Marker teeBoxMarker;
    private Marker greenFrontMarker;
    private Marker greenMiddleMarker;
    private Marker greenBackMarker;
    private int click_state = 1;
    private LatLng location = null;
    private LatLng redraw_greenMiddle = null;
    private LatLng redraw_greenFront = null;
    private LatLng redraw_teeBox = null;
    private CameraUpdate cam_update;
    private float firstHole_zoom = 15;
    private float teeBox_zoom = 19;
    private float hazard_zoom = 17;
    private float green_zoom = 20;
    private float move_zoom = 18;

    private String strAddress;
    private List<Address> address;
    private Geocoder coder;

    private double[][][] teeBox_location = null;
    private double[][][] greenFront_location = null;
    private double[][][] greenMiddle_location = null;
    private double[][][] greenBack_location = null;
    private LatLng current_zoom_center;
    private int current_hole = 1;
    private int current_nine = 1;
    private Button confirmButton;
    private Button backButton;
    private int previous_state = 0;
    private int screenState = 1;
    private int holenumberstart = 0;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcoursemaintop);

        buildDialog();
        buildFinishDialog();
        buildNextNineDialog();

        backButtonMainInitializer();
        nextButtonMainInitializer();

        coder = new Geocoder(this);

        savedCourseHoleInfoDAO = new CourseHoleInfoDAO(this);
        savedCourseHoleDAO = new CourseHoleDAO(this);
        savedSubCourseDAO = new SubCourseDAO(this);
        savedCourseDAO = new CourseDAO(this);

    }

    private void arrayInitializer(){

        blueHole = new int[9][numberOf9s];
        whiteHole = new int[9][numberOf9s];
        redHole = new int[9][numberOf9s];
        parHole = new int[9][numberOf9s];
        handicapHole = new int[9][numberOf9s];
        handicapWomanHole = new int[9][numberOf9s];
        parWomanHole = new int[9][numberOf9s];
        holenumbers = new int[9][numberOf9s];

        for(int x = 0;x < 9; x++) {
            for(int y = 0;y < numberOf9s; y++) {
                blueHole[x][y] = 0;
                whiteHole[x][y] = 0;
                redHole[x][y] = 0;
                parHole[x][y] = 4;
                handicapHole[x][y] = 0;
                handicapWomanHole[x][y] = 0;
                parWomanHole[x][y] = 0;
                holenumbers[x][y] = 0;
            }
        }

        teeBox_location = new double[2][9][numberOf9s];
        greenFront_location = new double[2][9][numberOf9s];
        greenMiddle_location = new double[2][9][numberOf9s];
        greenBack_location = new double[2][9][numberOf9s];
    }

    //If the back button is pressed, the dialog to confirm the exit is displayed
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            switch(screenState){
                case 1:
                    clickBackButtonMain();
                    break;
                case 2:
                    clickBackButtonSubcourseName();
                    break;
                case 3:
                    clickBackButtonBlue();
                    break;
                case 4:
                    clickBackButtonWhite();
                    break;
                case 5:
                    clickBackButtonRed();
                    break;
                case 6:
                    clickBackButtonPar();
                    break;
                case 7:
                    clickBackButtonHandicap();
                    break;
                case 8:
                    clickBackButtonHandicapWoman();
                    break;
                case 9:
                    clickMapBackButton();
                    break;
                case 10:
                    clickHoleNumbersBackButton();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Dialog asks the user if they would like to continue exiting even though there are changes
    private void buildDialog(){
        builder = new AlertDialog.Builder(AddCourse.this);
        builder.setMessage("Your changes have not be saved, would you still like to exit?");
        builder.setCancelable(true);

        //If the user would like to continue exiting
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                //Closes the present activity and returns the display to the home screen
                finish();
            }
        });

        //If the user does not want to exit the add course screen
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //The dialog is closed

                dialog.cancel();
            }
        });
    }

    private void buildFinishDialog(){
        finishBuilder = new AlertDialog.Builder(AddCourse.this);


        finishBuilder.setMessage("Done entering information for this course?");

        finishBuilder.setCancelable(true);

        //If the user would like to continue exiting
        finishBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                //Closes the present activity and returns the display to the home screen
                previous_state = 0;
                click_state = 1;
                current_hole = 1;
                save_course_data();
            }
        });

        //If the user does not want to exit the add course screen
        finishBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //The dialog is closed
                current_hole = current_hole - 1;
                current_nine = current_nine - 1;
                greenBackMarker.remove();
                greenMiddleMarker.showInfoWindow();
                dialog.cancel();
            }
        });
    }

    private void buildNextNineDialog(){
        nextNineBuilder = new AlertDialog.Builder(AddCourse.this);
        nextNineBuilder.setMessage("Done entering information for this set of nine holes?");

        nextNineBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                //Closes the present activity and returns the display to the home screen
                previous_state = 0;
                click_state = 1;
                current_hole = 1;
                next_nine();

            }
        });

        //If the user does not want to exit the add course screen
        nextNineBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //The dialog is closed
                current_hole = current_hole - 1;
                current_nine = current_nine - 1;
                greenBackMarker.remove();
                greenMiddleMarker.showInfoWindow();
                dialog.cancel();
            }
        });
    }

    //If the next button is pressed, the next screen is displayed
    private void nextButtonMainInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCourseTop);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.courseNameEditAddCourse);
                    courseName = input.getText().toString();

                    //Attempt to make first letter of each word upper case - currently broken, going out of bounds somewhere
                    /*
                    char[] temp = courseName.toCharArray();

                    if(temp[0] != ' ') {
                        temp[0] = Character.toUpperCase(temp[0]);
                    }
                    for(int x = 0;x<temp.length-1;x++){
                        if(temp[x] == ' ' && x+1 < temp.length && temp[x+1] != ' '){
                            temp[x+1] = Character.toUpperCase(temp[x+1]);
                        }
                    }

                    courseName = temp.toString();
                    */

                    input = (EditText)findViewById(R.id.zipCodeEditAddCourse);
                    courseZipCode = input.getText().toString();




                    input = (EditText)findViewById(R.id.numberOfNinesEditAddCourse);
                    if(input.getText().toString().equals(""))
                        numberOf9s = 0;
                    else
                        numberOf9s = Integer.parseInt(input.getText().toString());


                    //\todo Cap Number of Nines at 5



                    if(courseName.equals("") || courseZipCode.equals("") || numberOf9s == 0){
                        Toast.makeText(AddCourse.this, "Please enter a Name, Zip Code, and Number of 9's", Toast.LENGTH_LONG).show();
                    }
                    else if (courseZipCode.length() != 5)
                        Toast.makeText(AddCourse.this, "Zip Code must by 5 characters", Toast.LENGTH_LONG).show();
                    else {
                        if (!isNetworkAvailable()) {
                            Toast toast = Toast.makeText(getApplicationContext(), "No Internet connection. Please connect to the Internet and try again.", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        } else {
                            if (courseName.toLowerCase().contains(CC) || courseName.toLowerCase().contains(CR) || courseName.toLowerCase().contains(GF) || courseName.toLowerCase().contains(LK)) {
                                strAddress = courseName + ", " + courseZipCode;
                            } else {
                                strAddress = courseName + " " + GC + ", " + courseZipCode;
                            }


                            try {
                                address = coder.getFromLocationName(strAddress, 5);
                            } catch (IOException e) {
                                Toast toast = Toast.makeText(getApplicationContext(), "No course found with that name and area code. Please try again.", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                return;

                            }
                            Address location_temp = address.get(0);
                            double temp1 = location_temp.getLatitude();
                            double temp2 = location_temp.getLongitude();

                            location = new LatLng(temp1, temp2);
                        }

                        arrayInitializer();

                        setContentView(R.layout.subcoursenames);
                        EditText[] subcourseNameEdit = new EditText[]{(EditText) findViewById(R.id.subcoursename2),
                                (EditText) findViewById(R.id.subcoursename3),
                                (EditText) findViewById(R.id.subcoursename4),
                                (EditText) findViewById(R.id.subcoursename5)};

                        TextView[] subcourseNameText = new TextView[]{(TextView) findViewById(R.id.subcoursename2text),
                                (TextView) findViewById(R.id.subcoursename3text),
                                (TextView) findViewById(R.id.subcoursename4text),
                                (TextView) findViewById(R.id.subcoursename5text)};



                        for (int y = 1; y < numberOf9s; y++) {
                            subcourseNameEdit[y - 1].setVisibility(View.VISIBLE);
                            subcourseNameText[y - 1].setVisibility(View.VISIBLE);
                        }

                        setHints();
                        nextButtonSubcourseNameInitializer();
                        backButtonSubcourseNameInitializer();

                    }
                    screenState = 2;

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    private void setHints(){

        //Sets the hints to the defaults set by the settings page
        EditText subcourseName = (EditText)findViewById(R.id.subcoursename1);
        if(subCourseName[0].equals(""))
            subcourseName.setHint(subCourseNameHint[0]);
        else
            subcourseName.setText(subCourseName[0]);

        subcourseName = (EditText)findViewById(R.id.subcoursename2);
        if(subCourseName[1].equals(""))
            subcourseName.setHint(subCourseNameHint[1]);
        else
            subcourseName.setText(subCourseName[1]);

        subcourseName = (EditText)findViewById(R.id.subcoursename3);
        if(subCourseName[2].equals(""))
            subcourseName.setHint(subCourseNameHint[2]);
        else
            subcourseName.setText(subCourseName[2]);

        subcourseName = (EditText)findViewById(R.id.subcoursename4);
        if(subCourseName[3].equals(""))
            subcourseName.setHint(subCourseNameHint[3]);
        else
            subcourseName.setText(subCourseName[3]);

        subcourseName = (EditText)findViewById(R.id.subcoursename5);
        if(subCourseName[4].equals(""))
            subcourseName.setHint(subCourseNameHint[4]);
        else
            subcourseName.setText(subCourseName[4]);
    }

    private void nextButtonSubcourseNameInitializer(){
        TextView courseNameText = (TextView) findViewById(R.id.subcoursecoursename);

        courseNameText.setText(courseName);

        Button nextButton = (Button)findViewById(R.id.subcoursenamesfinishbutton);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.subcoursename1);
                    if(!input.getText().toString().equals(""))
                        subCourseName[0] = input.getText().toString();
                    else
                        subCourseName[0] = input.getHint().toString();

                    input = (EditText)findViewById(R.id.subcoursename2);
                    if(!input.getText().toString().equals(""))
                        subCourseName[1] = input.getText().toString();
                    else
                        subCourseName[1] = input.getHint().toString();

                    input = (EditText)findViewById(R.id.subcoursename3);
                    if(!input.getText().toString().equals(""))
                        subCourseName[2] = input.getText().toString();
                    else
                        subCourseName[2] = input.getHint().toString();

                    input = (EditText)findViewById(R.id.subcoursename4);
                    if(!input.getText().toString().equals(""))
                        subCourseName[3] = input.getText().toString();
                    else
                        subCourseName[3] = input.getHint().toString();

                    input = (EditText)findViewById(R.id.subcoursename5);
                    if(!input.getText().toString().equals(""))
                        subCourseName[4] = input.getText().toString();
                    else
                        subCourseName[4] = input.getHint().toString();

                    setContentView(R.layout.addcourseholenumbers);
                    nextButtonHoleNumbersInitializer();
                    backButtonHoleNumbersInitializer();

                    screenState = 10;


                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }


    private void nextButtonHoleNumbersInitializer(){
        TextView courseNameText = (TextView) findViewById(R.id.holenumberscoursename);

        courseNameText.setText(courseName);

        RadioGroup radiogroup = (RadioGroup)findViewById(R.id.holenumbersradiobuttons);

        radiogroup.check(radiogroup.getChildAt(holenumberstart).getId());
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
                int checkedRadioButton = rGroup.getCheckedRadioButtonId();

                switch(checkedRadioButton){
                    case R.id.holenumbersradioButton1:
                        holenumberstart = 0;
                        break;
                    case R.id.holenumbersradioButton2:
                        holenumberstart = 1;
                        break;
                    case R.id.holenumbersradioButton3:
                        holenumberstart = 2;
                        break;
                    case R.id.holenumbersradioButton4:
                        holenumberstart = 3;
                        break;
                    case R.id.holenumbersradioButton5:
                        holenumberstart = 4;
                        break;
                }

            }
        });

        Button nextButton = (Button)findViewById(R.id.holenumbersfinishbutton);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                int startingHoleNumber = 1+9*holenumberstart;
                for(int x = 0;x<9;x++){
                    holenumbers[x][current_nine] = startingHoleNumber+x;
                }

        setContentView(R.layout.addcoursemainblue);
        nextButtonBlueInitializer();
        backButtonBlueInitializer();

        inputText = (TextView) findViewById(R.id.courseNameTextAddCourseBlue);
        inputText.setText(courseName);

        input = (EditText) findViewById(R.id.blueHole1EditAddCourse);
        if (blueHole[0][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[0][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole2EditAddCourse);
        if (blueHole[1][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[1][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole3EditAddCourse);
        if (blueHole[2][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[2][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole4EditAddCourse);
        if (blueHole[3][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[3][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole5EditAddCourse);
        if (blueHole[4][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[4][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole6EditAddCourse);
        if (blueHole[5][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[5][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole7EditAddCourse);
        if (blueHole[6][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[6][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole8EditAddCourse);
        if (blueHole[7][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[7][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole9EditAddCourse);
        if (blueHole[8][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[8][current_nine-1]));

        screenState = 3;
            }
        });

    }

    private void clickHoleNumbersBackButton(){
        setContentView(R.layout.subcoursenames);
        EditText[] subcourseNameEdit = new EditText[]{(EditText) findViewById(R.id.subcoursename2),
                (EditText) findViewById(R.id.subcoursename3),
                (EditText) findViewById(R.id.subcoursename4),
                (EditText) findViewById(R.id.subcoursename5)};

        TextView[] subcourseNameText = new TextView[]{(TextView) findViewById(R.id.subcoursename2text),
                (TextView) findViewById(R.id.subcoursename3text),
                (TextView) findViewById(R.id.subcoursename4text),
                (TextView) findViewById(R.id.subcoursename5text)};

        for (int y = 1; y < numberOf9s; y++) {
            subcourseNameEdit[y - 1].setVisibility(View.VISIBLE);
            subcourseNameText[y - 1].setVisibility(View.VISIBLE);
        }

        setHints();
        nextButtonSubcourseNameInitializer();
        backButtonSubcourseNameInitializer();
        screenState = 2;
    }

    private void backButtonHoleNumbersInitializer(){
        Button backButton = (Button)findViewById(R.id.holenumbersbackbutton);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickHoleNumbersBackButton();

            }
        });
    }
    //If the next button is pressed, the next screen is displayed
    private void nextButtonBlueInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCourseBlue);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.blueHole1EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[0][current_nine-1] = 0;
                    else
                        blueHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole2EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[1][current_nine-1] = 0;
                    else
                        blueHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole3EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[2][current_nine-1] = 0;
                    else
                        blueHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole4EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[3][current_nine-1] = 0;
                    else
                        blueHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole5EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[4][current_nine-1] = 0;
                    else
                        blueHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole6EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[5][current_nine-1] = 0;
                    else
                        blueHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole7EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[6][current_nine-1] = 0;
                    else
                        blueHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole8EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[7][current_nine-1] = 0;
                    else
                        blueHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.blueHole9EditAddCourse);
                    if(input.getText().toString().equals(""))
                        blueHole[8][current_nine-1] = 0;
                    else
                        blueHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

                    setContentView(R.layout.addcoursemainwhite);
                    nextButtonWhiteInitializer();
                    backButtonWhiteInitializer();

                    inputText = (TextView)findViewById(R.id.courseNameTextAddCourseWhite);
                    inputText.setText(courseName);

                    input = (EditText)findViewById(R.id.whiteHole1EditAddCourse);
                    if(whiteHole[0][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[0][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole2EditAddCourse);
                    if(whiteHole[1][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[1][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole3EditAddCourse);
                    if(whiteHole[2][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[2][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole4EditAddCourse);
                    if(whiteHole[3][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[3][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole5EditAddCourse);
                    if(whiteHole[4][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[4][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole6EditAddCourse);
                    if(whiteHole[5][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[5][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole7EditAddCourse);
                    if(whiteHole[6][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[6][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole8EditAddCourse);
                    if(whiteHole[7][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[7][current_nine-1]));

                    input = (EditText)findViewById(R.id.whiteHole9EditAddCourse);
                    if(whiteHole[8][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(whiteHole[8][current_nine-1]));

                    screenState = 4;

                }

                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the next button is pressed, the next screen is displayed
    private void nextButtonWhiteInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCourseWhite);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.whiteHole1EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[0][current_nine-1] = 0;
                    else
                        whiteHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole2EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[1][current_nine-1] = 0;
                    else
                        whiteHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole3EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[2][current_nine-1] = 0;
                    else
                        whiteHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole4EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[3][current_nine-1] = 0;
                    else
                        whiteHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole5EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[4][current_nine-1] = 0;
                    else
                        whiteHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole6EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[5][current_nine-1] = 0;
                    else
                        whiteHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole7EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[6][current_nine-1] = 0;
                    else
                        whiteHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole8EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[7][current_nine-1] = 0;
                    else
                        whiteHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.whiteHole9EditAddCourse);
                    if(input.getText().toString().equals(""))
                        whiteHole[8][current_nine-1] = 0;
                    else
                        whiteHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

                    setContentView(R.layout.addcoursemainred);
                    nextButtonRedInitializer();
                    backButtonRedInitializer();

                    inputText = (TextView)findViewById(R.id.courseNameTextAddCourseRed);
                    inputText.setText(courseName);

                    input = (EditText)findViewById(R.id.redHole1EditAddCourse);
                    if(redHole[0][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[0][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole2EditAddCourse);
                    if(redHole[1][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[1][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole3EditAddCourse);
                    if(redHole[2][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[2][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole4EditAddCourse);
                    if(redHole[3][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[3][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole5EditAddCourse);
                    if(redHole[4][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[4][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole6EditAddCourse);
                    if(redHole[5][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[5][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole7EditAddCourse);
                    if(redHole[6][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[6][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole8EditAddCourse);
                    if(redHole[7][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[7][current_nine-1]));

                    input = (EditText)findViewById(R.id.redHole9EditAddCourse);
                    if(redHole[8][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(redHole[8][current_nine-1]));

                    screenState = 5;

                }

                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the next button is pressed, the next screen is displayed
    private void nextButtonRedInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCourseRed);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.redHole1EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[0][current_nine-1] = 0;
                    else
                        redHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole2EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[1][current_nine-1] = 0;
                    else
                        redHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole3EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[2][current_nine-1] = 0;
                    else
                        redHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole4EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[3][current_nine-1] = 0;
                    else
                        redHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole5EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[4][current_nine-1] = 0;
                    else
                        redHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole6EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[5][current_nine-1] = 0;
                    else
                        redHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole7EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[6][current_nine-1] = 0;
                    else
                        redHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole8EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[7][current_nine-1] = 0;
                    else
                        redHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.redHole9EditAddCourse);
                    if(input.getText().toString().equals(""))
                        redHole[8][current_nine-1] = 0;
                    else
                        redHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

                    setContentView(R.layout.addcoursemainpar);
                    nextButtonParInitializer();
                    backButtonParInitializer();
                    
                    inputText = (TextView)findViewById(R.id.courseNameTextAddCoursePar);
                    inputText.setText(courseName);

                    inputText = (TextView)findViewById(R.id.parHole1EditAddCourse);
                    inputText.setText(Integer.toString(parHole[0][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole2EditAddCourse);
                    inputText.setText(Integer.toString(parHole[1][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole3EditAddCourse);
                    inputText.setText(Integer.toString(parHole[2][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole4EditAddCourse);
                    inputText.setText(Integer.toString(parHole[3][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole5EditAddCourse);
                    inputText.setText(Integer.toString(parHole[4][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole6EditAddCourse);
                    inputText.setText(Integer.toString(parHole[5][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole7EditAddCourse);
                    inputText.setText(Integer.toString(parHole[6][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole8EditAddCourse);
                    inputText.setText(Integer.toString(parHole[7][current_nine-1]));

                    inputText = (TextView)findViewById(R.id.parHole9EditAddCourse);
                    inputText.setText(Integer.toString(parHole[8][current_nine-1]));

                    parMinusButton1Initializer();
                    parMinusButton2Initializer();
                    parMinusButton3Initializer();
                    parMinusButton4Initializer();
                    parMinusButton5Initializer();
                    parMinusButton6Initializer();
                    parMinusButton7Initializer();
                    parMinusButton8Initializer();
                    parMinusButton9Initializer();

                    parPlusButton1Initializer();
                    parPlusButton2Initializer();
                    parPlusButton3Initializer();
                    parPlusButton4Initializer();
                    parPlusButton5Initializer();
                    parPlusButton6Initializer();
                    parPlusButton7Initializer();
                    parPlusButton8Initializer();
                    parPlusButton9Initializer();

                    screenState = 6;

                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the next button is pressed, the next screen is displayed
    private void nextButtonParInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCoursePar);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[0][current_nine-1] == 0 || parHole[1][current_nine-1] == 0 || parHole[2][current_nine-1] == 0 ||
                            parHole[3][current_nine-1] == 0 || parHole[4][current_nine-1] == 0 || parHole[5][current_nine-1] == 0 ||
                            parHole[6][current_nine-1] == 0 || parHole[7][current_nine-1] == 0 || parHole[8][current_nine-1] == 0) {

                        Toast.makeText(AddCourse.this, "Please make sure each hole has a par greater than 0", Toast.LENGTH_LONG).show();
                    }
                    else {
                        setContentView(R.layout.addcoursemainhandicap);
                        nextButtonHandicapInitializer();
                        backButtonHandicapInitializer();

                        inputText = (TextView)findViewById(R.id.courseNameTextAddCourseHandicap);
                        inputText.setText(courseName);

                        input = (EditText)findViewById(R.id.handicapHole1EditAddCourse);
                        if(handicapHole[0][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[0][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole2EditAddCourse);
                        if(handicapHole[1][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[1][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole3EditAddCourse);
                        if(handicapHole[2][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[2][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole4EditAddCourse);
                        if(handicapHole[3][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[3][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole5EditAddCourse);
                        if(handicapHole[4][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[4][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole6EditAddCourse);
                        if(handicapHole[5][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[5][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole7EditAddCourse);
                        if(handicapHole[6][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[6][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole8EditAddCourse);
                        if(handicapHole[7][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[7][current_nine-1]));

                        input = (EditText)findViewById(R.id.handicapHole9EditAddCourse);
                        if(handicapHole[8][current_nine-1] == 0)
                            input.setText("");
                        else
                            input.setText(Integer.toString(handicapHole[8][current_nine-1]));
                    }

                    screenState = 7;

                }

                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the next button is pressed, the next screen is displayed
    private void nextButtonHandicapInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCourseHandicap);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.handicapHole1EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[0][current_nine-1] = 0;
                    else
                        handicapHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole2EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[1][current_nine-1] = 0;
                    else
                        handicapHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole3EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[2][current_nine-1] = 0;
                    else
                        handicapHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole4EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[3][current_nine-1] = 0;
                    else
                        handicapHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole5EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[4][current_nine-1] = 0;
                    else
                        handicapHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole6EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[5][current_nine-1] = 0;
                    else
                        handicapHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole7EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[6][current_nine-1] = 0;
                    else
                        handicapHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole8EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[7][current_nine-1] = 0;
                    else
                        handicapHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapHole9EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapHole[8][current_nine-1] = 0;
                    else
                        handicapHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

                    setContentView(R.layout.addcoursehandicapwomen);
                    nextButtonHandicapWomanInitializer();
                    backButtonHandicapWomanInitializer();

                    inputText = (TextView)findViewById(R.id.courseNameTextAddCourseHandicapWoman);
                    inputText.setText(courseName);

                    input = (EditText)findViewById(R.id.handicapWomanHole1EditAddCourse);
                    if(handicapWomanHole[0][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[0][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole2EditAddCourse);
                    if(handicapWomanHole[1][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[1][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole3EditAddCourse);
                    if(handicapWomanHole[2][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[2][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole4EditAddCourse);
                    if(handicapWomanHole[3][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[3][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole5EditAddCourse);
                    if(handicapWomanHole[4][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[4][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole6EditAddCourse);
                    if(handicapWomanHole[5][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[5][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole7EditAddCourse);
                    if(handicapWomanHole[6][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[6][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole8EditAddCourse);
                    if(handicapWomanHole[7][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[7][current_nine-1]));

                    input = (EditText)findViewById(R.id.handicapWomanHole9EditAddCourse);
                    if(handicapWomanHole[8][current_nine-1] == 0)
                        input.setText("");
                    else
                        input.setText(Integer.toString(handicapWomanHole[8][current_nine-1]));

                    screenState = 8;

                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the next button is pressed, the next screen is displayed
    private void nextButtonHandicapWomanInitializer(){
        Button nextButton = (Button)findViewById(R.id.nextButtonAddCourseHandicapWoman);

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    input = (EditText)findViewById(R.id.handicapWomanHole1EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[0][current_nine-1] = 0;
                    else
                        handicapWomanHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole2EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[1][current_nine-1] = 0;
                    else
                        handicapWomanHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole3EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[2][current_nine-1] = 0;
                    else
                        handicapWomanHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole4EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[3][current_nine-1] = 0;
                    else
                        handicapWomanHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole5EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[4][current_nine-1] = 0;
                    else
                        handicapWomanHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole6EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[5][current_nine-1] = 0;
                    else
                        handicapWomanHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole7EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[6][current_nine-1] = 0;
                    else
                        handicapWomanHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole8EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[7][current_nine-1] = 0;
                    else
                        handicapWomanHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

                    input = (EditText)findViewById(R.id.handicapWomanHole9EditAddCourse);
                    if(input.getText().toString().equals(""))
                        handicapWomanHole[8][current_nine-1] = 0;
                    else
                        handicapWomanHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

                    //Initializes map view
                    mapInitializer();
                    confirm_button_initializer();
                    back_button_initializer();
                    screenState = 9;

                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    private void clickBackButtonMain(){
        try{
            //Checks if any changes have been made
            EditText input = (EditText)findViewById(R.id.courseNameEditAddCourse);
            if(input.getText().toString().equals("")){
                input = (EditText)findViewById(R.id.zipCodeEditAddCourse);
                if(input.getText().toString().equals("")){
                    input = (EditText)findViewById(R.id.numberOfNinesEditAddCourse);
                    if(input.getText().toString().equals("")){
                        //If no changes have been made the present activity is closed and the display returns to the home screen
                        finish();
                    }
                    else{
                        //If changes have been made, the dialog box asking for save confirmation is displayed
                        builder.show();
                    }
                }
                else{
                    //If changes have been made, the dialog box asking for save confirmation is displayed
                    builder.show();
                }
            }
            else{
                //If changes have been made, the dialog box asking for save confirmation is displayed
                builder.show();
            }
        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, the dialog to confirm the exit is displayed
    private void backButtonMainInitializer(){
        Button backButton = (Button)findViewById(R.id.backButtonAddCourseTop);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonMain();

            }
        });
    }

    private void clickBackButtonBlue(){

        try {
            input = (EditText) findViewById(R.id.blueHole1EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[0][current_nine - 1] = 0;
            else
                blueHole[0][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole2EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[1][current_nine - 1] = 0;
            else
                blueHole[1][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole3EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[2][current_nine - 1] = 0;
            else
                blueHole[2][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole4EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[3][current_nine - 1] = 0;
            else
                blueHole[3][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole5EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[4][current_nine - 1] = 0;
            else
                blueHole[4][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole6EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[5][current_nine - 1] = 0;
            else
                blueHole[5][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole7EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[6][current_nine - 1] = 0;
            else
                blueHole[6][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole8EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[7][current_nine - 1] = 0;
            else
                blueHole[7][current_nine - 1] = Integer.parseInt(input.getText().toString());

            input = (EditText) findViewById(R.id.blueHole9EditAddCourse);
            if (input.getText().toString().equals(""))
                blueHole[8][current_nine - 1] = 0;
            else
                blueHole[8][current_nine - 1] = Integer.parseInt(input.getText().toString());

            setContentView(R.layout.addcourseholenumbers);
            nextButtonHoleNumbersInitializer();
            backButtonHoleNumbersInitializer();
            screenState = 10;
        }
        catch (Exception e){
            System.out.println(e);
        }

    }
    private void backButtonBlueInitializer(){
        final Button backButton = (Button)findViewById(R.id.backButtonAddCourseBlue);

        if(current_nine != 1){
            backButton.setVisibility(View.INVISIBLE);
        }

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonBlue();
            }
        });
    }
    private void clickBackButtonSubcourseName(){
        try{

            setContentView(R.layout.addcoursemaintop);
            nextButtonMainInitializer();
            backButtonMainInitializer();

            input = (EditText) findViewById(R.id.courseNameEditAddCourse);
            input.setText(courseName);
            input = (EditText) findViewById(R.id.zipCodeEditAddCourse);
            input.setText(courseZipCode);
            input = (EditText) findViewById(R.id.numberOfNinesEditAddCourse);
            input.setText(Integer.toString(numberOf9s));
            screenState = 1;


        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, it reverts to the previous screen
    private void backButtonSubcourseNameInitializer(){
        final Button backButton = (Button)findViewById(R.id.subcoursenamesbackbutton);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonSubcourseName();
            }
        });
    }

    private void clickBackButtonWhite(){
        try{
            input = (EditText)findViewById(R.id.whiteHole1EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[0][current_nine-1] = 0;
            else
                whiteHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole2EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[1][current_nine-1] = 0;
            else
                whiteHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole3EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[2][current_nine-1] = 0;
            else
                whiteHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole4EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[3][current_nine-1] = 0;
            else
                whiteHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole5EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[4][current_nine-1] = 0;
            else
                whiteHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole6EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[5][current_nine-1] = 0;
            else
                whiteHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole7EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[6][current_nine-1] = 0;
            else
                whiteHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole8EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[7][current_nine-1] = 0;
            else
                whiteHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.whiteHole9EditAddCourse);
            if(input.getText().toString().equals(""))
                whiteHole[8][current_nine-1] = 0;
            else
                whiteHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

            setContentView(R.layout.addcoursemainblue);
            nextButtonBlueInitializer();
            backButtonBlueInitializer();

            inputText = (TextView)findViewById(R.id.courseNameTextAddCourseBlue);
            inputText.setText(courseName);

            input = (EditText)findViewById(R.id.blueHole1EditAddCourse);
            if(blueHole[0][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[0][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole2EditAddCourse);
            if(blueHole[1][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[1][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole3EditAddCourse);
            if(blueHole[2][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[2][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole4EditAddCourse);
            if(blueHole[3][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[3][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole5EditAddCourse);
            if(blueHole[4][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[4][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole6EditAddCourse);
            if(blueHole[5][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[5][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole7EditAddCourse);
            if(blueHole[6][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[6][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole8EditAddCourse);
            if(blueHole[7][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[7][current_nine-1]));

            input = (EditText)findViewById(R.id.blueHole9EditAddCourse);
            if(blueHole[8][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(blueHole[8][current_nine-1]));

            screenState = 3;

        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, it reverts to the previous screen
    private void backButtonWhiteInitializer(){
        Button backButton = (Button)findViewById(R.id.backButtonAddCourseWhite);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonWhite();
            }
        });
    }

    private void clickBackButtonRed(){
        try{
            input = (EditText)findViewById(R.id.redHole1EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[0][current_nine-1] = 0;
            else
                redHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole2EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[1][current_nine-1] = 0;
            else
                redHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole3EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[2][current_nine-1] = 0;
            else
                redHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole4EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[3][current_nine-1] = 0;
            else
                redHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole5EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[4][current_nine-1] = 0;
            else
                redHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole6EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[5][current_nine-1] = 0;
            else
                redHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole7EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[6][current_nine-1] = 0;
            else
                redHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole8EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[7][current_nine-1] = 0;
            else
                redHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.redHole9EditAddCourse);
            if(input.getText().toString().equals(""))
                redHole[8][current_nine-1] = 0;
            else
                redHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

            setContentView(R.layout.addcoursemainwhite);
            nextButtonWhiteInitializer();
            backButtonWhiteInitializer();

            inputText = (TextView)findViewById(R.id.courseNameTextAddCourseWhite);
            inputText.setText(courseName);

            input = (EditText)findViewById(R.id.whiteHole1EditAddCourse);
            if(whiteHole[0][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[0][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole2EditAddCourse);
            if(whiteHole[1][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[1][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole3EditAddCourse);
            if(whiteHole[2][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[2][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole4EditAddCourse);
            if(whiteHole[3][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[3][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole5EditAddCourse);
            if(whiteHole[4][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[4][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole6EditAddCourse);
            if(whiteHole[5][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[5][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole7EditAddCourse);
            if(whiteHole[6][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[6][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole8EditAddCourse);
            if(whiteHole[7][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[7][current_nine-1]));

            input = (EditText)findViewById(R.id.whiteHole9EditAddCourse);
            if(whiteHole[8][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(whiteHole[8][current_nine-1]));

            screenState = 4;

        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, it reverts to the previous screen
    private void backButtonRedInitializer(){
        Button backButton = (Button)findViewById(R.id.backButtonAddCourseRed);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonRed();
            }
        });
    }

    private void clickBackButtonPar(){
        try{
            setContentView(R.layout.addcoursemainred);
            nextButtonRedInitializer();
            backButtonRedInitializer();

            inputText = (TextView)findViewById(R.id.courseNameTextAddCourseRed);
            inputText.setText(courseName);

            input = (EditText)findViewById(R.id.redHole1EditAddCourse);
            if(redHole[0][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[0][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole2EditAddCourse);
            if(redHole[1][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[1][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole3EditAddCourse);
            if(redHole[2][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[2][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole4EditAddCourse);
            if(redHole[3][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[3][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole5EditAddCourse);
            if(redHole[4][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[4][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole6EditAddCourse);
            if(redHole[5][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[5][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole7EditAddCourse);
            if(redHole[6][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[6][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole8EditAddCourse);
            if(redHole[7][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[7][current_nine-1]));

            input = (EditText)findViewById(R.id.redHole9EditAddCourse);
            if(redHole[8][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(redHole[8][current_nine-1]));

            screenState = 5;

        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, it reverts to the previous screen
    private void backButtonParInitializer(){
        Button backButton = (Button)findViewById(R.id.backButtonAddCoursePar);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonPar();
            }
        });
    }

    private void clickBackButtonHandicap(){
        try{
            input = (EditText)findViewById(R.id.handicapHole1EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[0][current_nine-1] = 0;
            else
                handicapHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole2EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[1][current_nine-1] = 0;
            else
                handicapHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole3EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[2][current_nine-1] = 0;
            else
                handicapHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole4EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[3][current_nine-1] = 0;
            else
                handicapHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole5EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[4][current_nine-1] = 0;
            else
                handicapHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole6EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[5][current_nine-1] = 0;
            else
                handicapHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole7EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[6][current_nine-1] = 0;
            else
                handicapHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole8EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[7][current_nine-1] = 0;
            else
                handicapHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapHole9EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapHole[8][current_nine-1] = 0;
            else
                handicapHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

            setContentView(R.layout.addcoursemainpar);
            nextButtonParInitializer();
            backButtonParInitializer();

            inputText = (TextView)findViewById(R.id.courseNameTextAddCoursePar);
            inputText.setText(courseName);

            inputText = (TextView)findViewById(R.id.parHole1EditAddCourse);
            inputText.setText(Integer.toString(parHole[0][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole2EditAddCourse);
            inputText.setText(Integer.toString(parHole[1][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole3EditAddCourse);
            inputText.setText(Integer.toString(parHole[2][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole4EditAddCourse);
            inputText.setText(Integer.toString(parHole[3][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole5EditAddCourse);
            inputText.setText(Integer.toString(parHole[4][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole6EditAddCourse);
            inputText.setText(Integer.toString(parHole[5][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole7EditAddCourse);
            inputText.setText(Integer.toString(parHole[6][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole8EditAddCourse);
            inputText.setText(Integer.toString(parHole[7][current_nine-1]));

            inputText = (TextView)findViewById(R.id.parHole9EditAddCourse);
            inputText.setText(Integer.toString(parHole[8][current_nine-1]));

            parMinusButton1Initializer();
            parMinusButton2Initializer();
            parMinusButton3Initializer();
            parMinusButton4Initializer();
            parMinusButton5Initializer();
            parMinusButton6Initializer();
            parMinusButton7Initializer();
            parMinusButton8Initializer();
            parMinusButton9Initializer();

            parPlusButton1Initializer();
            parPlusButton2Initializer();
            parPlusButton3Initializer();
            parPlusButton4Initializer();
            parPlusButton5Initializer();
            parPlusButton6Initializer();
            parPlusButton7Initializer();
            parPlusButton8Initializer();
            parPlusButton9Initializer();

            screenState = 6;

        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, it reverts to the previous screen
    private void backButtonHandicapInitializer(){
        Button backButton = (Button)findViewById(R.id.backButtonAddCourseHandicap);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonHandicap();
            }
        });
    }

    private void clickBackButtonHandicapWoman(){
        try{
            input = (EditText)findViewById(R.id.handicapWomanHole1EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[0][current_nine-1] = 0;
            else
                handicapWomanHole[0][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole2EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[1][current_nine-1] = 0;
            else
                handicapWomanHole[1][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole3EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[2][current_nine-1] = 0;
            else
                handicapWomanHole[2][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole4EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[3][current_nine-1] = 0;
            else
                handicapWomanHole[3][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole5EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[4][current_nine-1] = 0;
            else
                handicapWomanHole[4][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole6EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[5][current_nine-1] = 0;
            else
                handicapWomanHole[5][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole7EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[6][current_nine-1] = 0;
            else
                handicapWomanHole[6][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole8EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[7][current_nine-1] = 0;
            else
                handicapWomanHole[7][current_nine-1] = Integer.parseInt(input.getText().toString());

            input = (EditText)findViewById(R.id.handicapWomanHole9EditAddCourse);
            if(input.getText().toString().equals(""))
                handicapWomanHole[8][current_nine-1] = 0;
            else
                handicapWomanHole[8][current_nine-1] = Integer.parseInt(input.getText().toString());

            setContentView(R.layout.addcoursemainhandicap);
            nextButtonHandicapInitializer();
            backButtonHandicapInitializer();

            inputText = (TextView)findViewById(R.id.courseNameTextAddCourseHandicap);
            inputText.setText(courseName);

            input = (EditText)findViewById(R.id.handicapHole1EditAddCourse);
            if(handicapHole[0][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[0][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole2EditAddCourse);
            if(handicapHole[1][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[1][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole3EditAddCourse);
            if(handicapHole[2][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[2][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole4EditAddCourse);
            if(handicapHole[3][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[3][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole5EditAddCourse);
            if(handicapHole[4][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[4][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole6EditAddCourse);
            if(handicapHole[5][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[5][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole7EditAddCourse);
            if(handicapHole[6][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[6][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole8EditAddCourse);
            if(handicapHole[7][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[7][current_nine-1]));

            input = (EditText)findViewById(R.id.handicapHole9EditAddCourse);
            if(handicapHole[8][current_nine-1] == 0)
                input.setText("");
            else
                input.setText(Integer.toString(handicapHole[8][current_nine-1]));

            screenState = 7;

        }catch(Exception e) {
            System.out.println(e);
        }
    }
    //If the back button is pressed, it reverts to the previous screen
    private void backButtonHandicapWomanInitializer(){
        Button backButton = (Button)findViewById(R.id.backButtonAddCourseHandicapWoman);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBackButtonHandicapWoman();
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton1Initializer(){
        Button button = (Button)findViewById(R.id.hole1MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[0][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole1EditAddCourse);
                        parHole[0][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[0][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton2Initializer(){
        Button button = (Button)findViewById(R.id.hole2MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[1][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole2EditAddCourse);
                        parHole[1][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[1][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton3Initializer(){
        Button button = (Button)findViewById(R.id.hole3MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[2][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole3EditAddCourse);
                        parHole[2][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[2][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton4Initializer(){
        Button button = (Button)findViewById(R.id.hole4MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[3][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole4EditAddCourse);
                        parHole[3][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[3][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton5Initializer(){
        Button button = (Button)findViewById(R.id.hole5MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[4][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole5EditAddCourse);
                        parHole[4][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[4][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton6Initializer(){
        Button button = (Button)findViewById(R.id.hole6MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[5][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole6EditAddCourse);
                        parHole[5][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[5][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton7Initializer(){
        Button button = (Button)findViewById(R.id.hole7MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[6][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole7EditAddCourse);
                        parHole[6][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[6][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton8Initializer(){
        Button button = (Button)findViewById(R.id.hole8MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[7][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole8EditAddCourse);
                        parHole[7][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[7][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the minus button is pressed, the par is decremented
    private void parMinusButton9Initializer(){
        Button button = (Button)findViewById(R.id.hole9MinusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(parHole[8][current_nine-1] > 0) {
                        inputText = (TextView) findViewById(R.id.parHole9EditAddCourse);
                        parHole[8][current_nine-1]--;
                        inputText.setText(Integer.toString(parHole[8][current_nine-1]));
                    }

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the plus button is pressed, the par is incremented
    private void parPlusButton1Initializer() {
        Button button = (Button) findViewById(R.id.hole1PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole1EditAddCourse);
                    parHole[0][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[0][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton2Initializer() {
        Button button = (Button) findViewById(R.id.hole2PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole2EditAddCourse);
                    parHole[1][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[1][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton3Initializer() {
        Button button = (Button) findViewById(R.id.hole3PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole3EditAddCourse);
                    parHole[2][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[2][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton4Initializer() {
        Button button = (Button) findViewById(R.id.hole4PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole4EditAddCourse);
                    parHole[3][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[3][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton5Initializer() {
        Button button = (Button) findViewById(R.id.hole5PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole5EditAddCourse);
                    parHole[4][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[4][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton6Initializer() {
        Button button = (Button) findViewById(R.id.hole6PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole6EditAddCourse);
                    parHole[5][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[5][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton7Initializer() {
        Button button = (Button) findViewById(R.id.hole7PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole7EditAddCourse);
                    parHole[6][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[6][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

        //If the plus button is pressed, the par is incremented
    private void parPlusButton8Initializer() {
        Button button = (Button) findViewById(R.id.hole8PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputText = (TextView) findViewById(R.id.parHole8EditAddCourse);
                    parHole[7][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[7][current_nine-1]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    //If the plus button is pressed, the par is incremented
    private void parPlusButton9Initializer(){
        Button button = (Button)findViewById(R.id.hole9PlusButtonAddCoursePar);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    inputText = (TextView)findViewById(R.id.parHole9EditAddCourse);
                    parHole[8][current_nine-1]++;
                    inputText.setText(Integer.toString(parHole[8][current_nine-1]));

                }catch(Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    private void clickMapBackButton(){
        switch (previous_state){
            case 0:

                confirmButton.setVisibility(View.INVISIBLE);
                click_state = 1;

                setContentView(R.layout.addcoursehandicapwomen);
                nextButtonHandicapWomanInitializer();
                backButtonHandicapWomanInitializer();
                screenState = 8;

                inputText = (TextView)findViewById(R.id.courseNameTextAddCourseHandicapWoman);
                inputText.setText(courseName);

                input = (EditText)findViewById(R.id.handicapWomanHole1EditAddCourse);
                if(handicapWomanHole[0][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[0][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole2EditAddCourse);
                if(handicapWomanHole[1][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[1][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole3EditAddCourse);
                if(handicapWomanHole[2][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[2][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole4EditAddCourse);
                if(handicapWomanHole[3][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[3][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole5EditAddCourse);
                if(handicapWomanHole[4][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[4][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole6EditAddCourse);
                if(handicapWomanHole[5][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[5][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole7EditAddCourse);
                if(handicapWomanHole[6][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[6][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole8EditAddCourse);
                if(handicapWomanHole[7][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[7][current_nine-1]));

                input = (EditText)findViewById(R.id.handicapWomanHole9EditAddCourse);
                if(handicapWomanHole[8][current_nine-1] == 0)
                    input.setText("");
                else
                    input.setText(Integer.toString(handicapWomanHole[8][current_nine-1]));

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(frag);
                ft.commit();

                break;

            case 1:

                map.clear();
                cam_update = CameraUpdateFactory.newLatLngZoom(location, firstHole_zoom);
                map.moveCamera(cam_update);
                draw_clubHouseMarker(location);
                confirmButton.setVisibility(View.INVISIBLE);
                click_state = previous_state;
                update_text_instructions(click_state);
                previous_state = 0;

                break;

            case 2:

                map.clear();
                confirmButton.setVisibility(View.INVISIBLE);
                click_state = previous_state;
                update_text_instructions(click_state);
                teeBoxMarker.remove();
                redraw_greenMiddle = new LatLng(greenMiddle_location[0][current_hole-2][current_nine-1], greenMiddle_location[1][current_hole-2][current_nine-1]);
                cam_update = CameraUpdateFactory.newLatLngZoom(redraw_greenMiddle,move_zoom);
                map.animateCamera(cam_update);
                previous_state = 7;

                break;
            case 3:
                map.clear();
                confirmButton.setVisibility(View.INVISIBLE);
                click_state = previous_state;
                update_text_instructions(click_state);
                redraw_teeBox = new LatLng(teeBox_location[0][current_hole-1][current_nine-1], teeBox_location[1][current_hole-1][current_nine-1]);
                draw_teeBoxMarker(redraw_teeBox);
                cam_update = CameraUpdateFactory.newLatLngZoom(redraw_teeBox, hazard_zoom);
                map.animateCamera(cam_update);
                previous_state = 4;

                break;
            case 4:
                map.clear();
                confirmButton.setVisibility(View.INVISIBLE);
                click_state = previous_state;
                update_text_instructions(click_state);
                redraw_teeBox = new LatLng(teeBox_location[0][current_hole-1][current_nine-1], teeBox_location[1][current_hole-1][current_nine-1]);
                cam_update = CameraUpdateFactory.newLatLngZoom(redraw_teeBox,teeBox_zoom);
                map.animateCamera(cam_update);
                if(current_hole == 1){
                    previous_state = 1;
                }
                else {
                    previous_state = 2;
                }
                break;
            case 5:
                //clear and re-zoom to green middle location
                map.clear();
                confirmButton.setVisibility(View.INVISIBLE);
                greenFrontMarker.remove();
                click_state = previous_state;
                update_text_instructions(click_state);
                previous_state = 3;

                break;
            case 6:
                //clear, re-draw front marker, re-zoom to middle location
                map.clear();
                redraw_greenFront = new LatLng(greenFront_location[0][current_hole-1][current_nine-1], greenFront_location[1][current_hole-1][current_nine-1]);
                draw_greenFrontMarker(redraw_greenFront);
                confirmButton.setVisibility(View.INVISIBLE);
                greenMiddleMarker.remove();
                click_state = previous_state;
                update_text_instructions(click_state);
                previous_state = 5;

                break;
            case 7:
                map.clear();
                current_hole = current_hole - 1;
                redraw_greenMiddle = new LatLng(greenMiddle_location[0][current_hole-1][current_nine-1], greenMiddle_location[1][current_hole-1][current_nine-1]);
                cam_update = CameraUpdateFactory.newLatLngZoom(redraw_greenMiddle, green_zoom);
                redraw_greenFront = new LatLng(greenFront_location[0][current_hole-1][current_nine-1], greenFront_location[1][current_hole-1][current_nine-1]);
                draw_greenFrontMarker(redraw_greenFront);
                draw_greenMiddleMarker(redraw_greenMiddle);
                map.animateCamera(cam_update);
                confirmButton.setVisibility(View.INVISIBLE);
                click_state = previous_state;
                update_text_instructions(click_state);
                previous_state = 6;

                break;
        }
    }
    private void back_button_initializer(){
        //\todo Move back button such that it is not covering the zoom in and out buttons
        backButton = (Button)findViewById(R.id.AddCourseMarkerBack);
        backButton.setVisibility(View.VISIBLE);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMapBackButton();

            }
        });
    }
    private void draw_greenFrontMarker(LatLng greenFrontLocation){
        greenFrontMarker = map.addMarker(new MarkerOptions()
                .position(greenFrontLocation)
                .title("Green Front")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.greenfrontmarker))
                .anchor((float)0.37, (float)1.0));

        greenFrontMarker.showInfoWindow();
    }

    private void draw_greenMiddleMarker(LatLng greenMiddleLocation){
        greenMiddleMarker = map.addMarker(new MarkerOptions()
                .position(greenMiddleLocation)
                .title("Green Middle")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.greenmiddlemarker))
                .anchor((float)0.37, (float)1.0));

        greenMiddleMarker.showInfoWindow();
    }

    private void draw_greenBackMarker(LatLng greenBackLocation){
        greenBackMarker = map.addMarker(new MarkerOptions()
                .position(greenBackLocation)
                .title("Green Back")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.greenbackmarker))
                .anchor((float)0.37, (float)1.0));

        greenBackMarker.showInfoWindow();
    }

    private void draw_teeBoxMarker(LatLng teeBoxLocation){
        teeBoxMarker = map.addMarker(new MarkerOptions()
                .position(teeBoxLocation)
                .title("Tee Box")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.teeboxmarker))
                .anchor((float)0.37, (float)1.0));

        teeBoxMarker.showInfoWindow();
    }

    private void draw_clubHouseMarker(LatLng clubhouseLocation){
        clubhouseMarker = map.addMarker(new MarkerOptions()
                .position(clubhouseLocation)
                .title("Clubhouse")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.clubhousemarker))
                .anchor((float)0.37, (float)1.0));

        clubhouseMarker.showInfoWindow();
    }

    private void update_text_instructions(int state){
        //\todo Add course and subcourse name as title text
        //\todo Center and format text instructions to be readable
        instr = (TextView) findViewById(R.id.helloTextAddCourseMap);
        switch(state){
            case 1:
                instr.setText("Select the location of first tee box.");
                break;
            case 2:
                instr.setText("Select the location of the tee box for hole " + current_hole + ".");
                break;
            case 3:
                instr.setText("Select the green for hole "+ current_hole + ".");
                break;
            case 4:
                instr.setText("Locate the back of the furthest tee box from the green for hole "+ current_hole + ".");
                break;
            case 5:
                instr.setText("Locate the front edge of the green for hole "+ current_hole + ".");
                break;
            case 6:
                instr.setText("Locate the middle of the green for hole "+ current_hole + ".");
                break;
            case 7:
                instr.setText("Locate the back edge of the green for hole "+ current_hole + ".");
                break;

        }

    }

    private void confirm_button_initializer(){
        confirmButton = (Button)findViewById(R.id.AddCourseMarkersConfirm);
        confirmButton.setVisibility(View.INVISIBLE);

        confirmButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                switch (click_state){
                    case 4:
                        previous_state = click_state;
                        teeBox_location[0][current_hole-1][current_nine-1] = current_zoom_center.latitude;
                        teeBox_location[1][current_hole-1][current_nine-1] = current_zoom_center.longitude;
                        cam_update = CameraUpdateFactory.newLatLngZoom(current_zoom_center, hazard_zoom);
                        map.animateCamera(cam_update);
                        teeBoxMarker.remove();
                        confirmButton.setVisibility(View.INVISIBLE);
                        click_state = 3;
                        update_text_instructions(click_state);
                        break;

                    case 5:
                        previous_state = click_state;
                        greenFront_location[0][current_hole-1][current_nine-1] = current_zoom_center.latitude;
                        greenFront_location[1][current_hole-1][current_nine-1] = current_zoom_center.longitude;
                        click_state = 6;
                        //create new, smaller marker that won't be removed to show green front

                        greenFrontMarker.hideInfoWindow();
                        current_zoom_center = null;
                        confirmButton.setVisibility(View.INVISIBLE);
                        update_text_instructions(click_state);
                        break;

                    case 6:
                        previous_state = click_state;

                        greenMiddle_location[0][current_hole-1][current_nine-1] = current_zoom_center.latitude;
                        greenMiddle_location[1][current_hole-1][current_nine-1] = current_zoom_center.longitude;
                        click_state = 7;

                        greenMiddleMarker.hideInfoWindow();
                        current_zoom_center = null;
                        confirmButton.setVisibility(View.INVISIBLE);
                        update_text_instructions(click_state);
                        //create new, smaller marker that won't be removed to show green middle
                        break;

                    case 7:
                        greenBack_location[0][current_hole-1][current_nine-1] = current_zoom_center.latitude;
                        greenBack_location[1][current_hole-1][current_nine-1] = current_zoom_center.longitude;
                        current_hole += 1;
                        confirmButton.setVisibility(View.INVISIBLE);


                        if (current_hole > 9){
                            current_nine = current_nine + 1;
                            if (current_nine > numberOf9s){
                                finishBuilder.show();
                            }
                            else{
                                nextNineBuilder.show();
                            }

                        }
                        else{
                            previous_state = click_state;
                            click_state = 2;
                            map.clear();
                            cam_update = CameraUpdateFactory.newLatLngZoom(current_zoom_center, move_zoom);
                            map.animateCamera(cam_update);
                            update_text_instructions(click_state);
                        }
                        break;

                }
            }

        });
    }


    private void mapInitializer(){

        try {
            //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.addcoursemap);

            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.addCourseMap))
                    .getMap();
            frag = getFragmentManager().findFragmentById(R.id.addCourseMap);
        }
        catch (InflateException e) {
            System.out.println("Inflation Error as expected");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(frag);
            ft.commit();
        /* map is already there, just return view as it is */
        }

        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        map.setOnMapClickListener(this);

        cam_update = CameraUpdateFactory.newLatLngZoom(location, firstHole_zoom);
        map.moveCamera(cam_update);
        update_text_instructions(click_state);

        draw_clubHouseMarker(location);

    }

    @Override
    public void onClick(View arg0) {
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //\todo Recenter on green clicks such that selection cannot get buried behind a button? - Can still scroll around and re-select - stylistic choice
    @Override
    public void onMapClick(LatLng mark) {
        switch (click_state){
            case 1:
                previous_state = click_state;
                cam_update = CameraUpdateFactory.newLatLngZoom(mark, teeBox_zoom);
                map.animateCamera(cam_update);
                click_state = 4;
                update_text_instructions(click_state);

                break;

            case 2:
                previous_state = click_state;
                cam_update = CameraUpdateFactory.newLatLngZoom(mark, teeBox_zoom);
                map.animateCamera(cam_update);
                click_state = 4;
                update_text_instructions(click_state);

                break;

            case 3:
                previous_state = click_state;
                cam_update = CameraUpdateFactory.newLatLngZoom(mark, green_zoom);
                map.animateCamera(cam_update);
                click_state = 5;
                update_text_instructions(click_state);

                break;

            case 4:

                if(teeBoxMarker != null) {
                    teeBoxMarker.remove();
                }
                draw_teeBoxMarker(mark);
                current_zoom_center = mark;
                if(current_zoom_center != null){
                    confirmButton.setVisibility(View.VISIBLE);
                }
                break;

            case 5:

                if(greenFrontMarker != null) {
                    greenFrontMarker.remove();
                }
                draw_greenFrontMarker(mark);
                current_zoom_center = mark;
                if(current_zoom_center != null){
                    confirmButton.setVisibility(View.VISIBLE);
                }

                break;

            case 6:

                if (greenMiddleMarker != null) {
                    greenMiddleMarker.remove();
                }

                draw_greenMiddleMarker(mark);
                current_zoom_center = mark;
                if(current_zoom_center != null){
                    confirmButton.setVisibility(View.VISIBLE);
                }

                break;

            case 7:

                if (greenBackMarker != null) {
                    greenBackMarker.remove();
                }

                draw_greenBackMarker(mark);
                current_zoom_center = mark;
                if(current_zoom_center != null){
                    confirmButton.setVisibility(View.VISIBLE);
                }

                break;

        }
    }

    public void save_course_data(){
        Toast toast = Toast.makeText(getApplicationContext(),"Finished.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        savedCourse = new Course(courseName,strAddress);
        courseID = savedCourseDAO.createCourse(savedCourse);
        savedCourse.setID(courseID);
        for (int x = 0; x < numberOf9s; x++){
            savedSubCourse = new SubCourse(savedCourse,subCourseName[x]);
            subCourseID = savedSubCourseDAO.createSubCourse(savedSubCourse);
            savedSubCourse.setID(subCourseID);
            for (int y = 0; y < 9; y++){
                savedCourseHole = new CourseHole(savedSubCourse,holenumbers[y][x],parHole[y][x],parWomanHole[y][x],handicapHole[y][x],handicapWomanHole[y][x],blueHole[y][x],whiteHole[y][x],redHole[y][x]);
                courseHoleID = savedCourseHoleDAO.createCourseHole(savedCourseHole);
                savedCourseHole.setID(courseHoleID);
                for (int z = 0; z < 4; z++){
                    switch (z) {
                        case 0:
                            savedCourseHoleInfo = new CourseHoleInfo(savedCourseHole,locationInfo[z],greenFront_location[0][y][x],greenFront_location[1][y][x]);
                            savedCourseHoleInfoDAO.createCourseHoleInfo(savedCourseHoleInfo);
                            break;
                        case 1:
                            savedCourseHoleInfo = new CourseHoleInfo(savedCourseHole,locationInfo[z],greenMiddle_location[0][y][x],greenMiddle_location[1][y][x]);
                            savedCourseHoleInfoDAO.createCourseHoleInfo(savedCourseHoleInfo);
                            break;
                        case 2:
                            savedCourseHoleInfo = new CourseHoleInfo(savedCourseHole,locationInfo[z],greenBack_location[0][y][x],greenBack_location[1][y][x]);
                            savedCourseHoleInfoDAO.createCourseHoleInfo(savedCourseHoleInfo);
                            break;
                        case 3:
                            savedCourseHoleInfo = new CourseHoleInfo(savedCourseHole,locationInfo[z],teeBox_location[0][y][x],teeBox_location[1][y][x]);
                            savedCourseHoleInfoDAO.createCourseHoleInfo(savedCourseHoleInfo);
                            break;

                    }

                }

            }
        }
        finish();
    }

    public void next_nine(){
        setContentView(R.layout.addcoursemainblue);
        nextButtonBlueInitializer();
        backButtonBlueInitializer();

        inputText = (TextView) findViewById(R.id.courseNameTextAddCourseBlue);
        inputText.setText(courseName);

        input = (EditText) findViewById(R.id.blueHole1EditAddCourse);
        if (blueHole[0][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[0][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole2EditAddCourse);
        if (blueHole[1][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[1][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole3EditAddCourse);
        if (blueHole[2][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[2][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole4EditAddCourse);
        if (blueHole[3][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[3][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole5EditAddCourse);
        if (blueHole[4][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[4][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole6EditAddCourse);
        if (blueHole[5][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[5][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole7EditAddCourse);
        if (blueHole[6][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[6][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole8EditAddCourse);
        if (blueHole[7][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[7][current_nine-1]));

        input = (EditText) findViewById(R.id.blueHole9EditAddCourse);
        if (blueHole[8][current_nine-1] == 0)
            input.setText("");
        else
            input.setText(Integer.toString(blueHole[8][current_nine-1]));

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(frag);
        ft.commit();
        previous_state = 0;
    }
}
