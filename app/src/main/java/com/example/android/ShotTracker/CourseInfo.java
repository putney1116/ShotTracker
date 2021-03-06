package com.example.android.ShotTracker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class CourseInfo extends com.google.android.maps.MapActivity implements OnClickListener, OnMapClickListener{
	
	double lat[] = new double[2];
	double lng[] = new double[2];
    //Hey Douche
	
	private Location greenLocation = new Location("");
	private Location playerLocation = new Location("");
	private Location tapLocation = new Location("");
	
	private int padding = 0; // offset from edges of the map in pixels
	private CameraUpdate cu;
	
	private int middleDistance;
	private int distance;
	
	private GoogleMap map;
	private Marker pinMarker;
	private Marker playerMarker;
	private Marker mapClickMarker;
	private Polyline mapPolyline;
	private TabHost tabHost;

	private RelativeLayout scorecardTab; 

	private boolean frontActive = true;
	private String courseName = "";
	private long courseID = -1;
	private int par[] = new int[19];
	private int blueYardage[] = new int[19];
	private int whiteYardage[] = new int[19];
	private int redYardage[] = new int[19];
	private int menHandicap[] = new int[19];
	private int womenHandicap[] = new int[19];
	private int holeNumberText[] = new int [19];
	
	//greenlocations[lat,long][front,middle,back][holenumber]
	private double greenLocations[][][] = new double[2][3][19];
	
	//teelocations[lat,long][holenumber]
	private double teeLocations[][] = new double[2][19];

    //DAOs
    private CourseDAO courseDAO = null;
    private SubCourseDAO subCourseDAO = null;
    private CourseHoleDAO courseHoleDAO = null;
    private CourseHoleInfoDAO courseHoleInfoDAO = null;

	private Course course = null;
	private List<SubCourse> subCourses = null;

	private boolean eighteenHoleRound = true;
    
	public void onCreate(Bundle savedInstanceState) {
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
    	
    	//Loads the player names and course information
    	loadCourseInfo();

		selectLayout();
    	
    	//Initializes the scorecard tab
    	scorecardInitializer();
    	
    	//Loads the front 9 values
    	frontBackViewSwitcher();
    	
    	//Initializes the tab controller
    	tabSetup();  

    	//Initiliazes the map
		mapInitializer();
    	
    	//Initializes the map spinner
    	mapSpinnerSetup();
	}
	
	//Loads the course information
	private void loadCourseInfo(){

        courseDAO = new CourseDAO(this);
        subCourseDAO = new SubCourseDAO(this);
        courseHoleDAO = new CourseHoleDAO(this);
        courseHoleInfoDAO = new CourseHoleInfoDAO(this);

		Intent myIntent = getIntent();
		
		//Loads the course name from the previous activity
		long front9SubCourseID = myIntent.getLongExtra("Front 9 SubCourseID", -1);
		long back9SubCourseID = myIntent.getLongExtra("Back 9 SubCourseID", -1);

		if(back9SubCourseID==-10){
			eighteenHoleRound = false;
		}

		subCourses = new ArrayList<SubCourse>();

		subCourses.add(subCourseDAO.readSubCoursefromID(front9SubCourseID));
		if(eighteenHoleRound) {
			subCourses.add(subCourseDAO.readSubCoursefromID(back9SubCourseID));
		}

		courseID = subCourseDAO.readSubCoursefromID(front9SubCourseID).getCourseID();
        Course course = courseDAO.readCourseFromID(courseID);
        courseName = course.getName();

		int holecounter = 0;

        for (SubCourse subCourse : subCourses){

            List<CourseHole> courseHoles = courseHoleDAO.readListofCourseHoles(subCourse);

            for (CourseHole courseHole : courseHoles){

				holecounter++;

                List<CourseHoleInfo> courseHoleInfos = courseHoleInfoDAO.readListofCourseHoleInfos(courseHole);

                par[holecounter] = courseHole.getPar();
                blueYardage[holecounter] = courseHole.getBlueYardage();
                whiteYardage[holecounter] = courseHole.getWhiteYardage();
                redYardage[holecounter] = courseHole.getRedYardage();
                menHandicap[holecounter] = courseHole.getMenHandicap();
                womenHandicap[holecounter] = courseHole.getWomenHandicap();
				holeNumberText[holecounter] = courseHole.getHoleNumber();

                for (CourseHoleInfo courseHoleInfo : courseHoleInfos){

                    if (courseHoleInfo.getInfo().equals("Green Front")){
                        greenLocations[0][0][holecounter] = courseHoleInfo.getLatitude();
                        greenLocations[1][0][holecounter] = courseHoleInfo.getLongitude();
                    }else if (courseHoleInfo.getInfo().equals("Green Middle")) {
                        greenLocations[0][1][holecounter] = courseHoleInfo.getLatitude();
                        greenLocations[1][1][holecounter] = courseHoleInfo.getLongitude();
                    }else if (courseHoleInfo.getInfo().equals("Green Back")) {
                        greenLocations[0][2][holecounter] = courseHoleInfo.getLatitude();
                        greenLocations[1][2][holecounter] = courseHoleInfo.getLongitude();
                    }else if (courseHoleInfo.getInfo().equals("Tee")) {
                        teeLocations[0][holecounter] = courseHoleInfo.getLatitude();
                        teeLocations[1][holecounter] = courseHoleInfo.getLongitude();
                    }
                }
            }
        }
	}

	public void selectLayout(){
		if(eighteenHoleRound) {
			setContentView(R.layout.coursecatalogmainscreen);
		}
		else {
			setContentView(R.layout.coursecatalogmainscreen2);
		}
	}
	
	//Called when the front9 space is selected on the scorecard tab
	public void front9ButtonHandler(View view){	   	
		//Calls the methods that displays the values on the scorecard for the front 9
		frontActive = true;
		frontBackViewSwitcher();
    }
	
	//Called when the back9 space is selected on the scorecard tab
    public void back9ButtonHandler(View view) {
		if (eighteenHoleRound) {
			//Calls the methods that display the values on the scorecard for the back 9

			frontActive = false;
			frontBackViewSwitcher();
		}
	}
    
    //Loads the scorecard values for the front and back nines
    private void frontBackViewSwitcher(){
    	int nineHoles = 0;
    	TextView scorecardText;
    	RelativeLayout scorecardFrontBackButton;
    	
    	if(frontActive){
			if(eighteenHoleRound) {
				scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.catalogfront9Button);
    			scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
				scorecardFrontBackButton = (RelativeLayout) findViewById(R.id.catalogback9Button);
				scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
			}
    	}
    	else{
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.catalogfront9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.catalogback9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
    	}
    	
    	//Loads and displays the blue tee yardage row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Blue);
    		scorecardText.setText(Integer.toString(blueYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Blue);
    		scorecardText.setText(Integer.toString(blueYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Blue);
    		scorecardText.setText(Integer.toString(blueYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Blue);
    		scorecardText.setText(Integer.toString(blueYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Blue);
    		scorecardText.setText(Integer.toString(blueYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Blue);
    		scorecardText.setText(Integer.toString(blueYardage[6]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7Blue);
    		scorecardText.setText(Integer.toString(blueYardage[7]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8Blue);
    		scorecardText.setText(Integer.toString(blueYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Blue);
    		scorecardText.setText(Integer.toString(blueYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=blueYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Blue);
    		scorecardText.setText(Integer.toString(blueYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Blue);
    		scorecardText.setText(Integer.toString(blueYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Blue);
    		scorecardText.setText(Integer.toString(blueYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Blue);
    		scorecardText.setText(Integer.toString(blueYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Blue);
    		scorecardText.setText(Integer.toString(blueYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Blue);
    		scorecardText.setText(Integer.toString(blueYardage[15]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7Blue);
    		scorecardText.setText(Integer.toString(blueYardage[16]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8Blue);
    		scorecardText.setText(Integer.toString(blueYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Blue);
    		scorecardText.setText(Integer.toString(blueYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=blueYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.catalogtotalBlueText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the white tee yardage row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1White);
    		scorecardText.setText(Integer.toString(whiteYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2White);
    		scorecardText.setText(Integer.toString(whiteYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3White);
    		scorecardText.setText(Integer.toString(whiteYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4White);
    		scorecardText.setText(Integer.toString(whiteYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5White);
    		scorecardText.setText(Integer.toString(whiteYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6White);
    		scorecardText.setText(Integer.toString(whiteYardage[6]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7White);
    		scorecardText.setText(Integer.toString(whiteYardage[7]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8White);
    		scorecardText.setText(Integer.toString(whiteYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9White);
    		scorecardText.setText(Integer.toString(whiteYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=whiteYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1White);
    		scorecardText.setText(Integer.toString(whiteYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2White);
    		scorecardText.setText(Integer.toString(whiteYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3White);
    		scorecardText.setText(Integer.toString(whiteYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4White);
    		scorecardText.setText(Integer.toString(whiteYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5White);
    		scorecardText.setText(Integer.toString(whiteYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6White);
    		scorecardText.setText(Integer.toString(whiteYardage[15]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7White);
    		scorecardText.setText(Integer.toString(whiteYardage[16]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8White);
    		scorecardText.setText(Integer.toString(whiteYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9White);
    		scorecardText.setText(Integer.toString(whiteYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=whiteYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.catalogtotalWhiteText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the men's handicap row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[1]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[2]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[3]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[4]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[5]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[6]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[7]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[8]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[9]));
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[10]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[11]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[12]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[13]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[14]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[15]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[16]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[17]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[18]));
    	}
    	
    	//Loads and displays the par row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Par);
    		scorecardText.setText(Integer.toString(par[1]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Par);
    		scorecardText.setText(Integer.toString(par[2]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Par);
    		scorecardText.setText(Integer.toString(par[3]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Par);
    		scorecardText.setText(Integer.toString(par[4]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Par);
    		scorecardText.setText(Integer.toString(par[5]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Par);
    		scorecardText.setText(Integer.toString(par[6]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7Par);
    		scorecardText.setText(Integer.toString(par[7]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8Par);
    		scorecardText.setText(Integer.toString(par[8]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Par);
    		scorecardText.setText(Integer.toString(par[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=par[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Par);
    		scorecardText.setText(Integer.toString(par[10]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Par);
    		scorecardText.setText(Integer.toString(par[11]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Par);
    		scorecardText.setText(Integer.toString(par[12]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Par);
    		scorecardText.setText(Integer.toString(par[13]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Par);
    		scorecardText.setText(Integer.toString(par[14]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Par);
    		scorecardText.setText(Integer.toString(par[15]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7Par);
    		scorecardText.setText(Integer.toString(par[16]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8Par);
    		scorecardText.setText(Integer.toString(par[17]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Par);
    		scorecardText.setText(Integer.toString(par[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=par[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.catalogtotalParText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the red tee yardage row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Red);
    		scorecardText.setText(Integer.toString(redYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Red);
    		scorecardText.setText(Integer.toString(redYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Red);
    		scorecardText.setText(Integer.toString(redYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Red);
    		scorecardText.setText(Integer.toString(redYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Red);
    		scorecardText.setText(Integer.toString(redYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Red);
	    	scorecardText.setText(Integer.toString(redYardage[6]));
	    	scorecardText = (TextView)findViewById(R.id.cataloghole7Red);
	    	scorecardText.setText(Integer.toString(redYardage[7]));
	    	scorecardText = (TextView)findViewById(R.id.cataloghole8Red);
    		scorecardText.setText(Integer.toString(redYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Red);
    		scorecardText.setText(Integer.toString(redYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=redYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Red);
    		scorecardText.setText(Integer.toString(redYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2Red);
    		scorecardText.setText(Integer.toString(redYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3Red);
    		scorecardText.setText(Integer.toString(redYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4Red);
    		scorecardText.setText(Integer.toString(redYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5Red);
    		scorecardText.setText(Integer.toString(redYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6Red);
	    	scorecardText.setText(Integer.toString(redYardage[15]));
	    	scorecardText = (TextView)findViewById(R.id.cataloghole7Red);
	    	scorecardText.setText(Integer.toString(redYardage[16]));
	    	scorecardText = (TextView)findViewById(R.id.cataloghole8Red);
    		scorecardText.setText(Integer.toString(redYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9Red);
    		scorecardText.setText(Integer.toString(redYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=redYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.catalogtotalRedText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the women's handicap row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[1]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[2]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[3]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[4]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[5]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[6]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[7]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[8]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[9]));
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[10]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole2WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[11]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole3WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[12]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole4WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[13]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole5WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[14]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole6WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[15]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole7WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[16]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole8WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[17]));
    		scorecardText = (TextView)findViewById(R.id.cataloghole9WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[18]));
    	}
    	
    	//Loads and displays the top row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Text);
        	scorecardText.setText(Integer.toString(holeNumberText[1]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole2Text);
        	scorecardText.setText(Integer.toString(holeNumberText[2]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole3Text);
        	scorecardText.setText(Integer.toString(holeNumberText[3]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole4Text);
        	scorecardText.setText(Integer.toString(holeNumberText[4]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole5Text);
        	scorecardText.setText(Integer.toString(holeNumberText[5]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole6Text);
        	scorecardText.setText(Integer.toString(holeNumberText[6]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole7Text);
        	scorecardText.setText(Integer.toString(holeNumberText[7]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole8Text);
        	scorecardText.setText(Integer.toString(holeNumberText[8]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole9Text);
        	scorecardText.setText(Integer.toString(holeNumberText[9]));
    	}	
    	else{
    		scorecardText = (TextView)findViewById(R.id.cataloghole1Text);
        	scorecardText.setText(Integer.toString(holeNumberText[10]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole2Text);
        	scorecardText.setText(Integer.toString(holeNumberText[11]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole3Text);
        	scorecardText.setText(Integer.toString(holeNumberText[12]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole4Text);
        	scorecardText.setText(Integer.toString(holeNumberText[13]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole5Text);
        	scorecardText.setText(Integer.toString(holeNumberText[14]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole6Text);
        	scorecardText.setText(Integer.toString(holeNumberText[15]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole7Text);
        	scorecardText.setText(Integer.toString(holeNumberText[16]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole8Text);
        	scorecardText.setText(Integer.toString(holeNumberText[17]));
        	scorecardText = (TextView)findViewById(R.id.cataloghole9Text);
        	scorecardText.setText(Integer.toString(holeNumberText[18]));
    	}
    }
    
    //Initializes the scorecard tab with the course name
    private void scorecardInitializer() {  	
    	//Displays the course name in the top left corner
    	TextView scorecardText = (TextView)findViewById(R.id.catalogtopLeftCorner);
		if(eighteenHoleRound) {
			scorecardText.setText(courseName);
			scorecardText = (TextView)findViewById(R.id.catalogfront9ButtonText);
			scorecardText.setText(subCourses.get(0).getName());
			scorecardText = (TextView)findViewById(R.id.catalogback9ButtonText);
			scorecardText.setText(subCourses.get(1).getName());
		}
		else{
			scorecardText.setText(courseName + " - " + subCourses.get(0).getName());
		}
    	
    	//Loads the scorecard view for later use
    	scorecardTab = (RelativeLayout)findViewById(R.id.catalogtab1);
	}
    
    //Initializes the tab views
    private void tabSetup() {
    	tabHost = (TabHost)findViewById(R.id.catalogtabHost);
    	tabHost.setup();
    	
    	Resources res = getResources();
    	
    	TabSpec spec1;
    	TabSpec spec2;

    	//Initializes the scorecard tab
    	spec1=tabHost.newTabSpec("Tab 1");
    	spec1.setIndicator("Score Card", res.getDrawable(R.drawable.ic_tab3));
    	spec1.setContent(R.id.catalogtab1);
    	
    	//Initializes the map view tab
    	spec2=tabHost.newTabSpec("Tab 2");
    	spec2.setIndicator("Map", res.getDrawable(R.drawable.ic_tab2));
    	spec2.setContent(R.id.catalogtab2);
    	
    	tabHost.addTab(spec1);
    	tabHost.addTab(spec2);
    	
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	
    	//Handles when tabs are changed
    	tabHost.setOnTabChangedListener(new OnTabChangeListener() {
    	       @Override
    	       public void onTabChanged(String arg0) {
    	    	   	if(tabHost.getCurrentTab()==0){
    	    	   		//Changes the layout to landscape if the scorecard tab is selected
    	    	   		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	    	   	}
    	    	   	else{
    	    	   		//Changes the layout to portrait if any other tab is selected
	    	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	    	   	}	
    	       }     
    	 });  	
    }
    
    //Initializes the map spinner
	private void mapSpinnerSetup(){
		String [] items = null;
		if(eighteenHoleRound) {
			items = new String[18];
			for(int x = 1; x < 19; x++) {
				items[x-1] = "Hole " + Integer.toString(holeNumberText[x]);
			}
		}
		else{
			items = new String[9];
			for(int x = 1; x < 10; x++) {
				items[x-1] = "Hole " + Integer.toString(holeNumberText[x]);
			}
		}

    	Spinner spinner = (Spinner) findViewById(R.id.catalogSpinner);
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(CourseInfo.this, android.R.layout.simple_spinner_item, items);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	
    	//Sets the map location to the correct hole when the hole number is changed
    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    			setMapLocation(pos+1);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});    	
	}
	
	//Closes the activity and returns the display to the home screen if the back button is pressed
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	finish();
        	
        	return true;
        }
 	
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void onClick(View arg0) {
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	//Initializes the map view tab
	private void mapInitializer(){
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.catalogmap))
	        .getMap();
		
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		map.setOnMapClickListener(this);
	}
		
	//Sets the zoom and center of the map view
	//Runs when the map view tab is selected
	private void setMapLocation(int holeNumber){
		//Test values
		//greenLocations[0][2][1]=42.351185;
		//greenLocations[1][2][1]=-71.137167;
			
		map.clear();
		
		//Loads the tee location and the location of the middle of the green for the current hole
		lat[0] = (teeLocations[0][holeNumber]);
		lat[1] = (greenLocations[0][1][holeNumber]);
		lng[0] = (teeLocations[1][holeNumber]);
		lng[1] = (greenLocations[1][1][holeNumber]);
				
		//Loads the location of the middle of the green and displays the distance in yards from the current location
		greenLocation.setLatitude(lat[1]);
		greenLocation.setLongitude(lng[1]);
		playerLocation.setLatitude(lat[0]);
		playerLocation.setLongitude(lng[0]);
		    	
		middleDistance = (int)Math.round(greenLocation.distanceTo(playerLocation) * 1.09361);
		
		pinMarker = map.addMarker(new MarkerOptions()
	   		.position(new LatLng(lat[1],lng[1]))
	   		.title(Integer.toString(middleDistance)+" Yds")
	   		.snippet("Hole " + Integer.toString(holeNumberText[holeNumber]))
	   		.icon(BitmapDescriptorFactory
	   		.fromResource(R.drawable.pinmarker))
	   		.anchor((float)0.37, (float)1.0));
		
		pinMarker.showInfoWindow();
		
		playerMarker = map.addMarker(new MarkerOptions()
	       	.position(new LatLng(lat[0],lng[0]))
	       	.title("Tee")
	       	.snippet("Hole " + Integer.toString(holeNumberText[holeNumber]))
	       	.icon(BitmapDescriptorFactory
	        .fromResource(R.drawable.playermarker)));
	
		double lng1 = Math.toRadians(lng[0]);
		double lng2 = Math.toRadians(lng[1]);
		double lat1 = Math.toRadians(lat[0]);
		double lat2 = Math.toRadians(lat[1]);
		
		double dLon = (lng2-lng1);
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		float brng = (float)Math.toDegrees((Math.atan2(y, x)));
		brng = (brng + 360) % 360;
		
		LatLngBounds.Builder mapBuilder;
		LatLngBounds bounds;
		
		mapBuilder = new LatLngBounds.Builder();
		mapBuilder.include(pinMarker.getPosition());
		mapBuilder.include(playerMarker.getPosition());
		bounds = mapBuilder.build();
	   
		padding = 0; // offset from edges of the map in pixels
		cu = CameraUpdateFactory.newLatLngBounds(bounds, 
			this.getResources().getDisplayMetrics().widthPixels, 
			this.getResources().getDisplayMetrics().heightPixels-500, 
	        padding);
		
		// Move the camera instantly to location.
		map.moveCamera(cu);
		
		CameraPosition currentPlace = new CameraPosition.Builder()
		.target(map.getCameraPosition().target)
		.bearing(brng).tilt(0).zoom(map.getCameraPosition().zoom).build();
		map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
		
		map.addCircle(new CircleOptions()
	       .center(new LatLng(lat[1],lng[1]))
	       .radius(100*0.9144)
	       .strokeColor(Color.RED)
	       .strokeWidth(2));
	    
	    map.addCircle(new CircleOptions()
	       .center(new LatLng(lat[1],lng[1]))
	       .radius(150*0.9144)
	       .strokeColor(Color.WHITE)
	       .strokeWidth(2));
	    
	    map.addCircle(new CircleOptions()
	       .center(new LatLng(lat[1],lng[1]))
	       .radius(200*0.9144)
	       .strokeColor(Color.BLUE)
	       .strokeWidth(2));
		
	    map.addPolyline(new PolylineOptions()
	    	.add(new LatLng(lat[0],lng[0]))
	    	.add(new LatLng(lat[1],lng[1]))
	    	.color(Color.GRAY)
	    	.width(5));
	}
		
	@Override
	public void onMapClick(LatLng point){		
		if(mapClickMarker!=null){
			mapPolyline.remove();
			mapClickMarker.remove();
		}
		
		tapLocation.setLatitude(point.latitude);
		tapLocation.setLongitude(point.longitude);
		
		distance = (int)Math.round(tapLocation.distanceTo(playerLocation) * 1.09361);
		middleDistance = (int)Math.round(tapLocation.distanceTo(greenLocation) * 1.09361);
		
		mapClickMarker = map.addMarker(new MarkerOptions()
		.position(point)
		.title(distance +" yds away")
	   	.snippet(middleDistance + " yds to pin")
	   	.icon(BitmapDescriptorFactory
	    .fromResource(R.drawable.golfball)));
		
		mapClickMarker.showInfoWindow();
			
		mapPolyline = map.addPolyline(new PolylineOptions()
	    .add(new LatLng(lat[0],lng[0]))
	    .add(new LatLng(point.latitude, point.longitude))
	    .add(new LatLng(lat[1],lng[1]))
	    .color(Color.GRAY)
	    .width(5));
		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener(){
			public void onInfoWindowClick(Marker marker){
				marker.hideInfoWindow();
			}
		});
		
		map.setOnMarkerClickListener(new OnMarkerClickListener(){
			public boolean onMarkerClick(Marker marker){
				if(!marker.equals(mapClickMarker))
					marker.showInfoWindow();
					
				mapClickMarker.remove();
				mapPolyline.remove();
				
				return true;
			}
		});
	}
}