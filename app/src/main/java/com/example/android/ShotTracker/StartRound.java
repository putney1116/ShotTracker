package com.example.android.ShotTracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.CourseHoleDAO;
import com.example.android.ShotTracker.db.CourseHoleInfoDAO;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.db.RoundDAO;
import com.example.android.ShotTracker.db.RoundHoleDAO;
import com.example.android.ShotTracker.db.ShotDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.db.SubRoundDAO;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;
import com.example.android.ShotTracker.objects.SubCourse;
import com.example.android.ShotTracker.objects.SubRound;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StartRound extends com.google.android.maps.MapActivity implements OnClickListener, OnMapClickListener{
	
	private TabHost tabHost;
	private TextView scoreEntryGreen; 
	private TextView scoreEntryScorecard; 
	private TextView scorecardTotalText;
	private TextView scorecardPlusMinusText;
	private RelativeLayout scorecardTab;
	private RelativeLayout scorecardPlusButton;
	private RelativeLayout scorecardMinusButton;
	private TextView scorecardPlusButtonText;
	private TextView scorecardMinusButtonText;
	
	private int holeNumber = 1;
	private int playerNumber = 1;
	private int middleDistance;
	private int distance;	
	private boolean frontActive = true;
	private boolean buttonsVisible = false;
	private int holeScore[][] = new int[5][19];
	String playerName[] = {"","","","",""};
	
	View.OnTouchListener gestureListener;
	private GestureDetector swipeGesture;	
	
	protected LocationManager locationManager;
	protected Button retrieveLocationButton;
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	private LocationListener gpsClass;
	private Location greenLocation = new Location("");
	private Location playerLocation = new Location("");
	private Location tapLocation = new Location("");
	private Location location = new Location("");
	private ImageView gpsStatusPicture;
	
	double lat[] = new double[2];
	double lng[] = new double[2];
	
	private GoogleMap map;
	private Marker pinMarker;
	private Marker playerMarker;
	private Marker mapClickMarker;
	private Polyline mapPolyline;
	    
	private int padding = 0; // offset from edges of the map in pixels
	private CameraUpdate cu;
	
	private AlertDialog.Builder builder;
	
	private Vibrator vibe;
	
	private String courseName = "";
	private long courseID = -1;
	private int par[] = new int[19];
	private int blueYardage[] = new int[19];
	private int whiteYardage[] = new int[19];
	private int redYardage[] = new int[19];
	private int menHandicap[] = new int[19];
	private int womenHandicap[] = new int[19];
	private int holeNumberText[] = new int [19];
	private int numberOfPlayers = 0;
	
	//greenlocations[lat,long][front,middle,back][holenumber]
	private double greenLocations[][][] = new double[2][3][19];
	
	//teelocations[lat,long][holenumber]
	private double teeLocations[][] = new double[2][19];

    //DAOs
    private CourseDAO courseDAO = null;
    private SubCourseDAO subCourseDAO = null;
    private CourseHoleDAO courseHoleDAO = null;
    private CourseHoleInfoDAO courseHoleInfoDAO = null;
    private PlayerDAO playerDAO = null;
    private RoundDAO roundDAO = null;
    private SubRoundDAO subRoundDAO = null;
    private RoundHoleDAO roundHoleDAO = null;
    private ShotDAO shotDAO = null;

    private Course course = null;
    private List<SubCourse> subCourses = null;
    
	public void onCreate(Bundle savedInstanceState) {
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
	    super.onCreate(savedInstanceState);
    	setContentView(R.layout.inroundscreen);
    	
    	//Loads the player names and course information
    	loadCourseInfo();
    	
    	//Initializes the map tab
    	mapInitializer();
    	
    	//Builds the dialog that is displayed when the back button is pressed
    	buildDialog();
    	 
    	//Initializes the main screen plus minus and finish buttons
    	plusMinusFinishButtonHandler();
    	
    	//Initializes the scorecard plus and minus buttons
    	scorecardPlusMinusButtonInitializer();
    	
    	//Initializes the scorecard tab
    	scorecardInitializer();
    	
    	//Loads the front 9 values
    	frontBackViewSwitcher();
    	
    	//Initializes the swipe gesture variables
    	swipeGestureInitializer();
    	
    	//Initializes the gps module
    	gpsSetup();
    	
    	//Initializes the tab controller
    	tabSetup();
    }
	
	//Loads the player names and course information
	private void loadCourseInfo(){

        courseDAO = new CourseDAO(this);
        subCourseDAO = new SubCourseDAO(this);
        courseHoleDAO = new CourseHoleDAO(this);
        courseHoleInfoDAO = new CourseHoleInfoDAO(this);

		Intent myIntent = getIntent();
		
		//Loads the course name from the previous activity
        long front9SubCourseID = myIntent.getLongExtra("Front 9 SubCourseID", -1);
        long back9SubCourseID = myIntent.getLongExtra("Back 9 SubCourseID", -1);

        subCourses = new ArrayList<SubCourse>();

        subCourses.add(subCourseDAO.readSubCoursefromID(front9SubCourseID));
        subCourses.add(subCourseDAO.readSubCoursefromID(back9SubCourseID));
		
		numberOfPlayers = myIntent.getIntExtra("Players", 0);
		
		//Loads the player names from the previous activity
		for(int x=1;x<=numberOfPlayers;x++){
			playerName[x] = myIntent.getStringExtra("Player"+(x-1));
		}

        //Saves the official course name
        courseID = subCourseDAO.readSubCoursefromID(front9SubCourseID).getCourseID();
        course = courseDAO.readCourseFromID(courseID);
        courseName = course.getName();

        //\todo Need to make display of hole numbers dynamic based on subCourses

        int holecounter = 0;

        for (SubCourse subCourse : subCourses){
            List<CourseHole> courseHoles = courseHoleDAO.readListofCourseHoles(subCourse);

            for (CourseHole courseHole : courseHoles){

                holecounter++;

                List<CourseHoleInfo> courseHoleInfos = courseHoleInfoDAO.readListofCourseHoleInfos(courseHole);
                courseHole.setCourseHoleInfoList(courseHoleInfos);

                //\todo We should have a check to make sure the list of courseholes is in order 1-9 or 10-19 (ex). We currently assume they are.

                par[holecounter] = courseHole.getPar();
                blueYardage[holecounter] = courseHole.getBlueYardage();
                whiteYardage[holecounter] = courseHole.getWhiteYardage();
                redYardage[holecounter] = courseHole.getRedYardage();
                menHandicap[holecounter] = courseHole.getMenHandicap();
                womenHandicap[holecounter] = courseHole.getWomenHandicap();
				holeNumberText[holecounter] = courseHole.getHoleNumber();

                for(CourseHoleInfo courseHoleInfo : courseHoleInfos){

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

	//Called when the front9 space is selected on the scorecard tab
	public void front9ButtonHandler(View view){
		//Sets the vibrate time
		vibe.vibrate(15);
		
		//Sets the selected score view back to its default
		scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
	   	
		//Makes the plus minus buttons disappear
		scorecardPlusButton.setVisibility(View.GONE);
		scorecardMinusButton.setVisibility(View.GONE);
		scorecardPlusButtonText.setVisibility(View.GONE);
		scorecardMinusButtonText.setVisibility(View.GONE);
		
		//Declares that the scorecard is no longer being edited
		buttonsVisible = false;
		
		//Calls the methods that displays the values on the scorecard for the front 9
		frontActive = true;
		frontBackViewSwitcher();
		updateScorecard();
		
		//Calls the method that calculates the total score for each player
		for(int x=1;x<=numberOfPlayers;x++)
			updateScorecardTotals(x);
	}
    
	//Called when the back9 space is selected on the scorecard tab
    public void back9ButtonHandler(View view){		
    	//Sets the vibrate time
		vibe.vibrate(15);
    	
    	//Sets the selected score view back to its default
    	scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
	   	
    	//Makes the plus minus buttons disappear
		scorecardPlusButton.setVisibility(View.GONE);
		scorecardMinusButton.setVisibility(View.GONE);
		scorecardPlusButtonText.setVisibility(View.GONE);
		scorecardMinusButtonText.setVisibility(View.GONE);
		
		//Declares that the scorecard is no longer being edited
		buttonsVisible = false;
    	
		//Calls the methods that display the values on the scorecard for the back 9
    	frontActive = false;
		frontBackViewSwitcher();
		updateScorecard();
		
		//Calls the method that calculates the total score for each player
		for(int x=1;x<=numberOfPlayers;x++)
			updateScorecardTotals(x);
	}
    
    //Initializes the main screen plus minus and finish buttons
    private void plusMinusFinishButtonHandler() {
    	Button plusButton = (Button)findViewById(R.id.plusbutton);
    	Button minusButton = (Button)findViewById(R.id.minusbutton);
    	Button finishButton = (Button)findViewById(R.id.finishbutton);
    		
    	scoreEntryGreen = (TextView)findViewById(R.id.scoreentry);
    	
    	//Initialize Vibrate
    	vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    			
    	//Displays the course name on the main screen
    	TextView courseNameText = (TextView)findViewById(R.id.coursename);
    	courseNameText.setText(courseName);
    	
    	plusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					//Sets the vibrate time
					vibe.vibrate(15);
					
					//Increases the active player's score
					holeScore[playerNumber][holeNumber]++;
					
					//Displays the increased number
					scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));    
					
					//Loads the correct view from the scorecard tab
					setTextViewHoleNumber(playerNumber,holeNumber);
						
					//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					if(holeNumber<10){
						if(frontActive){
							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
							
							updateScorecardTotals(playerNumber);
						}
					}
					//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					else{
						if(!frontActive){
							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
							
							updateScorecardTotals(playerNumber);
						}
					}
					
				}catch(Exception e) {
				}
			}
    	});
    	
    	minusButton.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				try{
					//Sets the vibrate time
					vibe.vibrate(15);
					
					//Decreases the active player's score if the score is not already 0
					if(holeScore[playerNumber][holeNumber]!=0){
						holeScore[playerNumber][holeNumber]--;
						
						//Displays the decreased number
						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));  
						
						//Loads the correct view from the scorecard tab
						setTextViewHoleNumber(playerNumber,holeNumber);
						
						//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						if(holeNumber<10){
							if(frontActive){
								if(holeScore[playerNumber][holeNumber]==0)
				    				scoreEntryScorecard.setText("");
				    			else				
				    				scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
								
								updateScorecardTotals(playerNumber);
							}
						}
						//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						else{
							if(!frontActive){
								if(holeScore[playerNumber][holeNumber]==0)
				    				scoreEntryScorecard.setText("");
				    			else				
				    				scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
								
								updateScorecardTotals(playerNumber);
							}
						}
					}
				}catch(Exception e) {
				}
			}
    	});
    	
    	finishButton.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				try{
					//Sets the vibrate time
					vibe.vibrate(15);
					
					//Shows the save round confirmation dialog if the finish button is pressed
					builder.show();
				}
				catch(Exception e) {
				}
			}
    	});
    }
    
    //Updates the player score values on the scorecard tab
    private void updateScorecard(){
    	if(frontActive){   		
    		for(int x=1;x<=numberOfPlayers;x++){
    			for(int y=1;y<=9;y++){
    				//Loads the correct view from the scorecard tab
    				setTextViewHoleNumber(x,y);
		
    				//Displays the value on the scorecard if it is greater than 0
    				if(holeScore[x][y]==0)
    					scoreEntryScorecard.setText("");
    				else
    					scoreEntryScorecard.setText(Integer.toString(holeScore[x][y]));
    			}
    		}
    	}
    	else{
    		for(int x=1;x<=numberOfPlayers;x++){
    			for(int y=10;y<=18;y++){
    				//Loads the correct view from the scorecard tab
    				setTextViewHoleNumber(x,y);
		
    				//Displays the value on the scorecard if it is greater than 0
    				if(holeScore[x][y]==0)
    					scoreEntryScorecard.setText("");
    				else
    					scoreEntryScorecard.setText(Integer.toString(holeScore[x][y]));
    			}
    		}
    	}
    }
    
    //Updates the player score totals on the scorecard tab
    private void updateScorecardTotals(int playerNumber){
    	int nineHoles = 0, parTotal = 0, plusMinus = 0;
    	
    	//Calculates the total score and par if the score is greater than 0
    	if(frontActive){
			for(int x=1;x<=9;x++){
				if(holeScore[playerNumber][x]!=0)
					parTotal += par[x];
			
				nineHoles+=holeScore[playerNumber][x];
			}
		}
    	//Calculates the total score and par if the score is greater than 0
		else{
			for(int x=10;x<=18;x++){
				if(holeScore[playerNumber][x]!=0)
					parTotal += par[x];
				
				nineHoles+=holeScore[playerNumber][x];
			}
		}
    	
    	//Loads the correct view from the scorecard tab
    	setTextViewTotalPlayerNumber(playerNumber);
		
    	//The scorecard does not display 0 if there are no valid scores
		if(nineHoles==0){

			scorecardTotalText.setText("");
			scorecardPlusMinusText.setText("");
		}
		else{
			//Displays the total score on the scorecard
			scorecardTotalText.setText(Integer.toString(nineHoles));
			
			//Calculates the total score to par
			plusMinus = nineHoles - parTotal;
	
			//Displays the score to par on the scorecard as either red, "Even", or black
			if(plusMinus==0){
				scorecardPlusMinusText.setTextColor(Color.BLACK);
				scorecardPlusMinusText.setText("Even");
			}
			else
				if(plusMinus<0){
					scorecardPlusMinusText.setTextColor(Color.RED);
					scorecardPlusMinusText.setText(Integer.toString(plusMinus));
				}
				else{
					scorecardPlusMinusText.setTextColor(Color.BLACK);
					scorecardPlusMinusText.setText("+"+Integer.toString(plusMinus));
				}
		}
    }
    
    //Called when a score cell is selected in the scorecard tab
    private void playerScoreHandler(){    
    	//Sets the vibrate time
		vibe.vibrate(15);
		
    	//Resets the previously selected cell
    	scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
		
		//Loads the correct view from the scorecard tab
		setTextViewHoleNumber(playerNumber,holeNumber);
    	
		//Displays the edit score buttons
    	scorecardPlusButton.setVisibility(View.VISIBLE);
    	scorecardMinusButton.setVisibility(View.VISIBLE);
		scorecardPlusButtonText.setVisibility(View.VISIBLE);
		scorecardMinusButtonText.setVisibility(View.VISIBLE);
    	
    	//Highlights the selected cell
    	scoreEntryScorecard.setBackgroundColor(0xFFcccccc);
    	
    	//Declares that the scorecard is being edited
    	buttonsVisible = true;
    }
    
    //Initializes the increase score  and decrease score buttons
    private void scorecardPlusMinusButtonInitializer(){
    	//Initializes the plus and minus buttons
    	scorecardPlusButton = (RelativeLayout)findViewById(R.id.scorecardPlusButton);
    	scorecardMinusButton = (RelativeLayout)findViewById(R.id.scorecardMinusButton);
    	
    	scorecardPlusButtonText = (TextView)findViewById(R.id.scorecardPlusButtonText);
    	scorecardMinusButtonText = (TextView)findViewById(R.id.scorecardMinusButtonText);
    	
    	//Initializes the increase score button
    	scorecardPlusButton.setOnClickListener(new OnClickListener() {
    		@Override
			public void onClick(View v) {
				try{	
					//Sets the vibrate time
					vibe.vibrate(15);
					
					//Increases the score
					holeScore[playerNumber][holeNumber]++;
					
					//Displays the increased score
					scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
					
					//Updates the display on the scorecard
					updateScorecardTotals(playerNumber);
				}catch(Exception e) {
				}
			}
    	});
    	
    	//Initializes the decrease score button
    	scorecardMinusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					//Sets the vibrate time
					vibe.vibrate(15);
					
					//Decreases the score if it is greater than 0
					if(holeScore[playerNumber][holeNumber]!=0){
						holeScore[playerNumber][holeNumber]--;
							
						//Displays the decreased score
						if(holeScore[playerNumber][holeNumber]==0)
							scoreEntryScorecard.setText("");
						else				
							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
						
						//Updates the display on the scorecard
						updateScorecardTotals(playerNumber);
					}
				}catch(Exception e) {
				}
			}
    	});
    }
    
    //Called when the player 1 hole 1 cell is selected
    public void player1Hole1ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 1;
	    	else
	    		holeNumber = 10;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 1 cell is selected
    public void player2Hole1ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 1;
	    	else
	    		holeNumber = 10;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 1 cell is selected
    public void player3Hole1ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 1;
	    	else
	    		holeNumber = 10;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 1 cell is selected
    public void player4Hole1ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 1;
	    	else
	    		holeNumber = 10;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 2 cell is selected
    public void player1Hole2ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 2;
	    	else
	    		holeNumber = 11;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
  
    //Called when the player 2 hole 2 cell is selected
    public void player2Hole2ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 2;
	    	else
	    		holeNumber = 11;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 2 cell is selected
    public void player3Hole2ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 2;
	    	else
	    		holeNumber = 11;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 2 cell is selected
    public void player4Hole2ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 2;
	    	else
	    		holeNumber = 11;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 3 cell is selected
    public void player1Hole3ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 3;
	    	else
	    		holeNumber = 12;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 3 cell is selected
    public void player2Hole3ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 3;
	    	else
	    		holeNumber = 12;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }

    //Called when the player 3 hole 3 cell is selected
    public void player3Hole3ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 3;
	    	else
	    		holeNumber = 12;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 3 cell is selected
    public void player4Hole3ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 3;
	    	else
	    		holeNumber = 12;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 4 cell is selected
    public void player1Hole4ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 4;
	    	else
	    		holeNumber = 13;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 4 cell is selected
    public void player2Hole4ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 4;
	    	else
	    		holeNumber = 13;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 4 cell is selected
    public void player3Hole4ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 4;
	    	else
	    		holeNumber = 13;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 4 cell is selected
    public void player4Hole4ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 4;
	    	else
	    		holeNumber = 13;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 5 cell is selected
    public void player1Hole5ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 5;
	    	else
	    		holeNumber = 14;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 5 cell is selected
    public void player2Hole5ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 5;
	    	else
	    		holeNumber = 14;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 5 cell is selected
    public void player3Hole5ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 5;
	    	else
	    		holeNumber = 14;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 5 cell is selected
    public void player4Hole5ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 5;
	    	else
	    		holeNumber = 14;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 6 cell is selected
    public void player1Hole6ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 6;
	    	else
	    		holeNumber = 15;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 6 cell is selected
    public void player2Hole6ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 6;
	    	else
	    		holeNumber = 15;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 6 cell is selected
    public void player3Hole6ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 6;
	    	else
	    		holeNumber = 15;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 6 cell is selected
    public void player4Hole6ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 6;
	    	else
	    		holeNumber = 15;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
      	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 7 cell is selected
    public void player1Hole7ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 7;
	    	else
	    		holeNumber = 16;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 7 cell is selected
    public void player2Hole7ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 7;
	    	else
	    		holeNumber = 16;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 7 cell is selected
    public void player3Hole7ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 7;
	    	else
	    		holeNumber = 16;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 7 cell is selected
    public void player4Hole7ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 7;
	    	else
	    		holeNumber = 16;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 8 cell is selected
    public void player1Hole8ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 8;
	    	else
	    		holeNumber = 17;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 8 cell is selected
    public void player2Hole8ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;
    	
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 8;
	    	else
	    		holeNumber = 17;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 8 cell is selected
    public void player3Hole8ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;

    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 8;
	    	else
	    		holeNumber = 17;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 8 cell is selected
    public void player4Hole8ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;

    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 8;
	    	else
	    		holeNumber = 17;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 1 hole 9 cell is selected
    public void player1Hole9ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=1){
    		//Sets the current player number
    		playerNumber = 1;
   
    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 9;
	    	else
	    		holeNumber = 18;
    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 2 hole 9 cell is selected
    public void player2Hole9ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=2){
    		//Sets the current player number
    		playerNumber = 2;

    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 9;
	    	else
	    		holeNumber = 18;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 3 hole 9 cell is selected
    public void player3Hole9ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=3){
    		//Sets the current player number
    		playerNumber = 3;

    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 9;
	    	else
	    		holeNumber = 18;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Called when the player 4 hole 9 cell is selected
    public void player4Hole9ScoreHandler(View view){
    	//Checks if this is an active player's cell
    	if(numberOfPlayers>=4){
    		//Sets the current player number
    		playerNumber = 4;

    		//Sets the current hole number
	    	if(frontActive)
	    		holeNumber = 9;
	    	else
	    		holeNumber = 18;
	    	
	    	//Runs the generic handler with the current player and hole number
	    	playerScoreHandler();
    	}
    	//If the cell is not for an active player, the handler for blank spaces is called
    	else{
    		blankSpaceHandler(view);
    	}
    }
    
    //Run when the edit score buttons are deactivated
    public void blankSpaceHandler(View view){
		//Sets the selected score view back to its default
		scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
	   	
		//Makes the plus minus buttons disappear
		scorecardPlusButton.setVisibility(View.GONE);
		scorecardMinusButton.setVisibility(View.GONE);
		scorecardPlusButtonText.setVisibility(View.GONE);
		scorecardMinusButtonText.setVisibility(View.GONE);
		
		//Declares that the scorecard is no longer being edited
		buttonsVisible = false;
    }
    
    //Loads the total and plus minus view from the scorecard tab for a player
    private void setTextViewTotalPlayerNumber(int playerNumber){
    	switch(playerNumber){
    		case 1:
    			scorecardTotalText = (TextView)findViewById(R.id.totalPlayer1Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.plusMinusPlayer1Score);
    			break;
    		case 2:
    			scorecardTotalText = (TextView)findViewById(R.id.totalPlayer2Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.plusMinusPlayer2Score);
    			break;
    		case 3:
    			scorecardTotalText = (TextView)findViewById(R.id.totalPlayer3Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.plusMinusPlayer3Score);
    			break;
    		case 4:
    			scorecardTotalText = (TextView)findViewById(R.id.totalPlayer4Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.plusMinusPlayer4Score);
    			break;
    		default:
    			break;
    	}
    }
    
    //Loads the views for the cell, edit score buttons, and views above and below
    private void setTextViewHoleNumber(int player, int hole) {
    	switch(player){
    		case 1:
    			switch(hole) {
    				case 1:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole1Score);
    					break;
    				case 2:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole2Score); 
    					break;
    				case 3:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole3Score); 
    					break;
    				case 4:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole4Score); 
    					break;
    				case 5:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole5Score); 
    					break;
    				case 6:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole6Score); 
    					break;
    				case 7:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole7Score); 
    					break;
    				case 8:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole8Score); 
    					break;
    				case 9:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole9Score); 
    					break;
    				case 10:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole1Score); 
    					break;
    				case 11:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole2Score); 
    					break;
    				case 12:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole3Score); 
    					break;
    				case 13:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole4Score); 
    					break;
    				case 14:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole5Score); 
    					break;
    				case 15:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole6Score); 
    					break;
    				case 16:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole7Score); 
    					break;
    				case 17:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole8Score); 
    					break;
    				case 18:
    					scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole9Score); 
    					break;
    				default:
    					break;
    			}
    			break;
    		case 2:
        		switch(hole) {
        			case 1:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole1Score); 
        				break;
        			case 2:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole2Score); 
        				break;
        			case 3:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole3Score); 
        				break;
        			case 4:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole4Score); 
        				break;
        			case 5:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole5Score); 
        				break;
        			case 6:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole6Score); 
        				break;
        			case 7:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole7Score); 
        				break;
        			case 8:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole8Score); 
        				break;
        			case 9:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole9Score); 
        				break;
        			case 10:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole1Score); 
        				break;
        			case 11:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole2Score); 
        				break;
        			case 12:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole3Score); 
        				break;
        			case 13:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole4Score); 
        				break;
        			case 14:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole5Score); 
        				break;
        			case 15:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole6Score); 
        				break;
        			case 16:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole7Score); 
        				break;
        			case 17:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole8Score); 
        				break;
        			case 18:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player2Hole9Score); 
        				break;
        			default:
        				break;
        		}
        		break;
        	case 3:
            	switch(hole) {
        			case 1:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole1Score); 
        				break;
        			case 2:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole2Score); 
        				break;
        			case 3:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole3Score); 
        				break;
        			case 4:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole4Score); 
        				break;
        			case 5:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole5Score); 
        				break;
        			case 6:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole6Score); 
        				break;
        			case 7:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole7Score); 
        				break;
        			case 8:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole8Score); 
        				break;
        			case 9:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole9Score); 
        				break;
        			case 10:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole1Score); 
        				break;
        			case 11:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole2Score); 
        				break;
        			case 12:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole3Score); 
        				break;
        			case 13:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole4Score); 
        				break;
        			case 14:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole5Score); 
        				break;
        			case 15:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole6Score); 
        				break;
        			case 16:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole7Score); 
        				break;
        			case 17:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole8Score); 
        				break;
        			case 18:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player3Hole9Score); 
        				break;
        			default:
        				break;
            	}
        		break;
        	case 4:
            	switch(hole) {
            		case 1:
            			scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole1Score); 
            			break;
            		case 2:
            			scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole2Score); 
            			break;
        			case 3:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole3Score); 
        				break;
        			case 4:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole4Score); 
        				break;
        			case 5:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole5Score); 
        				break;
        			case 6:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole6Score); 
        				break;
        			case 7:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole7Score); 
        				break;
        			case 8:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole8Score); 
        				break;
        			case 9:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole9Score); 
        				break;
        			case 10:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole1Score); 
        				break;
        			case 11:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole2Score); 
        				break;
        			case 12:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole3Score); 
        				break;
        			case 13:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole4Score); 
        				break;
        			case 14:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole5Score);
        				break;
        			case 15:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole6Score); 
        				break;
        			case 16:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole7Score); 
        				break;
        			case 17:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole8Score); 
        				break;
        			case 18:
        				scoreEntryScorecard = (TextView)findViewById(R.id.player4Hole9Score); 
        				break;
        			default:
        				break;
        		}
        		break;
        	default:
        		break;
    	}
    }
    
    //Loads the scorecard values for the front and back nines
    private void frontBackViewSwitcher(){
    	int nineHoles = 0;
    	TextView scorecardText;
    	RelativeLayout scorecardFrontBackButton;
    	
    	if(frontActive){
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.front9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.back9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
    	}
    	else{
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.front9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.back9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
    	}
    	
    	//Loads and displays the blue tee yardage row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1Blue);
    		scorecardText.setText(Integer.toString(blueYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.hole2Blue);
    		scorecardText.setText(Integer.toString(blueYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.hole3Blue);
    		scorecardText.setText(Integer.toString(blueYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.hole4Blue);
    		scorecardText.setText(Integer.toString(blueYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.hole5Blue);
    		scorecardText.setText(Integer.toString(blueYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.hole6Blue);
    		scorecardText.setText(Integer.toString(blueYardage[6]));
    		scorecardText = (TextView)findViewById(R.id.hole7Blue);
    		scorecardText.setText(Integer.toString(blueYardage[7]));
    		scorecardText = (TextView)findViewById(R.id.hole8Blue);
    		scorecardText.setText(Integer.toString(blueYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.hole9Blue);
    		scorecardText.setText(Integer.toString(blueYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=blueYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1Blue);
    		scorecardText.setText(Integer.toString(blueYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.hole2Blue);
    		scorecardText.setText(Integer.toString(blueYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.hole3Blue);
    		scorecardText.setText(Integer.toString(blueYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.hole4Blue);
    		scorecardText.setText(Integer.toString(blueYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.hole5Blue);
    		scorecardText.setText(Integer.toString(blueYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.hole6Blue);
    		scorecardText.setText(Integer.toString(blueYardage[15]));
    		scorecardText = (TextView)findViewById(R.id.hole7Blue);
    		scorecardText.setText(Integer.toString(blueYardage[16]));
    		scorecardText = (TextView)findViewById(R.id.hole8Blue);
    		scorecardText.setText(Integer.toString(blueYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.hole9Blue);
    		scorecardText.setText(Integer.toString(blueYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=blueYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.totalBlueText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the white tee yardage row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1White);
    		scorecardText.setText(Integer.toString(whiteYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.hole2White);
    		scorecardText.setText(Integer.toString(whiteYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.hole3White);
    		scorecardText.setText(Integer.toString(whiteYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.hole4White);
    		scorecardText.setText(Integer.toString(whiteYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.hole5White);
    		scorecardText.setText(Integer.toString(whiteYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.hole6White);
    		scorecardText.setText(Integer.toString(whiteYardage[6]));
    		scorecardText = (TextView)findViewById(R.id.hole7White);
    		scorecardText.setText(Integer.toString(whiteYardage[7]));
    		scorecardText = (TextView)findViewById(R.id.hole8White);
    		scorecardText.setText(Integer.toString(whiteYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.hole9White);
    		scorecardText.setText(Integer.toString(whiteYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=whiteYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1White);
    		scorecardText.setText(Integer.toString(whiteYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.hole2White);
    		scorecardText.setText(Integer.toString(whiteYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.hole3White);
    		scorecardText.setText(Integer.toString(whiteYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.hole4White);
    		scorecardText.setText(Integer.toString(whiteYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.hole5White);
    		scorecardText.setText(Integer.toString(whiteYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.hole6White);
    		scorecardText.setText(Integer.toString(whiteYardage[15]));
    		scorecardText = (TextView)findViewById(R.id.hole7White);
    		scorecardText.setText(Integer.toString(whiteYardage[16]));
    		scorecardText = (TextView)findViewById(R.id.hole8White);
    		scorecardText.setText(Integer.toString(whiteYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.hole9White);
    		scorecardText.setText(Integer.toString(whiteYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=whiteYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.totalWhiteText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the men's handicap row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[1]));
    		scorecardText = (TextView)findViewById(R.id.hole2Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[2]));
    		scorecardText = (TextView)findViewById(R.id.hole3Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[3]));
    		scorecardText = (TextView)findViewById(R.id.hole4Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[4]));
    		scorecardText = (TextView)findViewById(R.id.hole5Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[5]));
    		scorecardText = (TextView)findViewById(R.id.hole6Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[6]));
    		scorecardText = (TextView)findViewById(R.id.hole7Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[7]));
    		scorecardText = (TextView)findViewById(R.id.hole8Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[8]));
    		scorecardText = (TextView)findViewById(R.id.hole9Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[9]));
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[10]));
    		scorecardText = (TextView)findViewById(R.id.hole2Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[11]));
    		scorecardText = (TextView)findViewById(R.id.hole3Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[12]));
    		scorecardText = (TextView)findViewById(R.id.hole4Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[13]));
    		scorecardText = (TextView)findViewById(R.id.hole5Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[14]));
    		scorecardText = (TextView)findViewById(R.id.hole6Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[15]));
    		scorecardText = (TextView)findViewById(R.id.hole7Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[16]));
    		scorecardText = (TextView)findViewById(R.id.hole8Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[17]));
    		scorecardText = (TextView)findViewById(R.id.hole9Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[18]));
    	}
    	
    	//Loads and displays the par row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1Par);
    		scorecardText.setText(Integer.toString(par[1]));
    		scorecardText = (TextView)findViewById(R.id.hole2Par);
    		scorecardText.setText(Integer.toString(par[2]));
    		scorecardText = (TextView)findViewById(R.id.hole3Par);
    		scorecardText.setText(Integer.toString(par[3]));
    		scorecardText = (TextView)findViewById(R.id.hole4Par);
    		scorecardText.setText(Integer.toString(par[4]));
    		scorecardText = (TextView)findViewById(R.id.hole5Par);
    		scorecardText.setText(Integer.toString(par[5]));
    		scorecardText = (TextView)findViewById(R.id.hole6Par);
    		scorecardText.setText(Integer.toString(par[6]));
    		scorecardText = (TextView)findViewById(R.id.hole7Par);
    		scorecardText.setText(Integer.toString(par[7]));
    		scorecardText = (TextView)findViewById(R.id.hole8Par);
    		scorecardText.setText(Integer.toString(par[8]));
    		scorecardText = (TextView)findViewById(R.id.hole9Par);
    		scorecardText.setText(Integer.toString(par[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=par[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1Par);
    		scorecardText.setText(Integer.toString(par[10]));
    		scorecardText = (TextView)findViewById(R.id.hole2Par);
    		scorecardText.setText(Integer.toString(par[11]));
    		scorecardText = (TextView)findViewById(R.id.hole3Par);
    		scorecardText.setText(Integer.toString(par[12]));
    		scorecardText = (TextView)findViewById(R.id.hole4Par);
    		scorecardText.setText(Integer.toString(par[13]));
    		scorecardText = (TextView)findViewById(R.id.hole5Par);
    		scorecardText.setText(Integer.toString(par[14]));
    		scorecardText = (TextView)findViewById(R.id.hole6Par);
    		scorecardText.setText(Integer.toString(par[15]));
    		scorecardText = (TextView)findViewById(R.id.hole7Par);
    		scorecardText.setText(Integer.toString(par[16]));
    		scorecardText = (TextView)findViewById(R.id.hole8Par);
    		scorecardText.setText(Integer.toString(par[17]));
    		scorecardText = (TextView)findViewById(R.id.hole9Par);
    		scorecardText.setText(Integer.toString(par[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=par[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.totalParText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the red tee yardage row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1Red);
    		scorecardText.setText(Integer.toString(redYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.hole2Red);
    		scorecardText.setText(Integer.toString(redYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.hole3Red);
    		scorecardText.setText(Integer.toString(redYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.hole4Red);
    		scorecardText.setText(Integer.toString(redYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.hole5Red);
    		scorecardText.setText(Integer.toString(redYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.hole6Red);
	    	scorecardText.setText(Integer.toString(redYardage[6]));
	    	scorecardText = (TextView)findViewById(R.id.hole7Red);
	    	scorecardText.setText(Integer.toString(redYardage[7]));
	    	scorecardText = (TextView)findViewById(R.id.hole8Red);
    		scorecardText.setText(Integer.toString(redYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.hole9Red);
    		scorecardText.setText(Integer.toString(redYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=redYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1Red);
    		scorecardText.setText(Integer.toString(redYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.hole2Red);
    		scorecardText.setText(Integer.toString(redYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.hole3Red);
    		scorecardText.setText(Integer.toString(redYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.hole4Red);
    		scorecardText.setText(Integer.toString(redYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.hole5Red);
    		scorecardText.setText(Integer.toString(redYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.hole6Red);
	    	scorecardText.setText(Integer.toString(redYardage[15]));
	    	scorecardText = (TextView)findViewById(R.id.hole7Red);
	    	scorecardText.setText(Integer.toString(redYardage[16]));
	    	scorecardText = (TextView)findViewById(R.id.hole8Red);
    		scorecardText.setText(Integer.toString(redYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.hole9Red);
    		scorecardText.setText(Integer.toString(redYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=redYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.totalRedText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the women's handicap row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[1]));
    		scorecardText = (TextView)findViewById(R.id.hole2WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[2]));
    		scorecardText = (TextView)findViewById(R.id.hole3WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[3]));
    		scorecardText = (TextView)findViewById(R.id.hole4WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[4]));
    		scorecardText = (TextView)findViewById(R.id.hole5WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[5]));
    		scorecardText = (TextView)findViewById(R.id.hole6WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[6]));
    		scorecardText = (TextView)findViewById(R.id.hole7WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[7]));
    		scorecardText = (TextView)findViewById(R.id.hole8WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[8]));
    		scorecardText = (TextView)findViewById(R.id.hole9WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[9]));
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[10]));
    		scorecardText = (TextView)findViewById(R.id.hole2WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[11]));
    		scorecardText = (TextView)findViewById(R.id.hole3WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[12]));
    		scorecardText = (TextView)findViewById(R.id.hole4WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[13]));
    		scorecardText = (TextView)findViewById(R.id.hole5WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[14]));
    		scorecardText = (TextView)findViewById(R.id.hole6WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[15]));
    		scorecardText = (TextView)findViewById(R.id.hole7WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[16]));
    		scorecardText = (TextView)findViewById(R.id.hole8WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[17]));
    		scorecardText = (TextView)findViewById(R.id.hole9WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[18]));
    	}
    	
    	//Loads and displays the top row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.hole1Text);
        	scorecardText.setText(Integer.toString(holeNumberText[1]));
        	scorecardText = (TextView)findViewById(R.id.hole2Text);
        	scorecardText.setText(Integer.toString(holeNumberText[2]));
        	scorecardText = (TextView)findViewById(R.id.hole3Text);
        	scorecardText.setText(Integer.toString(holeNumberText[3]));
        	scorecardText = (TextView)findViewById(R.id.hole4Text);
        	scorecardText.setText(Integer.toString(holeNumberText[4]));
        	scorecardText = (TextView)findViewById(R.id.hole5Text);
        	scorecardText.setText(Integer.toString(holeNumberText[5]));
        	scorecardText = (TextView)findViewById(R.id.hole6Text);
        	scorecardText.setText(Integer.toString(holeNumberText[6]));
        	scorecardText = (TextView)findViewById(R.id.hole7Text);
        	scorecardText.setText(Integer.toString(holeNumberText[7]));
        	scorecardText = (TextView)findViewById(R.id.hole8Text);
        	scorecardText.setText(Integer.toString(holeNumberText[8]));
        	scorecardText = (TextView)findViewById(R.id.hole9Text);
        	scorecardText.setText(Integer.toString(holeNumberText[9]));
    	}	
    	else{
    		scorecardText = (TextView)findViewById(R.id.hole1Text);
        	scorecardText.setText(Integer.toString(holeNumberText[10]));
        	scorecardText = (TextView)findViewById(R.id.hole2Text);
        	scorecardText.setText(Integer.toString(holeNumberText[11]));
        	scorecardText = (TextView)findViewById(R.id.hole3Text);
        	scorecardText.setText(Integer.toString(holeNumberText[12]));
        	scorecardText = (TextView)findViewById(R.id.hole4Text);
        	scorecardText.setText(Integer.toString(holeNumberText[13]));
        	scorecardText = (TextView)findViewById(R.id.hole5Text);
        	scorecardText.setText(Integer.toString(holeNumberText[14]));
        	scorecardText = (TextView)findViewById(R.id.hole6Text);
        	scorecardText.setText(Integer.toString(holeNumberText[15]));
        	scorecardText = (TextView)findViewById(R.id.hole7Text);
        	scorecardText.setText(Integer.toString(holeNumberText[16]));
        	scorecardText = (TextView)findViewById(R.id.hole8Text);
        	scorecardText.setText(Integer.toString(holeNumberText[17]));
        	scorecardText = (TextView)findViewById(R.id.hole9Text);
        	scorecardText.setText(Integer.toString(holeNumberText[18]));
    	}
    }
    
    //Initializes the scorecard tab with the course name and players names
    private void scorecardInitializer() {  
    	//Displays the course name in the top left corner
    	TextView scorecardText = (TextView)findViewById(R.id.topLeftCorner);
    	scorecardText.setText(courseName);
    	
    	//Displays the player names
    	scorecardText = (TextView)findViewById(R.id.player1Name);
    	scorecardText.setText(playerName[1]);
    	scorecardText = (TextView)findViewById(R.id.player2Name);
    	scorecardText.setText(playerName[2]);
    	scorecardText = (TextView)findViewById(R.id.player3Name);
    	scorecardText.setText(playerName[3]);
    	scorecardText = (TextView)findViewById(R.id.player4Name);
    	scorecardText.setText(playerName[4]);
    	
    	//Loads the scorecard view for later use
    	scorecardTab = (RelativeLayout)findViewById(R.id.tab3);
    	
    	//setLayoutSize();
    	
    	//Initializes the scorecard views
    	scoreEntryScorecard = (TextView)findViewById(R.id.player1Hole1Score);
	}
    
    //Initializes the swipe gestures
    private void swipeGestureInitializer(){
    	swipeGesture = new GestureDetector(this.getApplicationContext(),new MyGestureDetector());
    	gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return swipeGesture.onTouchEvent(event);
            }
        };
        
        RelativeLayout scorecardButtons = null;
        
        //Initializes the scorecard views so that the swipe gesture is detected over them
        scorecardButtons = (RelativeLayout)findViewById(R.id.front9Button);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.back9Button);
        scorecardButtons.setOnTouchListener(gestureListener);
        
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceTopLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceTopLevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlusButtonLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlusButtonLevelMiddle);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlusButtonLevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceMinusButtonLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceMinusButtonLevelMiddle);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceMinusButtonLevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer1LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer1LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer1LevelFarRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer2LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer2LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer2LevelFarRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer3LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer3LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer3LevelFarRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer4LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer4LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer4LevelFarRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceBottomLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceBottomLevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceLeft1);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.front9Button);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceLeft2);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.back9Button);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceBottom);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceLeft3);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceLeft4);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceBottomText);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceTopLevel2);
        scorecardButtons.setOnTouchListener(gestureListener);
        
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole1ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole2ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole3ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole4ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole5ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole6ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole7ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole8ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player1Hole9ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole1ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole2ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole3ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole4ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole5ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole6ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole7ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole8ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player2Hole9ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole1ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole2ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole3ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole4ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole5ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole6ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole7ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole8ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player3Hole9ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole1ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole2ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole3ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole4ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole5ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole6ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole7ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole8ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.player4Hole9ScoreButton);
        scorecardButtons.setOnTouchListener(gestureListener);
    }
    
    //Initializes the tab views
    private void tabSetup() {
    	//Loads and displays the text for the first hole, par, and first player's name
    	final TextView holeNumberTextView = (TextView)findViewById(R.id.holenumber);
    	final TextView parText = (TextView)findViewById(R.id.parnumber);
    	final TextView playerNameText = (TextView)findViewById(R.id.playername);
    	
    	holeNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    	parText.setText("Par " + par[holeNumber]);
    	playerNameText.setText(playerName[playerNumber]);
    	
    	tabHost = (TabHost)findViewById(R.id.tabHost);
    	tabHost.setup();
    	
    	Resources res = getResources();
    	
    	TabSpec spec1;
    	TabSpec spec2;
    	TabSpec spec3;
    	TabSpec spec4;

    	//Initializes the main screen tab
    	spec1=tabHost.newTabSpec("Tab 1");
    	spec1.setIndicator("Green", res.getDrawable(R.drawable.ic_tab1));
    	spec1.setContent(R.id.tab1);

    	//Initializes the map view tab
    	spec2=tabHost.newTabSpec("Tab 2");
    	spec2.setIndicator("Map", res.getDrawable(R.drawable.ic_tab2));
    	spec2.setContent(R.id.tab2);

    	//Initializes the scorecard tab
    	spec3=tabHost.newTabSpec("Tab 3");
    	spec3.setIndicator("Score Card", res.getDrawable(R.drawable.ic_tab3));
    	spec3.setContent(R.id.tab3);
    	
    	//Initializes the caddy tab
    	spec4=tabHost.newTabSpec("Tab 4");
    	spec4.setIndicator("Caddy", res.getDrawable(R.drawable.ic_tab4));
    	spec4.setContent(R.id.tab4);
    	
    	tabHost.addTab(spec1);
    	tabHost.addTab(spec2);
    	tabHost.addTab(spec3);
    	tabHost.addTab(spec4);
    	
    	//Handles when tabs are changed
    	tabHost.setOnTabChangedListener(new OnTabChangeListener() {
    	       @Override
    	       public void onTabChanged(String arg0) {
    	            //Sets the vibrate time
    	    	    vibe.vibrate(15);
    				
    	    	    if(tabHost.getCurrentTab()==2){
    	    	   		//Changes the layout to landscape if the scorecard tab is selected
    	    	   		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	    	   	}
    	    	   	else{
    	    	   		//Changes the layout to portrait if any other tab is selected
    	    	   		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    	        
	    	   			if(tabHost.getCurrentTab()==0){
	    	   				//Displays the current hole number, par, player name, and score if the main screen tab is selected
	    	   				scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
	    	   				holeNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
	    	   				parText.setText("Par " + par[holeNumber]);
    						playerNameText.setText(playerName[playerNumber]);
    						
    						//Gets the location and calls the method that calculates the distance to the green
	    	   				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    						if (location != null)
    							gpsClass.onLocationChanged(location);
	    	   			}
	    	   			else{
	    	   				if(tabHost.getCurrentTab()==1){
	    	   					//Sets the maps location if the map view tab is selected
	    	   					setMapLocation();
	    	   				}
	    	   			}
    	    	   	}	
    	    	   	
	    	   		//Sets the selected score view back to its default
	    			scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
	    		   	
	    			//Makes the plus minus buttons disappear
	    			scorecardPlusButton.setVisibility(View.GONE);
	    			scorecardMinusButton.setVisibility(View.GONE);
					scorecardPlusButtonText.setVisibility(View.GONE);
					scorecardMinusButtonText.setVisibility(View.GONE);
	    			
	    			//Declares that the scorecard is no longer being edited
	    			buttonsVisible = false;
    	       }     
    	 });  	
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event){
    	return swipeGesture.onTouchEvent(event);
    }
 
    //Handles the swipes
    class MyGestureDetector extends SimpleOnGestureListener{
    	
    	private static final int SWIPE_MIN_DISTANCE = 100;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 20;
        
    	private TextView greenHoleNumberTextView = (TextView)findViewById(R.id.holenumber);
    	private TextView greenParText = (TextView)findViewById(R.id.parnumber);
    	private TextView greenPlayerNameText = (TextView)findViewById(R.id.playername);
    	 
    	@Override
    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){

    		//Handles swipes for the main screen tab
    		if(tabHost.getCurrentTab()==0){
    			try {
    				//Right to left swipe
    				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    					//Sets the vibrate time
    					vibe.vibrate(15);
    					
    					//Only runs if the current hole is not the last hole
    					if(holeNumber==18)
    						return false;
    					else {
    						//Increases the current hole and displays the increase
    						holeNumber++;
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						
    						//Updates the scorecard display if the increase goes from the front to the back
    						if(holeNumber==10){
    							frontActive = false;
    							frontBackViewSwitcher();
    							updateScorecard();
    							
    							for(int x=1;x<=numberOfPlayers;x++)
    								updateScorecardTotals(x);
    						}
    						
    						//Loads the current hole's score
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
    						
    						//Gets the location and calls the method that calculates the distance to the green
    						Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    						if (location != null)
    							gpsClass.onLocationChanged(location);
    					}
    				}
    				
    				//Left to right swipe
    				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    					//Sets the vibrate time
    					vibe.vibrate(15);
    					
    					//Only runs if the current hole is not the first hole
    					if(holeNumber==1)
    						return false;
    					else {
    						//Decreases the current hole and displays the decrease
    						holeNumber--;
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						
    						//Updates the scorecard display if the decrease goes from the back to the front
    						if(holeNumber==9){
    							frontActive = true;
    							frontBackViewSwitcher();
    							updateScorecard();
    							
    							for(int x=1;x<=numberOfPlayers;x++)
    								updateScorecardTotals(x);
    						}
    						
    						//Loads the current hole's score
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
    						
    						//Gets the location and calls the method that calculates the distance to the green
    						Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    						if (location != null)
    							gpsClass.onLocationChanged(location);
    					}
    				}
    				
    				//Bottom to top swipe
    				else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE-60 && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    					//Sets the vibrate time
    					vibe.vibrate(15);
    					
    					if(playerNumber==numberOfPlayers){
    						//Only runs if it is not the last hole and player
    						if(holeNumber==18){
    							return false;
    						}
    						else{ 
    							//Increases the current hole and displays the increase
    							holeNumber++;
    							
    							//Gets the location and calls the method that calculates the distance to the green
    							Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    							if (location != null)
    								gpsClass.onLocationChanged(location);
    							
    							//Updates the scorecard display if the increase goes from the front to the back
    							if(holeNumber==10){
    								frontActive = false;
    								frontBackViewSwitcher();
    								updateScorecard();
    							
    								for(int x=1;x<=numberOfPlayers;x++)
    									updateScorecardTotals(x);
    							}
    						}
    						//Runs if the current player is the last player
    						playerNumber = 1;
        					
    						//Displays the new hole number, par, player name, and score
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						greenPlayerNameText.setText(playerName[playerNumber]);
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
    					}
    					else{   
    						//Runs if the current player is not the last player
    						playerNumber++;
    					
    						//Displays the new hole number, par, player name, and score
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						greenPlayerNameText.setText(playerName[playerNumber]);
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
    					}
    				}
    				
    				//Top to bottom swipe
    				else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE-60 && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    					//Sets the vibrate time
    					vibe.vibrate(15);
    					
    					if(playerNumber==1){
    						//Only runs if it is not the first hole and player
    						if(holeNumber==1){
    							return false;
    						}
    						else{ 
    							//Decreases the current hole and displays the decrease
    							holeNumber--;
    							
    							//Gets the location and calls the method that calculates the distance to the green
    							Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    							if (location != null)
    								gpsClass.onLocationChanged(location);
    							
    							//Updates the scorecard display if the decrease goes from the back to the front
    							if(holeNumber==9){
    								frontActive = true;
    								frontBackViewSwitcher();
    								updateScorecard();
    							
    								for(int x=1;x<=numberOfPlayers;x++)
    									updateScorecardTotals(x);
    							}
    						}
    						//Runs if the current player is the first player
    						playerNumber = numberOfPlayers;
    						
    						//Displays the new hole number, par, player name, and score
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						greenPlayerNameText.setText(playerName[playerNumber]);
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
    					}
    					else{
    						//Runs if the current player is not the first player
    						playerNumber--;
    					   			
    						//Displays the new hole number, par, player name, and score
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						greenPlayerNameText.setText(playerName[playerNumber]);
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
    					}  
    				}
    			} 
    			catch (Exception e) {
    				//Do nothing
    			}
    		}
    		else{
    			//Handles swipes for the scorecard tab
    			if(tabHost.getCurrentTab()==2){
        			try {
        				//Right to left swipe
        				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        					//Sets the vibrate time
        					vibe.vibrate(15);
        					
        					//Clears the edit score views if it is the last hole
        					if(holeNumber==18){
        						scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
						   	
								scorecardPlusButton.setVisibility(View.GONE);
								scorecardMinusButton.setVisibility(View.GONE);
								scorecardPlusButtonText.setVisibility(View.GONE);
								scorecardMinusButtonText.setVisibility(View.GONE);
								
								buttonsVisible = false;
        					}
        					else{  
        						//Runs if the edit score buttons are active
        						if(buttonsVisible){
        							//Increases the current hole number
        							holeNumber++;
        						
        							//Updates the scorecard display if the increase goes from the front to the back
        							if(holeNumber==10){
        								scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
        						   	
        								scorecardPlusButton.setVisibility(View.GONE);
        								scorecardMinusButton.setVisibility(View.GONE);
        								scorecardPlusButtonText.setVisibility(View.GONE);
        								scorecardMinusButtonText.setVisibility(View.GONE);
        		
        								frontActive = false;
        								frontBackViewSwitcher();
        								updateScorecard();
        							
        								for(int x=1;x<=numberOfPlayers;x++)
        									updateScorecardTotals(x);
        							}
        						}
        						
        						//Displays the new edit score buttons if the right nine holes is currently displayed
        						if(frontActive&&holeNumber<10 || !frontActive&&holeNumber>9)
        							playerScoreHandler();
        					}
        				}
        				
        				//Left to right swipe
        				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        					//Sets the vibrate time
        					vibe.vibrate(15);
        					
        					//Clears the edit score views if it is the first hole
        					if(holeNumber==1){
        						scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
						   	
								scorecardPlusButton.setVisibility(View.GONE);
								scorecardMinusButton.setVisibility(View.GONE);
								scorecardPlusButtonText.setVisibility(View.GONE);
								scorecardMinusButtonText.setVisibility(View.GONE);
								
								buttonsVisible = false;
        					}
	        				else{
	        					//Runs if the edit score buttons are active
	        					if(buttonsVisible){
	        						//Decreases the current hole number
        							holeNumber--;
        						
        							//Updates the scorecard display if the decrease goes from the back to the front
        							if(holeNumber==9){
        								scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
        							
        								scorecardPlusButton.setVisibility(View.GONE);
        								scorecardMinusButton.setVisibility(View.GONE);
        								scorecardPlusButtonText.setVisibility(View.GONE);
        								scorecardMinusButtonText.setVisibility(View.GONE);
        							
        								frontActive = true;
        								frontBackViewSwitcher();
        								updateScorecard();
        							
        								for(int x=1;x<=numberOfPlayers;x++)
        									updateScorecardTotals(x);
        							}
	        					}
        					
	        					//Displays the new edit score buttons if the right nine holes is currently displayed
	        					if(frontActive&&holeNumber<10 || !frontActive&&holeNumber>9)
	        						playerScoreHandler();
        					}
        				}
        				
        				//Bottom to top swipe
        				else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE-60 && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        					//Sets the vibrate time
        					vibe.vibrate(15);
        					
        					//Clears the edit score views if it is the last hole and player
        					if(playerNumber==numberOfPlayers && holeNumber==18){
        						scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
    						   	
								scorecardPlusButton.setVisibility(View.GONE);
								scorecardMinusButton.setVisibility(View.GONE);
								scorecardPlusButtonText.setVisibility(View.GONE);
								scorecardMinusButtonText.setVisibility(View.GONE);
								
								buttonsVisible = false;
        					}
        					else{
        						//Runs if it is the last player
        						if(playerNumber==numberOfPlayers){
        							//Runs if the edit score buttons are active
	        						if(buttonsVisible){
	        							//Increases the current hole number and sets the current player to the first player
	        							playerNumber = 1;
	        							holeNumber++;
	        						
	        							//Updates the scorecard display if the increase goes from the front to the back
	        							if(holeNumber==10){
	        								scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
	        						   	
	        								scorecardPlusButton.setVisibility(View.GONE);
	        								scorecardMinusButton.setVisibility(View.GONE);
	        								scorecardPlusButtonText.setVisibility(View.GONE);
	        								scorecardMinusButtonText.setVisibility(View.GONE);
	        							
		        					    	frontActive = false;
		        							frontBackViewSwitcher();
		        							updateScorecard();
		        							
		        							for(int x=1;x<=numberOfPlayers;x++)
		        								updateScorecardTotals(x);
		        							   		
		        						}
	        						}
	        						
	        						//Displays the new edit score buttons if the right nine holes is currently displayed
	        						if(frontActive&&holeNumber<10 || !frontActive&&holeNumber>9)
	        							playerScoreHandler();
        						}
        						//Runs if the current player is not the last player
	        					else{
	        						//Runs if the edit score buttons are active
	        						if(buttonsVisible)
	        							playerNumber++;
	        					
	        						//Displays the new edit score buttons if the right nine holes is currently displayed
	        						if(frontActive&&holeNumber<10 || !frontActive&&holeNumber>9)
	        							playerScoreHandler();
	        					}
        					}
        				}  
        				
        				//Top to bottom swipe
        				else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE-60 && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        					//Sets the vibrate time
        					vibe.vibrate(15);
        					
        					//Clears the edit score views if it is the first hole and player
        					if(playerNumber==1 && holeNumber==1){
        						scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
    						   	
								scorecardPlusButton.setVisibility(View.GONE);
								scorecardMinusButton.setVisibility(View.GONE);
								scorecardPlusButtonText.setVisibility(View.GONE);
								scorecardMinusButtonText.setVisibility(View.GONE);
								
								buttonsVisible = false;
        					}
        					else{
        						//Runs if it is the first player
        						if(playerNumber==1){
        							//Runs if the edit score buttons are active
	        						if(buttonsVisible){
	        							//Decreases the current hole number and sets the current player to the last player
	        							playerNumber = numberOfPlayers;
		        						holeNumber--;
		        						
		        						//Updates the scorecard display if the decrease goes from the back to the front
		        						if(holeNumber==9){
		        							scoreEntryScorecard.setBackgroundColor(0xFFFFC775);
		        						   	
		        							scorecardPlusButton.setVisibility(View.GONE);
		        							scorecardMinusButton.setVisibility(View.GONE);
		    								scorecardPlusButtonText.setVisibility(View.GONE);
		    								scorecardMinusButtonText.setVisibility(View.GONE);
		        							
		        							frontActive = true;
		        							frontBackViewSwitcher();
		        							updateScorecard();
		        							
		        							for(int x=1;x<=numberOfPlayers;x++)
		        								updateScorecardTotals(x);
		        							
		        						}
	        						}
	        						
	        						//Displays the new edit score buttons if the right nine holes is currently displayed
	        						if(frontActive&&holeNumber<10 || !frontActive&&holeNumber>9)
	        							playerScoreHandler();
	        					}
        						//Runs if the current player is not the first player
	        					else{
	        						//Runs if the edit score buttons are active
	        						if(buttonsVisible)
	        							playerNumber--;
	        					
	        						//Displays the new edit score buttons if the right nine holes is currently displayed
	        						if(frontActive&&holeNumber<10 || !frontActive&&holeNumber>9)
	        							playerScoreHandler();
	        					}
        					}
        				}
        			} 
        			catch (Exception e) {
        				//Do nothing
        			}
        		}
    		}	
            return false;
    	}
    	
    	@Override
    	public boolean onDown(MotionEvent e){
    	    return false;
    	}
    	
    	@Override
    	public void onLongPress(MotionEvent e){
    	}
    	     
    	@Override
    	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
    	    return false;
    	}
    	
    	@Override
    	public void onShowPress(MotionEvent e){
    	}
    	
    	@Override
    	public boolean onSingleTapUp(MotionEvent e){ 
    	    return false;
    	}
    }

    @Override
	public void onClick(View arg0) {
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	//Initializes the gps module
	private void gpsSetup(){
		gpsClass = new MyLocationListener();
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//Loads the gps related views
		retrieveLocationButton = (Button) findViewById(R.id.locationButton);
        gpsStatusPicture = (ImageView)findViewById(R.id.gpsstatusindicator);
         
        //Sets when the gps location should be updated
        locationManager.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                gpsClass
        );
             
        //Initializes the retrieve location button to display the current location
        retrieveLocationButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
    	                showCurrentLocation();
    	            }
    	});
    }
	
	//Shows the save round confirmation dialog if the back button is pressed
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	//Sets the vibrate time
			vibe.vibrate(15);
			
        	if(tabHost.getCurrentTab()==0)
        		builder.show();
        	else
        		tabHost.setCurrentTab(0);
        	return true;
        }
 	
        return super.onKeyDown(keyCode, event);
    }
	
	//Initializes the save round confirmation dialog
	private void buildDialog(){
		builder = new AlertDialog.Builder(this);
    	builder.setMessage("Would you like to save the round?");
    	builder.setCancelable(true);
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			//Sets the vibrate time
				vibe.vibrate(15);
				
				//Stops the gps module
    			locationManager.removeUpdates(gpsClass);

                saveRound();
	
	    	    //Closes the activity and returns the display to the home screen
	    	    finish();
    		}
    	});
    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			//Sets the vibrate time
				vibe.vibrate(15);
    			
    			//Stops the gps module
    			locationManager.removeUpdates(gpsClass);
    			
    			//Closes the activity without saving the round and returns the display to the home screen
    			finish();
    	    }
    	});
    	builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			//Sets the vibrate time
				vibe.vibrate(15);
				
    			//Closes the dialog box
    			dialog.cancel();
    	    }
    	});
	}

    private void saveRound(){

        playerDAO = new PlayerDAO(this);
        roundDAO = new RoundDAO(this);
        subRoundDAO = new SubRoundDAO(this);
        roundHoleDAO = new RoundHoleDAO(this);
        shotDAO = new ShotDAO(this);

        //Writes the date of the round to the file
        Calendar cal = Calendar.getInstance();

        Round round = null;
        SubRound subRound = null;
        RoundHole roundHole = null;
        Player player = null;
        List<CourseHole> courseHoles = null;

        round = new Round();

        round.setDate(cal.getTime());

        round.setID(roundDAO.createRound(round));

        boolean totalRoundNull = true;

        for (int subCourseNumber = 0; subCourseNumber < 2; subCourseNumber++) {

            courseHoles = courseHoleDAO.readListofCourseHoles(subCourses.get(subCourseNumber));

            subRound = new SubRound();

            subRound.setSubCourseID(subCourses.get(subCourseNumber));
            subRound.setRoundID(round);

            subRound.setID(subRoundDAO.createSubRound(subRound));

            for (int x = 1; x <= numberOfPlayers; x++) {
                player = new Player();

                Log.d("test", playerName[x]);

                player.setID(playerDAO.readIDFromName(playerName[x]));

                for (int y = 1; y < 10; y++) {
                    int playerScore = holeScore[x][9*(subCourseNumber) + y];

                    if (playerScore > 0) {
                        totalRoundNull = false;

                        roundHole = new RoundHole();

                        roundHole.setScore(playerScore);
                        roundHole.setSubRoundID(subRound);
                        roundHole.setPlayerID(player);
                        roundHole.setCourseHoleID(courseHoles.get(y - 1));

                        roundHole.setID(roundHoleDAO.createRoundHole(roundHole));
                    }
                }
            }
        }

        if (totalRoundNull) {
            //\todo Once created, use DAOUtility to delete a round
            List <SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            for (SubRound subRoundDelete : subRounds) {
                subRoundDAO.deleteSubRound(subRoundDelete);
            }
            roundDAO.deleteRound(round);
        }
    }
    
	//Shows the gps coordinates and the current city of the current location
    protected void showCurrentLocation() {
    	//Loads the last location
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	 
	    if (location != null) {
	    	//Displays a toast message of the current gps coordinates
	        String message = String.format(
	                "Current Location \n Longitude: %1$s \n Latitude: %2$s",
	                location.getLongitude(), location.getLatitude()
	        );
	        Toast.makeText(StartRound.this, message,Toast.LENGTH_SHORT).show();

	        String cityName = null;  
	        Geocoder gcd = new Geocoder(getBaseContext(),   
	          Locale.getDefault());  

	        //Loads the current location's city name
	        List<Address> addresses;  
	        try {  
	         addresses = gcd.getFromLocation(location.getLatitude(),  
	           location.getLongitude(), 1);  
	         if (addresses.size() > 0)   
	        	 cityName = addresses.get(0).getLocality();  
	        } catch (IOException e) {  
	         e.printStackTrace();  
	        }  

	        //Displays a toast message of the current city
	        message = "My Currrent City is: " + cityName;  
	        Toast.makeText(StartRound.this, message,Toast.LENGTH_SHORT).show();
	    }
	}
	
    //Handles the change in gps coordinates 
	private class MyLocationListener implements LocationListener {

		@SuppressWarnings("unused")
		//Run when the location of the gps is requested or has changed
		public void onLocationChanged(Location location){
			//Loads the location of the front of the green and displays the distance in yards from the current location
			greenLocation.setLatitude(greenLocations[0][0][holeNumber]);
	    	greenLocation.setLongitude(greenLocations[1][0][holeNumber]);
			TextView greenDistance = (TextView)findViewById(R.id.greenfrontdistance);
			distance = (int)Math.round(location.distanceTo(greenLocation) * 1.09361);
			greenDistance.setText(Integer.toString(distance));
			
			//Loads the location of the middle of the green and displays the distance in yards from the current location
			greenLocation.setLatitude(greenLocations[0][1][holeNumber]);
	    	greenLocation.setLongitude(greenLocations[1][1][holeNumber]);
			greenDistance = (TextView)findViewById(R.id.greenmiddledistance);
			distance = (int)Math.round(location.distanceTo(greenLocation) * 1.09361);
			greenDistance.setText(Integer.toString(distance));
			middleDistance = distance;
			
			//Loads the location of the back of the green and displays the distance in yards from the current location
			greenLocation.setLatitude(greenLocations[0][2][holeNumber]);
	    	greenLocation.setLongitude(greenLocations[1][2][holeNumber]);
			greenDistance = (TextView)findViewById(R.id.greenbackdistance);
			distance = (int)Math.round(location.distanceTo(greenLocation) * 1.09361);
			greenDistance.setText(Integer.toString(distance));
			
			//Displays the gps status indicator based on if the current location exists
			if(location != null)
				gpsStatusPicture.setImageResource(R.drawable.gpsstatusgood);
			else
				gpsStatusPicture.setImageResource(R.drawable.gpsstatusbad);
		}
 
		//Displays the gps status indicator based on if the current location exists
		public void onStatusChanged(String s, int i, Bundle b) {
			if(i>0)
				gpsStatusPicture.setImageResource(R.drawable.gpsstatusgood);
			else
				gpsStatusPicture.setImageResource(R.drawable.gpsstatusbad);
		}

		//Displays the gps status indicator based on if the current location exists
		public void onProviderDisabled(String s) {
			gpsStatusPicture.setImageResource(R.drawable.gpsstatusbad);
			Toast.makeText(StartRound.this, "Provider status changed to Disabled",Toast.LENGTH_SHORT).show();
		}

		//Displays the gps status indicator based on if the current location exists
		public void onProviderEnabled(String s) {
			gpsStatusPicture.setImageResource(R.drawable.gpsstatusgood);
			Toast.makeText(StartRound.this, "Provider status changed to Enabled",Toast.LENGTH_SHORT).show();
		}
	}
	
	//Initializes the map view tab
	private void mapInitializer(){
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.maptab2))
		        .getMap();
		
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		map.setOnMapClickListener(this);
	}
	
	//Sets the zoom and center of the map view
	//Runs when the map view tab is selected
	private void setMapLocation(){
		//Gets the current location
		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		//Test values
		//greenLocations[0][2][1]=42.351185;
		//greenLocations[1][2][1]=-71.137167;
		
		map.clear();
		
		if(location!=null){			
			//Loads the current location and the location of the middle of the green for the current hole
			lat[0] = (location.getLatitude());
			lat[1] = (greenLocations[0][1][holeNumber]);
			lng[0] = (location.getLongitude());
			lng[1] = (greenLocations[1][1][holeNumber]);
		}
		else{
			//Loads the tee location and the location of the middle of the green for the current hole
			lat[0] = (teeLocations[0][holeNumber]);
			lat[1] = (greenLocations[0][1][holeNumber]);
			lng[0] = (teeLocations[1][holeNumber]);
			lng[1] = (greenLocations[1][1][holeNumber]);
		}
		
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
		//Sets the vibrate time
		vibe.vibrate(15);
		
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
				//Sets the vibrate time
				vibe.vibrate(15);
				
				marker.hideInfoWindow();
			}
		});
		
		map.setOnMarkerClickListener(new OnMarkerClickListener(){
			public boolean onMarkerClick(Marker marker){
				//Sets the vibrate time
				vibe.vibrate(15);
				
				if(!marker.equals(mapClickMarker))
					marker.showInfoWindow();
					
				mapClickMarker.remove();
				mapPolyline.remove();
				
				return true;
			}
		});
	}
}

