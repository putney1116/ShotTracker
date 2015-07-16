package com.example.android.ShotTracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ShotTracker.db.BagDAO;
import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.CourseHoleDAO;
import com.example.android.ShotTracker.db.CourseHoleInfoDAO;
import com.example.android.ShotTracker.db.DAOUtilities;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.db.RoundDAO;
import com.example.android.ShotTracker.db.RoundHoleDAO;
import com.example.android.ShotTracker.db.ShotDAO;
import com.example.android.ShotTracker.db.ShotLinkDAO;
import com.example.android.ShotTracker.db.ShotTypeDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.db.SubRoundDAO;
import com.example.android.ShotTracker.objects.Club;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;
import com.example.android.ShotTracker.objects.Shot;
import com.example.android.ShotTracker.objects.ShotType;
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

	//\todo Copy xml changes to inroundscreen2
	
	private TabHost tabHost;
	private TextView scoreEntryGreen;
	private TextView scoreEntryGreenShots;
	private TextView scoreEntryGreenPutts;
	private TextView scoreEntryGreenChips;
	private TextView scoreEntryGreenPenalty;
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
	private int shotScore[][] = new int[5][19];
	private int puttScore[][] = new int[5][19];
	private int chipScore[][] = new int[5][19];
	private int penaltyScore[][] = new int[5][19];
	private boolean fairwayHit[][] = new boolean[5][19];
	String playerName[] = {"","","","",""};
	
	View.OnTouchListener gestureListener;
	private GestureDetector swipeGesture;	
	
	protected LocationManager locationManager;
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
	private ShotLinkDAO shotLinkDAO = null;
	private BagDAO bagDAO = null;
	private ShotTypeDAO shotTypeDAO = null;

    private Course course = null;
    private List<SubCourse> subCourses = null;

	private boolean eighteenHoleRound = true;

	private int caddyPlayerNumber = 0;
    
	public void onCreate(Bundle savedInstanceState) {
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
	    super.onCreate(savedInstanceState);
    	
    	//Loads the player names and course information
    	loadCourseInfo();

		selectLayout();
    	
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

		//Initializes the caddy screen
		CaddyScreenInitializer();
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

		if(back9SubCourseID==-10){
			eighteenHoleRound = false;
		}

        subCourses = new ArrayList<SubCourse>();

        subCourses.add(subCourseDAO.readSubCoursefromID(front9SubCourseID));
		if(eighteenHoleRound) {
			subCourses.add(subCourseDAO.readSubCoursefromID(back9SubCourseID));
		}
		
		numberOfPlayers = myIntent.getIntExtra("Players", 0);
		
		//Loads the player names from the previous activity
		for(int x=1;x<=numberOfPlayers;x++){
			playerName[x] = myIntent.getStringExtra("Player"+(x-1));
		}

        //Saves the official course name
        courseID = subCourseDAO.readSubCoursefromID(front9SubCourseID).getCourseID();
        course = courseDAO.readCourseFromID(courseID);
        courseName = course.getName();

        int holecounter = 0;

        for (SubCourse subCourse : subCourses){
            List<CourseHole> courseHoles = courseHoleDAO.readListofCourseHoles(subCourse);

            for (CourseHole courseHole : courseHoles){

                holecounter++;

                List<CourseHoleInfo> courseHoleInfos = courseHoleInfoDAO.readListofCourseHoleInfos(courseHole);
                courseHole.setCourseHoleInfoList(courseHoleInfos);

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

	public void selectLayout(){
		if(eighteenHoleRound) {
			setContentView(R.layout.inroundscreen);
		}
		else {
			setContentView(R.layout.inroundscreen2);
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
    	if(eighteenHoleRound) {
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
			for (int x = 1; x <= numberOfPlayers; x++)
				updateScorecardTotals(x);
		}
	}
    
    //Initializes the main screen plus minus and finish buttons
    private void plusMinusFinishButtonHandler() {
    	Button plusButton = (Button)findViewById(R.id.plusbutton);
    	Button minusButton = (Button)findViewById(R.id.minusbutton);
    	Button finishButton = (Button)findViewById(R.id.finishbutton);
		final Button fairwayButton = (Button)findViewById(R.id.fairwayhitbutton);
    		
    	scoreEntryGreen = (TextView)findViewById(R.id.totalscore);

		scoreEntryGreenShots = (TextView)findViewById(R.id.scoreentry);
    	
    	//Initialize Vibrate
    	vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    			
    	//Displays the course name on the main screen
    	TextView courseNameText = (TextView)findViewById(R.id.coursename);
    	courseNameText.setText(courseName);
    	
    	plusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Increases the active player's score
					shotScore[playerNumber][holeNumber]++;
					holeScore[playerNumber][holeNumber]++;

					//Displays the increased number
					scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
					scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

					//Loads the correct view from the scorecard tab
					setTextViewHoleNumber(playerNumber, holeNumber);

					//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					if (holeNumber < 10) {
						if (frontActive) {
							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}
					//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					else {
						if (!frontActive) {
							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}

				} catch (Exception e) {
				}
			}
		});
    	
    	minusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Decreases the active player's score if the score is not already 0
					if (shotScore[playerNumber][holeNumber] != 0) {
						shotScore[playerNumber][holeNumber]--;
						holeScore[playerNumber][holeNumber]--;

						//Displays the decreased number
						scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

						//Loads the correct view from the scorecard tab
						setTextViewHoleNumber(playerNumber, holeNumber);

						//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						if (holeNumber < 10) {
							if (frontActive) {
								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
						//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						else {
							if (!frontActive) {
								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
					}
				} catch (Exception e) {
				}
			}
		});



		scoreEntryGreenPutts = (TextView)findViewById(R.id.puttscoreentry);

		//\todo handle for 9 holes. Need to port caddy screen changes over to inroundscreen2
		plusButton = (Button)findViewById(R.id.puttplusbutton);
		minusButton = (Button)findViewById(R.id.puttminusbutton);

		plusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Increases the active player's score
					puttScore[playerNumber][holeNumber]++;
					holeScore[playerNumber][holeNumber]++;

					//Displays the increased number
					scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
					scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

					//Loads the correct view from the scorecard tab
					setTextViewHoleNumber(playerNumber, holeNumber);

					//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					if (holeNumber < 10) {
						if (frontActive) {

							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}
					//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					else {
						if (!frontActive) {

							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}

				} catch (Exception e) {
				}
			}
		});

		minusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Decreases the active player's score if the score is not already 0
					if (puttScore[playerNumber][holeNumber] != 0) {
						puttScore[playerNumber][holeNumber]--;
						holeScore[playerNumber][holeNumber]--;

						//Displays the decreased number
						scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

						//Loads the correct view from the scorecard tab
						setTextViewHoleNumber(playerNumber, holeNumber);

						//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						if (holeNumber < 10) {
							if (frontActive) {

								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
						//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						else {
							if (!frontActive) {

								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
					}
				} catch (Exception e) {
				}
			}
		});


		scoreEntryGreenChips = (TextView)findViewById(R.id.chipscoreentry);

		plusButton = (Button)findViewById(R.id.chipplusbutton);
		minusButton = (Button)findViewById(R.id.chipminusbutton);

		plusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Increases the active player's score
					chipScore[playerNumber][holeNumber]++;
					holeScore[playerNumber][holeNumber]++;

					//Displays the increased number
					scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
					scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

					//Loads the correct view from the scorecard tab
					setTextViewHoleNumber(playerNumber, holeNumber);

					//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					if (holeNumber < 10) {
						if (frontActive) {

							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}
					//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					else {
						if (!frontActive) {

							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}

				} catch (Exception e) {
				}
			}
		});

		minusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Decreases the active player's score if the score is not already 0
					if (chipScore[playerNumber][holeNumber] != 0) {
						chipScore[playerNumber][holeNumber]--;
						holeScore[playerNumber][holeNumber]--;

						//Displays the decreased number
						scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

						//Loads the correct view from the scorecard tab
						setTextViewHoleNumber(playerNumber, holeNumber);

						//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						if (holeNumber < 10) {
							if (frontActive) {

								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
						//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						else {
							if (!frontActive) {

								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
					}
				} catch (Exception e) {
				}
			}
		});



		scoreEntryGreenPenalty = (TextView)findViewById(R.id.penaltyscoreentry);

		plusButton = (Button)findViewById(R.id.penaltyplusbutton);
		minusButton = (Button)findViewById(R.id.penaltyminusbutton);

		plusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Increases the active player's score
					penaltyScore[playerNumber][holeNumber]++;
					holeScore[playerNumber][holeNumber]++;

					//Displays the increased number
					scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));
					scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

					//Loads the correct view from the scorecard tab
					setTextViewHoleNumber(playerNumber, holeNumber);

					//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					if (holeNumber < 10) {
						if (frontActive) {

							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}
					//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
					//If so, the scorecard display is updated to show the change in score
					else {
						if (!frontActive) {

							scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

							updateScorecardTotals(playerNumber);
						}
					}

				} catch (Exception e) {
				}
			}
		});

		minusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Decreases the active player's score if the score is not already 0
					if (penaltyScore[playerNumber][holeNumber] != 0) {
						penaltyScore[playerNumber][holeNumber]--;
						holeScore[playerNumber][holeNumber]--;

						//Displays the decreased number
						scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));
						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

						//Loads the correct view from the scorecard tab
						setTextViewHoleNumber(playerNumber, holeNumber);

						//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						if (holeNumber < 10) {
							if (frontActive) {

								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
						//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
						//If so, the scorecard display is updated to show the change in score
						else {
							if (!frontActive) {

								if (holeScore[playerNumber][holeNumber] == 0)
									scoreEntryScorecard.setText("");
								else
									scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

								updateScorecardTotals(playerNumber);
							}
						}
					}
				} catch (Exception e) {
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


		for (int x = 1; x < 19; x++) {
			for (int y = 1; y < 5; y++) {
				fairwayHit[y][x] = false;
			}
		}

		if(par[holeNumber] != 3) {
			fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
			fairwayButton.setEnabled(true);
		}
		else {
			fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
			fairwayButton.setEnabled(false);
		}

		fairwayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {

					if(par[holeNumber] != 3){
						//Sets the vibrate time
						vibe.vibrate(15);

						if (fairwayHit[playerNumber][holeNumber]) {
							fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
							fairwayHit[playerNumber][holeNumber] = false;
						} else {
							fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
							fairwayHit[playerNumber][holeNumber] = true;
						}
					}

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
					shotScore[playerNumber][holeNumber]++;
					
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
					if(shotScore[playerNumber][holeNumber]!=0){
						holeScore[playerNumber][holeNumber]--;
						shotScore[playerNumber][holeNumber]--;

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
	    	if(frontActive) {
				if(holeNumber!=1) {
					holeNumber = 1;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=10) {
					holeNumber = 10;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=1) {
					holeNumber = 1;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=10) {
					holeNumber = 10;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=1) {
					holeNumber = 1;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=10) {
					holeNumber = 10;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=1) {
					holeNumber = 1;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=10) {
					holeNumber = 10;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=2) {
					holeNumber = 2;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=11) {
					holeNumber = 11;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=2) {
					holeNumber = 2;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=11) {
					holeNumber = 11;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=2) {
					holeNumber = 2;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=11) {
					holeNumber = 11;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=2) {
					holeNumber = 2;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=11) {
					holeNumber = 11;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=3) {
					holeNumber = 3;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=12) {
					holeNumber = 12;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=3) {
					holeNumber = 3;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=12) {
					holeNumber = 12;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=3) {
					holeNumber = 3;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=12) {
					holeNumber = 12;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=3) {
					holeNumber = 3;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=12) {
					holeNumber = 12;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=4) {
					holeNumber = 4;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=13) {
					holeNumber = 13;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=4) {
					holeNumber = 4;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=13) {
					holeNumber = 13;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=4) {
					holeNumber = 4;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=13) {
					holeNumber = 13;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=4) {
					holeNumber = 4;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=13) {
					holeNumber = 13;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=5) {
					holeNumber = 5;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=14) {
					holeNumber = 14;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=5) {
					holeNumber = 5;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=14) {
					holeNumber = 14;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=5) {
					holeNumber = 5;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=14) {
					holeNumber = 14;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=5) {
					holeNumber = 5;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=14) {
					holeNumber = 14;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=6) {
					holeNumber = 6;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=15) {
					holeNumber = 15;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=6) {
					holeNumber = 6;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=15) {
					holeNumber = 15;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=6) {
					holeNumber = 6;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=15) {
					holeNumber = 15;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=6) {
					holeNumber = 6;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=15) {
					holeNumber = 15;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=7) {
					holeNumber = 7;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=16) {
					holeNumber = 16;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=7) {
					holeNumber = 7;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=16) {
					holeNumber = 16;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=7) {
					holeNumber = 7;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=16) {
					holeNumber = 16;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=7) {
					holeNumber = 7;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=16) {
					holeNumber = 16;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=8) {
					holeNumber = 8;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=17) {
					holeNumber = 17;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=8) {
					holeNumber = 8;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=17) {
					holeNumber = 17;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=8) {
					holeNumber = 8;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=17) {
					holeNumber = 17;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=8) {
					holeNumber = 8;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=17) {
					holeNumber = 17;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=9) {
					holeNumber = 9;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=18) {
					holeNumber = 18;
					CaddyScreenResetShot();
				}
			}
    	
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
			if(frontActive) {
				if(holeNumber!=9) {
					holeNumber = 9;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=18) {
					holeNumber = 18;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=9) {
					holeNumber = 9;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=18) {
					holeNumber = 18;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(frontActive) {
				if(holeNumber!=9) {
					holeNumber = 9;
					CaddyScreenResetShot();
				}
			}
			else{
				if(holeNumber!=18) {
					holeNumber = 18;
					CaddyScreenResetShot();
				}
			}
	    	
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
			if(eighteenHoleRound) {
				scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.front9Button);
				scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
				scorecardFrontBackButton = (RelativeLayout) findViewById(R.id.back9Button);
				scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
			}
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
		if(eighteenHoleRound) {
			scorecardText.setText(courseName);
			scorecardText = (TextView)findViewById(R.id.front9ButtonText);
			scorecardText.setText(subCourses.get(0).getName());
			scorecardText = (TextView)findViewById(R.id.back9ButtonText);
			scorecardText.setText(subCourses.get(1).getName());
		}
		else{
			scorecardText.setText(courseName + " - " + subCourses.get(0).getName());
		}
    	
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
        if(eighteenHoleRound) {
			scorecardButtons = (RelativeLayout) findViewById(R.id.front9Button);
			scorecardButtons.setOnTouchListener(gestureListener);
			scorecardButtons = (RelativeLayout) findViewById(R.id.back9Button);
			scorecardButtons.setOnTouchListener(gestureListener);
		}
        
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceTopLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlusButtonLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlusButtonLevelMiddle);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceMinusButtonLevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceMinusButtonLevelMiddle);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer1LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer1LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer2LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer2LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer3LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer3LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer4LevelLeft);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpacePlayer4LevelRight);
        scorecardButtons.setOnTouchListener(gestureListener);
        scorecardButtons = (RelativeLayout)findViewById(R.id.blankSpaceBottomLevelLeft);
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
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));
	    	   				holeNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
	    	   				parText.setText("Par " + par[holeNumber]);
    						playerNameText.setText(playerName[playerNumber]);

							Button fairwayButton = (Button)findViewById(R.id.fairwayhitbutton);

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}
    						
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
							else{
								if(tabHost.getCurrentTab()==3){
									//Sets the hole number if the caddy tab is selected
									CaddyScreenFrontHoleSwitcher();
								}
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
		private Button fairwayButton = (Button)findViewById(R.id.fairwayhitbutton);
    	 
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
						if ((eighteenHoleRound && holeNumber == 18) || (!eighteenHoleRound && holeNumber == 9)){
							return false;
						} else {
							//Increases the current hole and displays the increase
							holeNumber++;
							CaddyScreenResetShot();
							greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
							greenParText.setText("Par " + par[holeNumber]);

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}

							//Updates the scorecard display if the increase goes from the front to the back
							if (holeNumber == 10) {
								frontActive = false;
								frontBackViewSwitcher();
								updateScorecard();

								for (int x = 1; x <= numberOfPlayers; x++)
									updateScorecardTotals(x);
							}

							//Loads the current hole's score
							scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));

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
							CaddyScreenResetShot();
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}
    						
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
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));
    						
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
    						if((eighteenHoleRound && holeNumber == 18) || (!eighteenHoleRound && holeNumber == 9)){
    							return false;
    						}
    						else{ 
    							//Increases the current hole and displays the increase
    							holeNumber++;
								CaddyScreenResetShot();
    							
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
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}
    					}
    					else{   
    						//Runs if the current player is not the last player
    						playerNumber++;
    					
    						//Displays the new hole number, par, player name, and score
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						greenPlayerNameText.setText(playerName[playerNumber]);
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}
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
								CaddyScreenResetShot();
    							
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
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}
    					}
    					else{
    						//Runs if the current player is not the first player
    						playerNumber--;
    					   			
    						//Displays the new hole number, par, player name, and score
    						greenHoleNumberTextView.setText("Hole " + Integer.toString(holeNumberText[holeNumber]));
    						greenParText.setText("Par " + par[holeNumber]);
    						greenPlayerNameText.setText(playerName[playerNumber]);
    						scoreEntryGreen.setText(Integer.toString(holeScore[playerNumber][holeNumber]));
							scoreEntryGreenShots.setText(Integer.toString(shotScore[playerNumber][holeNumber]));
							scoreEntryGreenPutts.setText(Integer.toString(puttScore[playerNumber][holeNumber]));
							scoreEntryGreenChips.setText(Integer.toString(chipScore[playerNumber][holeNumber]));
							scoreEntryGreenPenalty.setText(Integer.toString(penaltyScore[playerNumber][holeNumber]));

							if(par[holeNumber] == 3){
								fairwayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
								fairwayButton.setEnabled(false);
							}
							else {
								fairwayButton.setEnabled(true);
								if (fairwayHit[playerNumber][holeNumber]) {
									fairwayButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
								} else {
									fairwayButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
								}
							}
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
        					if((eighteenHoleRound && holeNumber == 18) || (!eighteenHoleRound && holeNumber == 9)){
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
									CaddyScreenResetShot();
        						
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
									CaddyScreenResetShot();
        						
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
        					if(playerNumber==numberOfPlayers && ((eighteenHoleRound && holeNumber == 18) || (!eighteenHoleRound && holeNumber == 9))){
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
										CaddyScreenResetShot();
	        						
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
										CaddyScreenResetShot();
		        						
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
        gpsStatusPicture = (ImageView)findViewById(R.id.gpsstatusindicator);
         
        //Sets when the gps location should be updated
        locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				MINIMUM_TIME_BETWEEN_UPDATES,
				MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
				gpsClass
		);
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
		shotLinkDAO = new ShotLinkDAO(this);

        //Writes the date of the round to the file
        Calendar cal = Calendar.getInstance();

        List<CourseHole> courseHoles = null;
		Player player = null;
		SubRound subRound = null;
		RoundHole roundHole = null;

        round.setID(roundDAO.createRound(round));

        boolean totalRoundNull = true;

        for (int subCourseNumber = 0; subCourseNumber < subCourses.size(); subCourseNumber++) {

            courseHoles = courseHoleDAO.readListofCourseHoles(subCourses.get(subCourseNumber));

			subRound = round.getSubRoundList().get(subCourseNumber);

            subRound.setSubCourseID(subCourses.get(subCourseNumber));
            subRound.setRoundID(round);

            subRound.setID(subRoundDAO.createSubRound(subRound));

            for (int x = 1; x <= numberOfPlayers; x++) {
                player = new Player();

                player.setID(playerDAO.readIDFromName(playerName[x]));

                for (int y = 1; y < 10; y++) {
					if (holeScore[x][9*(subCourseNumber) + y] > 0) {
                        totalRoundNull = false;

						roundHole = subRound.getRoundHoleList().get(((x-1) * 9) + (y-1));

                        roundHole.setScore(holeScore[x][9*(subCourseNumber) + y]);
						roundHole.setPenalties(penaltyScore[x][9 * (subCourseNumber) + y]);
						roundHole.setPutts(puttScore[x][9 * (subCourseNumber) + y]);
						roundHole.setChips(chipScore[x][9 * (subCourseNumber) + y]);
						roundHole.setFairways(fairwayHit[x][9*(subCourseNumber) + y]);
						if(holeScore[x][9*(subCourseNumber)+y] - puttScore[x][9*(subCourseNumber)+y] <= par[9*(subCourseNumber)+y] - 2)
							roundHole.setGiR(true);
						else
							roundHole.setGiR(false);
						//\todo Add code so that GIR is an array similar to fairways and can be called by other parts of the code
						roundHole.setSubRoundID(subRound);
                        roundHole.setPlayerID(player);
                        roundHole.setPlayerNumber((long)x);
                        roundHole.setCourseHoleID(courseHoles.get(y - 1));

                        roundHole.setID(roundHoleDAO.createRoundHole(roundHole));

                        try {
                            for (Shot shot : roundHole.getShotList()) {

                                shot.setRoundHoleID(roundHole);

                                shot.setID(shotDAO.createShot(shot));

                                for (ShotType shotType : shot.getShotTypePreList()) {
                                    shotLinkDAO.createShotLink(shot, shotType);
                                }

                                for (ShotType shotType : shot.getShotTypePostList()) {
                                    shotLinkDAO.createShotLink(shot, shotType);
                                }
                            }
                        } catch(Exception e){

                            }
                        }
                }
            }
        }

        if (totalRoundNull) {
			DAOUtilities daoUtility = new DAOUtilities(this);

			daoUtility.deleteRound(round);
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

		//\todo Map screen needs to update on the fly if location is changing

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

			//Sets the previous shot and next shot location text bars
			greenDistance = (TextView) findViewById(R.id.caddyMainSectionDistanceToPinText);
			if(targetNotPin){
				distance = (int)Math.round(location.distanceTo(targetLocation) * 1.09361);
				greenDistance.setText("Distance to Map Target: " + distance + " yds");
			}
			else {
				greenDistance.setText("Distance to Middle of Green: " + middleDistance + " yds");
			}

			greenDistance = (TextView) findViewById(R.id.caddyMainSectionPreviousShotText);
			if(initialLocationRecorded){
				distance = (int) Math.round(location.distanceTo(shot.getShotStartLocation()) * 1.09361);
				greenDistance.setText("Previous Shot Distance: " + distance + " yds");
			}
			else {
				greenDistance.setText("");
			}
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
	public void onMapClick(LatLng point) {
		//Sets the vibrate time
		vibe.vibrate(15);

		if (mapClickMarker != null) {
			mapPolyline.remove();
			mapClickMarker.remove();
		}

		tapLocation.setLatitude(point.latitude);
		tapLocation.setLongitude(point.longitude);

		distance = (int) Math.round(tapLocation.distanceTo(playerLocation) * 1.09361);
		middleDistance = (int) Math.round(tapLocation.distanceTo(greenLocation) * 1.09361);

		mapClickMarker = map.addMarker(new MarkerOptions()
				.position(point)
				.title(distance + " yds away")
				.snippet(middleDistance + " yds to pin")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.golfball)));

		mapClickMarker.showInfoWindow();

		mapPolyline = map.addPolyline(new PolylineOptions()
				.add(new LatLng(lat[0], lng[0]))
				.add(new LatLng(point.latitude, point.longitude))
				.add(new LatLng(lat[1], lng[1]))
				.color(Color.GRAY)
				.width(5));

		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			public void onInfoWindowClick(Marker marker) {
				//Sets the vibrate time
				vibe.vibrate(15);

				marker.hideInfoWindow();
			}
		});

		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(Marker marker) {
				//Sets the vibrate time
				vibe.vibrate(15);

				if (!marker.equals(mapClickMarker))
					marker.showInfoWindow();

				mapClickMarker.remove();
				mapPolyline.remove();

				return true;
			}
		});
	}

	private ViewEditCurrentRound viewEditCurrentRound = null;

	private List[] shotArray = null;
	private Button editRoundButton = null;
	private Button changeTargetButton = null;
	private Button finishHoleButton = null;
	private Button recordLocationButton = null;
	private Button countDistanceButton = null;
	private Button selectClubButton = null;
	private Button selectResultButton = null;
	private Button selectLieButton = null;
	private Button middleClubButton = null;
	private Button leftClubButton = null;
	private Button rightClubButton = null;
	private Button farRightClubButton = null;
	private Button farLeftClubButton = null;
	private TextView caddyTitleBarText = null;
	private TextView caddyHoleNumberText = null;
	private boolean targetNotPin = false;
	private boolean initialLocationRecorded = false;
	private Location targetLocation = null;
	private Location shotStartLocation = null;
	private int request_code = 1;
	private boolean countDistance = true;

	private TextView finishHoleShotScore = null;
	private TextView finishHolePuttScore = null;
	private TextView finishHoleChipScore = null;
	private TextView finishHolePenaltyScore = null;
	private TextView finishHoleTotalScore = null;

	private List<RoundHole> roundHoles = new ArrayList<RoundHole>();

	private List<Club> clubs = null;

	private int currentMainClubListIndex;
	private int clubListLength;

	AlertDialog.Builder clubBuilder = null;
	AlertDialog.Builder resultBuilder = null;
	AlertDialog.Builder lieBuilder = null;
	AlertDialog.Builder finishHoleDialog = null;

	//Initialize Caddy Screen
	private void CaddyScreenInitializer(){
		//Set the title bar to the course and subcourse name
		caddyTitleBarText = (TextView) findViewById(R.id.caddyCourseTitle);
		String subCourseName = subCourses.get(0).getName();
		caddyTitleBarText.setText(courseName + " - " + subCourseName);

		//Set the hole number to 1
		caddyHoleNumberText = (TextView) findViewById(R.id.caddyHoleNumber);
		caddyHoleNumberText.setText("Hole " + holeNumber);

		bagDAO = new BagDAO(this);
		playerDAO = new PlayerDAO(this);
		shotTypeDAO = new ShotTypeDAO(this);

		Player defaultPlayer = playerDAO.readUserDefaultPlayer();

		boolean defaultPlayerExists = false;

		for(int x = 1; x <= numberOfPlayers;x++){
			if(defaultPlayer.getName().equals(playerName[x])){
				defaultPlayerExists = true;
				caddyPlayerNumber = x;
				break;
			}
		}

		if(!defaultPlayerExists){
			Long id = playerDAO.readIDFromName(playerName[1]);
			Player player = new Player();
			player.setID(id);
			defaultPlayer = playerDAO.readPlayer(player);
			caddyPlayerNumber = 1;
		}


		clubs = bagDAO.readClubsInBag(defaultPlayer);

		clubListLength = clubs.size();

		//\todo set this index based on current distance to target
		currentMainClubListIndex = 6;

		rightClubButton = (Button) findViewById(R.id.caddyRightClubButton);
		leftClubButton = (Button) findViewById(R.id.caddyLeftClubButton);
		middleClubButton = (Button) findViewById(R.id.caddyMiddleClubButton);
		farRightClubButton = (Button) findViewById(R.id.caddyFarRightClubButton);
		farLeftClubButton = (Button) findViewById(R.id.caddyFarLeftClubButton);

		farRightClubButton.setText(clubs.get(currentMainClubListIndex-2).getClub());
		rightClubButton.setText(clubs.get(currentMainClubListIndex-1).getClub());
		middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
		leftClubButton.setText(clubs.get(currentMainClubListIndex+1).getClub());
		farLeftClubButton.setText(clubs.get(currentMainClubListIndex+2).getClub());

		//Initializes the EditRoundButton
		EditRoundButtonInitializer();

		//Initializes the ChangeTargetButton
		ChangeTargetButtonInitializer();

		//Initialize the title bar text
		CaddyScreenFrontHoleSwitcher();

		//Initialze the RecordLocationButton
		CaddyRecordLocationButtonInitializer();

		//Initialze the FinishHoleButton
		CaddyFinishHoleButtonInitializer();

		//Initialze the CountDistanceButton
		CaddyCountDistanceButtonInitializer();

		//Initialze the SelectClubButton
		CaddySelectClubButtonInitializer();

		//Initialze the SelectClubButton
		CaddySelectResultButtonInitializer();

		//Initialze the SelectClubButton
		CaddySelectLieButtonInitializer();

		//Initialize the LeftClubButton
		CaddyLeftClubButtonInitializer();

		//Initialize the RightClubButton
		CaddyRightClubButtonInitializer();

		//Initialize the MiddleClubButton
		CaddyMiddleClubButtonInitializer();

		//Sets up the round to be used by the caddy screen and to later be saved
		CaddySetUpRound();
	}

	//Initialzes the Change Target Button
	private void ChangeTargetButtonInitializer(){
		changeTargetButton = (Button) findViewById(R.id.caddyViewMapButton);

		changeTargetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//\todo call Jensens class for displaying the map and changing the target
				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the View/Edit Round Button
	private void EditRoundButtonInitializer(){

		shotArray = new List[19];

		for(int x = 0;x<19;x++){
			shotArray[x] = new ArrayList<Shot>();
		}



		editRoundButton = (Button) findViewById(R.id.caddyEditRoundButton);

		viewEditCurrentRound = new ViewEditCurrentRound(this, this);

		editRoundButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					Log.d("Test", "Switching classes...");

					/*//\todo change all serializable objects to parcelable objects if slow
					Intent myIntent = new Intent(v.getContext(), ViewEditCurrentRound.class);
					//\todo Make sure data passing is correct. Might have to implement serializable clas
					myIntent.putExtra("Round Holes", (Serializable)roundHoles);
					myIntent.putExtra("Green Locations", greenLocations);
					myIntent.putExtra("Tee Locations", teeLocations);
					myIntent.putExtra("Hole Number", holeNumber);
					myIntent.putExtra("Hole Number Text", holeNumberText);
					myIntent.putExtra("Eighteen", eighteenHoleRound);
					myIntent.putExtra("Player Name", playerName[1]);

					//The activity is started
					startActivityForResult(myIntent, request_code);*/

					shotArray = viewEditCurrentRound.ViewEditCurrentRoundMain(shotArray, greenLocations, teeLocations, holeNumber, holeNumberText, eighteenHoleRound);

					//\todo reset from justin's xml here by calling all initializer methods
				} catch (Exception e) {
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == request_code) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				roundHoles = (List<RoundHole>) data.getSerializableExtra("Round Hole List");
			}
		}
	}

	private Round round = null;
	private Shot shot = new Shot();

	//Sets up the Round
	private void CaddySetUpRound(){
		//Writes the date of the round to the file
		Calendar cal = Calendar.getInstance();

		SubRound subRound = null;
		RoundHole roundHole = null;

		round = new Round();

		round.setDate(cal.getTime());

		for (int subCourseNumber = 0; subCourseNumber < subCourses.size(); subCourseNumber++) {

			subRound = new SubRound();

			for (int x = 1; x <= numberOfPlayers; x++) {

				for (int y = 1; y < 10; y++) {
					roundHole = new RoundHole();

					subRound.addRoundHole(roundHole);
				}
			}

			round.addSubRound(subRound);
		}
	}

	//Initializes the Record Location Round Button
	private void CaddyRecordLocationButtonInitializer(){

		recordLocationButton = (Button) findViewById(R.id.caddyRecordLocationButton);

		recordLocationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					//Gets the current location
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

					if(!initialLocationRecorded){

						shot.setShotStartLatLong(location.getLatitude(), location.getLongitude());

						initialLocationRecorded = true;

						recordLocationButton.setText("At Ball");

						TextView previousShotTitleText = (TextView) findViewById(R.id.caddyBottomSectionTitleText);
						previousShotTitleText.setText("Previous Shot");

						TextView previousShotDistanceText = (TextView) findViewById(R.id.caddyMainSectionPreviousShotText);
						previousShotDistanceText.setText("Previous Shot Distance: " + (int)Math.round(location.distanceTo(shot.getShotStartLocation())* 1.09361) + " yds");
					}
					else{

						int subRoundNumber;
						int subRoundHoleNumber;

						if(holeNumber < 10) {
							subRoundNumber = 0;
							subRoundHoleNumber = holeNumber;
						}
						else{
							subRoundNumber = 1;
							subRoundHoleNumber = holeNumber - 9;
						}

						int roundHoleNumber = ((caddyPlayerNumber-1) * 9) + (subRoundHoleNumber-1);

						shot.setShotEndLatLong(location.getLatitude(), location.getLongitude());

						if(countDistance) {
							Location startLocation = new Location("");
							startLocation.setLatitude(shot.getShotStartLat());
							startLocation.setLongitude(shot.getShotStartLong());

							Location endLocation = new Location("");
							endLocation.setLatitude(shot.getShotEndLat());
							endLocation.setLongitude(shot.getShotEndLong());

							shot.setYards((int)Math.round(startLocation.distanceTo(endLocation) * 1.09361));
						}

						round.getSubRoundList().get(subRoundNumber).getRoundHoleList().get(roundHoleNumber).addShot(shot);

						shotScore[caddyPlayerNumber][holeNumber]++;
						holeScore[caddyPlayerNumber][holeNumber]++;

						shot = new Shot();
						shot.setShotStartLatLong(location.getLatitude(), location.getLongitude());

						TextView previousShotDistanceText = (TextView) findViewById(R.id.caddyMainSectionPreviousShotText);
						previousShotDistanceText.setText("Previous Shot Distance: " + (int)Math.round(location.distanceTo(shot.getShotStartLocation())* 1.09361) + " yds");

						//\todo Need to pick the index based off how far from the middle of the green and the club distances
						currentMainClubListIndex = 9;

						if(clubListLength - currentMainClubListIndex < 2){
							farRightClubButton.setText(clubs.get(currentMainClubListIndex-2).getClub());
							rightClubButton.setText(clubs.get(currentMainClubListIndex-1).getClub());
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							leftClubButton.setVisibility(View.INVISIBLE);
							farLeftClubButton.setVisibility(View.INVISIBLE);

							farRightClubButton.setVisibility(View.VISIBLE);
							rightClubButton.setVisibility(View.VISIBLE);
						}
						else if(clubListLength - currentMainClubListIndex < 3){
							farRightClubButton.setText(clubs.get(currentMainClubListIndex-2).getClub());
							rightClubButton.setText(clubs.get(currentMainClubListIndex-1).getClub());
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							leftClubButton.setText(clubs.get(currentMainClubListIndex+1).getClub());
							farLeftClubButton.setVisibility(View.INVISIBLE);

							farRightClubButton.setVisibility(View.VISIBLE);
							rightClubButton.setVisibility(View.VISIBLE);
							leftClubButton.setVisibility(View.VISIBLE);
						}
						else if(currentMainClubListIndex == 0){
							farRightClubButton.setVisibility(View.INVISIBLE);
							rightClubButton.setVisibility(View.INVISIBLE);
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
							farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());

							farLeftClubButton.setVisibility(View.VISIBLE);
							leftClubButton.setVisibility(View.VISIBLE);
						}
						else if(currentMainClubListIndex == 1){
							farRightClubButton.setVisibility(View.INVISIBLE);
							rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
							farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());

							rightClubButton.setVisibility(View.VISIBLE);
							farLeftClubButton.setVisibility(View.VISIBLE);
							leftClubButton.setVisibility(View.VISIBLE);
						}
						else {
							farRightClubButton.setText(clubs.get(currentMainClubListIndex - 2).getClub());
							rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
							farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());

							farRightClubButton.setVisibility(View.VISIBLE);
							rightClubButton.setVisibility(View.VISIBLE);
							farLeftClubButton.setVisibility(View.VISIBLE);
							leftClubButton.setVisibility(View.VISIBLE);
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the finish hole Button
	private void CaddyFinishHoleButtonInitializer(){

		finishHoleButton = (Button) findViewById(R.id.caddyFinishHoleButton);

		finishHoleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					finishHoleDialog = new AlertDialog.Builder(StartRound.this);
					// Get the layout inflater
					LayoutInflater inflater = StartRound.this.getLayoutInflater();

					View dialogView = inflater.inflate(R.layout.finishholedialoglayout, null);

					// Inflate and set the layout for the dialog
					// Pass null as the parent view because its going in the dialog layout
					finishHoleDialog.setView(dialogView)
							// Add action buttons
							.setTitle("Add scores")
							.setPositiveButton("Next Hole>", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									if(eighteenHoleRound) {
										if(holeNumber < 18) {
											holeNumber++;
											CaddyScreenResetShot();
											CaddyScreenFrontHoleSwitcher();
										}
										else{
											Toast.makeText(StartRound.this, "This is the last hole", Toast.LENGTH_SHORT).show();
										}
									}
									else{
										if(holeNumber<9){
											holeNumber++;
											CaddyScreenResetShot();
											CaddyScreenFrontHoleSwitcher();
										}
										else{
											Toast.makeText(StartRound.this, "This is the last hole", Toast.LENGTH_SHORT).show();
										}
									}
								}
							})
							.setNeutralButton("Close", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {

								}
							});

					finishHoleTotalScore = (TextView)dialogView.findViewById(R.id.caddyFinishHoleTotalScore);
					finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

					finishHoleShotScore = (TextView)dialogView.findViewById(R.id.caddyFinishHoleShotScore);
					finishHoleShotScore.setText(Integer.toString(shotScore[caddyPlayerNumber][holeNumber]));

					Button plusButton = (Button)dialogView.findViewById(R.id.caddyFinishHoleShotPlusButton);
					Button minusButton = (Button)dialogView.findViewById(R.id.caddyFinishHoleShotMinusButton);

					plusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Increases the active player's score
								shotScore[caddyPlayerNumber][holeNumber]++;
								holeScore[caddyPlayerNumber][holeNumber]++;

								//Displays the increased number
								finishHoleShotScore.setText(Integer.toString(shotScore[caddyPlayerNumber][holeNumber]));
								finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

								//Loads the correct view from the scorecard tab
								setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

								//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								if (holeNumber < 10) {
									if (frontActive) {
										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}
								//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								else {
									if (!frontActive) {
										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}

							} catch (Exception e) {
							}
						}
					});

					minusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Decreases the active player's score if the score is not already 0
								if (shotScore[caddyPlayerNumber][holeNumber] != 0) {
									shotScore[caddyPlayerNumber][holeNumber]--;
									holeScore[caddyPlayerNumber][holeNumber]--;

									finishHoleShotScore.setText(Integer.toString(shotScore[caddyPlayerNumber][holeNumber]));
									finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

									//Loads the correct view from the scorecard tab
									setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

									//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									if (holeNumber < 10) {
										if (frontActive) {
											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[playerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
									//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									else {
										if (!frontActive) {
											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
								}
							} catch (Exception e) {
							}
						}
					});

					finishHolePuttScore = (TextView)dialogView.findViewById(R.id.caddyFinishHolePuttScore);
					finishHolePuttScore.setText(Integer.toString(puttScore[caddyPlayerNumber][holeNumber]));

					plusButton = (Button)dialogView.findViewById(R.id.caddyFinishHolePuttPlusButton);
					minusButton = (Button)dialogView.findViewById(R.id.caddyFinishHolePuttMinusButton);

					plusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Increases the active player's score
								puttScore[caddyPlayerNumber][holeNumber]++;
								holeScore[caddyPlayerNumber][holeNumber]++;

								//Displays the increased number
								finishHolePuttScore.setText(Integer.toString(puttScore[caddyPlayerNumber][holeNumber]));
								finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

								//Loads the correct view from the scorecard tab
								setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

								//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								if (holeNumber < 10) {
									if (frontActive) {

										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}
								//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								else {
									if (!frontActive) {

										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}

							} catch (Exception e) {
							}
						}
					});

					minusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Decreases the active player's score if the score is not already 0
								if (puttScore[caddyPlayerNumber][holeNumber] != 0) {
									puttScore[caddyPlayerNumber][holeNumber]--;
									holeScore[caddyPlayerNumber][holeNumber]--;

									//Displays the decreased number
									finishHolePuttScore.setText(Integer.toString(puttScore[caddyPlayerNumber][holeNumber]));
									finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

									//Loads the correct view from the scorecard tab
									setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

									//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									if (holeNumber < 10) {
										if (frontActive) {

											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
									//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									else {
										if (!frontActive) {

											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
								}
							} catch (Exception e) {
							}
						}
					});

					finishHoleChipScore = (TextView)dialogView.findViewById(R.id.caddyFinishHoleChipScore);
					finishHoleChipScore.setText(Integer.toString(chipScore[caddyPlayerNumber][holeNumber]));

					plusButton = (Button)dialogView.findViewById(R.id.caddyFinishHoleChipPlusButton);
					minusButton = (Button)dialogView.findViewById(R.id.caddyFinishHoleChipMinusButton);

					plusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Increases the active player's score
								chipScore[caddyPlayerNumber][holeNumber]++;
								holeScore[caddyPlayerNumber][holeNumber]++;

								//Displays the increased number
								finishHoleChipScore.setText(Integer.toString(chipScore[caddyPlayerNumber][holeNumber]));
								finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

								//Loads the correct view from the scorecard tab
								setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

								//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								if (holeNumber < 10) {
									if (frontActive) {

										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}
								//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								else {
									if (!frontActive) {

										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}

							} catch (Exception e) {
							}
						}
					});

					minusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Decreases the active player's score if the score is not already 0
								if (chipScore[caddyPlayerNumber][holeNumber] != 0) {
									chipScore[caddyPlayerNumber][holeNumber]--;
									holeScore[caddyPlayerNumber][holeNumber]--;

									//Displays the decreased number
									finishHoleChipScore.setText(Integer.toString(chipScore[caddyPlayerNumber][holeNumber]));
									finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

									//Loads the correct view from the scorecard tab
									setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

									//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									if (holeNumber < 10) {
										if (frontActive) {

											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
									//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									else {
										if (!frontActive) {

											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
								}
							} catch (Exception e) {
							}
						}
					});

					finishHolePenaltyScore = (TextView)dialogView.findViewById(R.id.caddyFinishHolePenaltyScore);
					finishHolePenaltyScore.setText(Integer.toString(penaltyScore[caddyPlayerNumber][holeNumber]));

					plusButton = (Button)dialogView.findViewById(R.id.caddyFinishHolePenaltyPlusButton);
					minusButton = (Button)dialogView.findViewById(R.id.caddyFinishHolePenaltyMinusButton);

					plusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Increases the active player's score
								penaltyScore[caddyPlayerNumber][holeNumber]++;
								holeScore[caddyPlayerNumber][holeNumber]++;

								//Displays the increased number
								finishHolePenaltyScore.setText(Integer.toString(penaltyScore[caddyPlayerNumber][holeNumber]));
								finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

								//Loads the correct view from the scorecard tab
								setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

								//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								if (holeNumber < 10) {
									if (frontActive) {

										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}
								//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
								//If so, the scorecard display is updated to show the change in score
								else {
									if (!frontActive) {

										scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

										updateScorecardTotals(caddyPlayerNumber);
									}
								}

							} catch (Exception e) {
							}
						}
					});

					minusButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								//Sets the vibrate time
								vibe.vibrate(15);

								//Decreases the active player's score if the score is not already 0
								if (penaltyScore[caddyPlayerNumber][holeNumber] != 0) {
									penaltyScore[caddyPlayerNumber][holeNumber]--;
									holeScore[caddyPlayerNumber][holeNumber]--;

									//Displays the decreased number
									finishHolePenaltyScore.setText(Integer.toString(penaltyScore[caddyPlayerNumber][holeNumber]));
									finishHoleTotalScore.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

									//Loads the correct view from the scorecard tab
									setTextViewHoleNumber(caddyPlayerNumber, holeNumber);

									//Checks if the hole is in the front 9 and if the front 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									if (holeNumber < 10) {
										if (frontActive) {

											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
									//Checks if the hole is in the back 9 and if the back 9 is currently displayed.
									//If so, the scorecard display is updated to show the change in score
									else {
										if (!frontActive) {

											if (holeScore[caddyPlayerNumber][holeNumber] == 0)
												scoreEntryScorecard.setText("");
											else
												scoreEntryScorecard.setText(Integer.toString(holeScore[caddyPlayerNumber][holeNumber]));

											updateScorecardTotals(caddyPlayerNumber);
										}
									}
								}
							} catch (Exception e) {
							}
						}
					});

					finishHoleDialog.show();
				} catch (Exception e) {
					Log.d("test", e + "");
				}
			}
		});
	}

	//Initializes the Count Distance Button
	private void CaddyCountDistanceButtonInitializer(){

		countDistanceButton = (Button) findViewById(R.id.caddyCountDistanceButton);

		countDistanceButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

		countDistanceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					if(countDistance) {
						countDistanceButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
						countDistance = false;
					}
					else{
						countDistanceButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
						countDistance = true;
					}

				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the Select Club Button
	private void CaddySelectClubButtonInitializer(){

		selectClubButton = (Button) findViewById(R.id.caddyClubButton);

		selectClubButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					final String[] clubList = new String[clubListLength];
					int x = 0;

					for(Club club : clubs){

						clubList[x] = club.getClub();

						x++;
					}

					clubBuilder = new AlertDialog.Builder(StartRound.this);
					clubBuilder.setTitle("Select Club")
							.setItems(clubList, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									currentMainClubListIndex = which;

									Club tempClub = new Club();
									tempClub.setID(clubs.get(currentMainClubListIndex).getID());
									shot.setClubID(tempClub);

									if(clubList.length - currentMainClubListIndex < 2){
										farRightClubButton.setText(clubs.get(currentMainClubListIndex-2).getClub());
										rightClubButton.setText(clubs.get(currentMainClubListIndex-1).getClub());
										middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
										leftClubButton.setVisibility(View.INVISIBLE);
										farLeftClubButton.setVisibility(View.INVISIBLE);

										farRightClubButton.setVisibility(View.VISIBLE);
										rightClubButton.setVisibility(View.VISIBLE);
									}
									else if(clubList.length - currentMainClubListIndex < 3){
										farRightClubButton.setText(clubs.get(currentMainClubListIndex-2).getClub());
										rightClubButton.setText(clubs.get(currentMainClubListIndex-1).getClub());
										middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
										leftClubButton.setText(clubs.get(currentMainClubListIndex+1).getClub());
										farLeftClubButton.setVisibility(View.INVISIBLE);

										farRightClubButton.setVisibility(View.VISIBLE);
										rightClubButton.setVisibility(View.VISIBLE);
										leftClubButton.setVisibility(View.VISIBLE);
									}
									else if(currentMainClubListIndex == 0){
										farRightClubButton.setVisibility(View.INVISIBLE);
										rightClubButton.setVisibility(View.INVISIBLE);
										middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
										leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
										farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());

										farLeftClubButton.setVisibility(View.VISIBLE);
										leftClubButton.setVisibility(View.VISIBLE);
									}
									else if(currentMainClubListIndex == 1){
										farRightClubButton.setVisibility(View.INVISIBLE);
										rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());
										middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
										leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
										farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());

										rightClubButton.setVisibility(View.VISIBLE);
										farLeftClubButton.setVisibility(View.VISIBLE);
										leftClubButton.setVisibility(View.VISIBLE);
									}
									else {
										farRightClubButton.setText(clubs.get(currentMainClubListIndex - 2).getClub());
										rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());
										middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
										leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
										farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());

										farRightClubButton.setVisibility(View.VISIBLE);
										rightClubButton.setVisibility(View.VISIBLE);
										farLeftClubButton.setVisibility(View.VISIBLE);
										leftClubButton.setVisibility(View.VISIBLE);
									}

								}
							});
					clubBuilder.show();

				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the Select Result Button
	private void CaddySelectResultButtonInitializer(){

		selectResultButton = (Button) findViewById(R.id.caddyResultButton);

		selectResultButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					final List<ShotType> resultTypes = new ArrayList<ShotType>();

					final List<ShotType> shotTypes = shotTypeDAO.readListShotTypesIsPre(false);

					ShotType penaltyType = new ShotType();
					penaltyType.setType("Penalty Shot");

					shotTypes.add(penaltyType);

					ShotType dontrecordType = new ShotType();
					dontrecordType.setType("Do Not Record");

					shotTypes.add(dontrecordType);

					final int resultLength = shotTypes.size();

					final String[] resultList = new String[resultLength];
					int x = 0;

					for(ShotType shotType : shotTypes){

						resultList[x] = shotType.getType();

						x++;
					}

					final List<ShotType> addedShotTypes = shot.getShotTypePostList();
					final boolean[] addedShotTypesBoolean = new boolean[resultLength];
					x = 0;

					for(ShotType shotType : shotTypes){
						for(ShotType addedShotType : addedShotTypes){
							if(addedShotType.getType().equals(shotType.getType())){
								addedShotTypesBoolean[x] = true;
							}
						}
						x++;
					}

					for(ShotType addedShotType : addedShotTypes){
						resultTypes.add(addedShotType);
					}

					resultBuilder = new AlertDialog.Builder(StartRound.this);
					resultBuilder.setTitle("Select Results")
						.setMultiChoiceItems(resultList, addedShotTypesBoolean,
								new DialogInterface.OnMultiChoiceClickListener() {
									public void onClick(DialogInterface dialog, int which, boolean isChecked) {
										if (isChecked) {
											// If the user checked the item, add it to the selected items
											resultTypes.add(shotTypes.get(which));
										} else if (resultTypes.contains(shotTypes.get(which))) {
											// Else, if the item is already in the array, remove it
											resultTypes.remove(shotTypes.get(which));
										}

									}
								}
						)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								shot.clearShotTypesPost();

								for (ShotType resultType : resultTypes) {

									if (resultType.getType().equals("Penalty Shot")) {

										penaltyScore[caddyPlayerNumber][holeNumber]++;
										holeScore[caddyPlayerNumber][holeNumber]++;
										CaddyScreenResetShot();
									} else {
										if (resultType.getType().equals("Do Not Record")) {

											CaddyScreenResetShot();
										} else {

											shot.addShotTypePost(resultType);
										}
									}
								}
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

							}
						});

					resultBuilder.show();
				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the Select Lie Button
	private void CaddySelectLieButtonInitializer(){

		selectLieButton = (Button) findViewById(R.id.caddyLieButton);

		selectLieButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					final List<ShotType> lieTypes = new ArrayList<ShotType>();

					final List<ShotType> shotTypes = shotTypeDAO.readListShotTypesIsPre(true);

					int lieLength = shotTypes.size();

					final String[] lieList = new String[lieLength];
					int x = 0;

					for(ShotType shotType : shotTypes){

						lieList[x] = shotType.getType();

						x++;
					}

					final List<ShotType> addedShotTypes = shot.getShotTypePreList();
					final boolean[] addedShotTypesBoolean = new boolean[lieLength];
					x = 0;

					for(ShotType shotType : shotTypes){
						for(ShotType addedShotType : addedShotTypes){
							if(addedShotType.getType().equals(shotType.getType())){
								addedShotTypesBoolean[x] = true;
							}
						}
						x++;
					}

					for(ShotType addedShotType : addedShotTypes){
						lieTypes.add(addedShotType);
					}

					lieBuilder = new AlertDialog.Builder(StartRound.this);
					lieBuilder.setTitle("Select Lie")
							.setMultiChoiceItems(lieList, addedShotTypesBoolean,
									new DialogInterface.OnMultiChoiceClickListener() {
										public void onClick(DialogInterface dialog, int which, boolean isChecked) {
											if (isChecked) {
												// If the user checked the item, add it to the selected items
												lieTypes.add(shotTypes.get(which));
											} else if (lieTypes.contains(shotTypes.get(which))) {
												// Else, if the item is already in the array, remove it
												lieTypes.remove(shotTypes.get(which));
											}
										}
									}
							)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {

									shot.clearShotTypesPre();

									for (ShotType lieType : lieTypes) {

										shot.addShotTypePre(lieType);
									}

								}
							})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {

								}
							});

					lieBuilder.show();
				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the Left Club Button
	private void CaddyLeftClubButtonInitializer(){



		leftClubButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					int toLocation[] = new int[2];
					int fromLocation[] = new int[2];

					int toSize[] = new int[2];
					int fromSize[] = new int[2];

					int xMove;
					int yMove;

					float xChange;
					float yChange;

					currentMainClubListIndex += 1;



					if(clubListLength - currentMainClubListIndex >= 2){

						animationSet = new AnimationSet(true);
						animationSet.setFillAfter(true);

						leftClubButton.getLocationInWindow(toLocation);
						farLeftClubButton.getLocationInWindow(fromLocation);

						xMove = toLocation[0]-fromLocation[0];
						yMove = toLocation[1]-fromLocation[1];

						toSize[0] = leftClubButton.getWidth();
						toSize[1] = leftClubButton.getHeight();
						fromSize[0] = farLeftClubButton.getWidth();
						fromSize[1] = farLeftClubButton.getHeight();

						xChange = (float)toSize[0] / (float)fromSize[0];
						yChange = (float)toSize[1] / (float)fromSize[1];

						xMove = (int)((float)xMove / xChange);
						yMove = (int)((float)yMove / yChange);

						translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
						translateAnimation.setDuration(1000);
						translateAnimation.setFillAfter(true);
						animationSet.addAnimation(translateAnimation);

						scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
						scaleAnimation.setDuration(1000);
						animationSet.addAnimation(scaleAnimation);

						animationSet.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								farLeftClubButton.clearAnimation();
								if(clubListLength - currentMainClubListIndex >= 3) {
									farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());
								}
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationStart(Animation animation) {

							}
						});

						farLeftClubButton.startAnimation(animationSet);
					}



					if(clubListLength - currentMainClubListIndex >= 1) {
						animationSet = new AnimationSet(true);
						animationSet.setFillAfter(true);

						middleClubButton.getLocationInWindow(toLocation);
						leftClubButton.getLocationInWindow(fromLocation);

						xMove = toLocation[0] - fromLocation[0];
						yMove = toLocation[1] - fromLocation[1];

						toSize[0] = middleClubButton.getWidth();
						toSize[1] = middleClubButton.getHeight();
						fromSize[0] = leftClubButton.getWidth();
						fromSize[1] = leftClubButton.getHeight();

						xChange = (float) toSize[0] / (float) fromSize[0];
						yChange = (float) toSize[1] / (float) fromSize[1];

						xMove = (int) ((float) xMove / xChange);
						yMove = (int) ((float) yMove / yChange);

						translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
						translateAnimation.setDuration(1000);
						translateAnimation.setFillAfter(true);
						animationSet.addAnimation(translateAnimation);

						scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
						scaleAnimation.setDuration(1000);
						animationSet.addAnimation(scaleAnimation);

						animationSet.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								leftClubButton.clearAnimation();
								if(clubListLength - currentMainClubListIndex >= 2) {
									leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
								}
								else{
									leftClubButton.setVisibility(View.INVISIBLE);
								}
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationStart(Animation animation) {

							}
						});

						leftClubButton.startAnimation(animationSet);
					}




					animationSet = new AnimationSet(true);
					animationSet.setFillAfter(true);

					rightClubButton.getLocationInWindow(toLocation);
					middleClubButton.getLocationInWindow(fromLocation);

					xMove = toLocation[0]-fromLocation[0];
					yMove = toLocation[1]-fromLocation[1];

					toSize[0] = rightClubButton.getWidth();
					toSize[1] = rightClubButton.getHeight();
					fromSize[0] = middleClubButton.getWidth();
					fromSize[1] = middleClubButton.getHeight();

					xChange = (float)toSize[0] / (float)fromSize[0];
					yChange = (float)toSize[1] / (float)fromSize[1];

					xMove = (int)((float)xMove / xChange);
					yMove = (int)((float)yMove / yChange);

					translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
					translateAnimation.setDuration(1000);
					translateAnimation.setFillAfter(true);
					animationSet.addAnimation(translateAnimation);

					scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
					scaleAnimation.setDuration(1000);
					animationSet.addAnimation(scaleAnimation);

					animationSet.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationEnd(Animation animation) {
							middleClubButton.clearAnimation();
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							if(currentMainClubListIndex - 1 == 0){
								rightClubButton.setVisibility(View.VISIBLE);
								rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());
							}
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationStart(Animation animation) {

						}
					});

					middleClubButton.startAnimation(animationSet);





					if(currentMainClubListIndex - 1 != 0) {

						animationSet = new AnimationSet(true);
						animationSet.setFillAfter(true);

						farRightClubButton.getLocationInWindow(toLocation);
						rightClubButton.getLocationInWindow(fromLocation);

						xMove = toLocation[0] - fromLocation[0];
						yMove = toLocation[1] - fromLocation[1];

						toSize[0] = farRightClubButton.getWidth();
						toSize[1] = farRightClubButton.getHeight();
						fromSize[0] = rightClubButton.getWidth();
						fromSize[1] = rightClubButton.getHeight();

						xChange = (float) toSize[0] / (float) fromSize[0];
						yChange = (float) toSize[1] / (float) fromSize[1];

						xMove = (int) ((float) xMove / xChange);
						yMove = (int) ((float) yMove / yChange);

						translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
						translateAnimation.setDuration(1000);
						translateAnimation.setFillAfter(true);
						animationSet.addAnimation(translateAnimation);

						scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
						scaleAnimation.setDuration(1000);
						animationSet.addAnimation(scaleAnimation);

						animationSet.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								rightClubButton.clearAnimation();
								rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());

								farRightClubButton.setText(clubs.get(currentMainClubListIndex - 2).getClub());
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationStart(Animation animation) {
								//farRightClubButton.clearAnimation();
							}
						});
						rightClubButton.startAnimation(animationSet);
					}

					//\todo implement the left club button
				} catch (Exception e) {
				}
			}
		});
	}

	//Initializes the Right Club Button
	private void CaddyRightClubButtonInitializer(){



		rightClubButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);

					int toLocation[] = new int[2];
					int fromLocation[] = new int[2];

					int toSize[] = new int[2];
					int fromSize[] = new int[2];

					int xMove;
					int yMove;

					float xChange;
					float yChange;

					currentMainClubListIndex -= 1;


					if(currentMainClubListIndex + 1 != clubs.size() - 1) {

						animationSet = new AnimationSet(true);
						animationSet.setFillAfter(true);

						farLeftClubButton.getLocationInWindow(toLocation);
						leftClubButton.getLocationInWindow(fromLocation);

						xMove = toLocation[0] - fromLocation[0];
						yMove = toLocation[1] - fromLocation[1];

						toSize[0] = farLeftClubButton.getWidth();
						toSize[1] = farLeftClubButton.getHeight();
						fromSize[0] = leftClubButton.getWidth();
						fromSize[1] = leftClubButton.getHeight();

						xChange = (float) toSize[0] / (float) fromSize[0];
						yChange = (float) toSize[1] / (float) fromSize[1];

						xMove = (int) ((float) xMove / xChange);
						yMove = (int) ((float) yMove / yChange);

						translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
						translateAnimation.setDuration(1000);
						translateAnimation.setFillAfter(true);
						animationSet.addAnimation(translateAnimation);

						scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
						scaleAnimation.setDuration(1000);
						animationSet.addAnimation(scaleAnimation);

						animationSet.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								leftClubButton.clearAnimation();
								leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());

								farLeftClubButton.setText(clubs.get(currentMainClubListIndex + 2).getClub());
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationStart(Animation animation) {
								//farRightClubButton.clearAnimation();
							}
						});
						leftClubButton.startAnimation(animationSet);
					}






					animationSet = new AnimationSet(true);
					animationSet.setFillAfter(true);

					leftClubButton.getLocationInWindow(toLocation);
					middleClubButton.getLocationInWindow(fromLocation);

					xMove = toLocation[0]-fromLocation[0];
					yMove = toLocation[1]-fromLocation[1];

					toSize[0] = leftClubButton.getWidth();
					toSize[1] = leftClubButton.getHeight();
					fromSize[0] = middleClubButton.getWidth();
					fromSize[1] = middleClubButton.getHeight();

					xChange = (float)toSize[0] / (float)fromSize[0];
					yChange = (float)toSize[1] / (float)fromSize[1];

					xMove = (int)((float)xMove / xChange);
					yMove = (int)((float)yMove / yChange);

					translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
					translateAnimation.setDuration(1000);
					translateAnimation.setFillAfter(true);
					animationSet.addAnimation(translateAnimation);

					scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
					scaleAnimation.setDuration(1000);
					animationSet.addAnimation(scaleAnimation);

					animationSet.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationEnd(Animation animation) {
							middleClubButton.clearAnimation();
							middleClubButton.setText(clubs.get(currentMainClubListIndex).getClub());
							if (currentMainClubListIndex + 1 == clubs.size() - 1) {
								leftClubButton.setVisibility(View.VISIBLE);
								leftClubButton.setText(clubs.get(currentMainClubListIndex + 1).getClub());
							}
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationStart(Animation animation) {

						}
					});

					middleClubButton.startAnimation(animationSet);



					if(currentMainClubListIndex >= 0) {

						animationSet = new AnimationSet(true);
						animationSet.setFillAfter(true);

						middleClubButton.getLocationInWindow(toLocation);
						rightClubButton.getLocationInWindow(fromLocation);

						xMove = toLocation[0] - fromLocation[0];
						yMove = toLocation[1] - fromLocation[1];

						toSize[0] = middleClubButton.getWidth();
						toSize[1] = middleClubButton.getHeight();
						fromSize[0] = rightClubButton.getWidth();
						fromSize[1] = rightClubButton.getHeight();

						xChange = (float) toSize[0] / (float) fromSize[0];
						yChange = (float) toSize[1] / (float) fromSize[1];

						xMove = (int) ((float) xMove / xChange);
						yMove = (int) ((float) yMove / yChange);

						translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
						translateAnimation.setDuration(1000);
						translateAnimation.setFillAfter(true);
						animationSet.addAnimation(translateAnimation);

						scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
						scaleAnimation.setDuration(1000);
						animationSet.addAnimation(scaleAnimation);

						animationSet.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								rightClubButton.clearAnimation();
								if(currentMainClubListIndex >= 1) {
									rightClubButton.setText(clubs.get(currentMainClubListIndex - 1).getClub());
								}
								else{
									rightClubButton.setVisibility(View.INVISIBLE);
								}
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationStart(Animation animation) {

							}
						});

						rightClubButton.startAnimation(animationSet);
					}



					if(currentMainClubListIndex >= 1) {

						animationSet = new AnimationSet(true);
						animationSet.setFillAfter(true);

						rightClubButton.getLocationInWindow(toLocation);
						farRightClubButton.getLocationInWindow(fromLocation);

						xMove = toLocation[0] - fromLocation[0];
						yMove = toLocation[1] - fromLocation[1];

						toSize[0] = rightClubButton.getWidth();
						toSize[1] = rightClubButton.getHeight();
						fromSize[0] = farRightClubButton.getWidth();
						fromSize[1] = farRightClubButton.getHeight();

						xChange = (float) toSize[0] / (float) fromSize[0];
						yChange = (float) toSize[1] / (float) fromSize[1];

						xMove = (int) ((float) xMove / xChange);
						yMove = (int) ((float) yMove / yChange);

						translateAnimation = new TranslateAnimation(0, xMove, 0, yMove);
						translateAnimation.setDuration(1000);
						translateAnimation.setFillAfter(true);
						animationSet.addAnimation(translateAnimation);

						scaleAnimation = new ScaleAnimation(1.0f, xChange, 1.0f, yChange, 0.0f, 0.0f);
						scaleAnimation.setDuration(1000);
						animationSet.addAnimation(scaleAnimation);

						animationSet.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								farRightClubButton.clearAnimation();
								if(currentMainClubListIndex >= 2) {
									farRightClubButton.setText(clubs.get(currentMainClubListIndex - 2).getClub());
								}
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationStart(Animation animation) {

							}
						});
						farRightClubButton.startAnimation(animationSet);
					}

					//\todo implement the right club button
				} catch (Exception e) {
				}
			}
		});
	}

	private TranslateAnimation translateAnimation = null;
	private AnimationSet animationSet = null;
	private ScaleAnimation scaleAnimation = null;

	//Initializes the Middle Club Button
	private void CaddyMiddleClubButtonInitializer(){





		middleClubButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//Sets the vibrate time
					vibe.vibrate(15);


					//\todo implement the middle club button
				} catch (Exception e) {
				}
			}
		});
	}

	//Caddy Screen Reset
	private void CaddyScreenResetShot(){
		shot = new Shot();

		TextView previousShotTitleText = (TextView) findViewById(R.id.caddyBottomSectionTitleText);
		previousShotTitleText.setText("Next Shot");

		TextView previousShotDistanceText = (TextView) findViewById(R.id.caddyMainSectionPreviousShotText);
		previousShotDistanceText.setText("");

		recordLocationButton.setText("Start Shot");

		initialLocationRecorded = false;
	}

	//Caddy Screen switching from front to back
	private void CaddyScreenFrontHoleSwitcher(){

		//Set the hole number to the current hole
		caddyHoleNumberText.setText("Hole " + holeNumberText[holeNumber]);

		//\todo handle if only 9 holes
		if(holeNumber<10) {
			String subCourseName = subCourses.get(0).getName();
			caddyTitleBarText.setText(courseName + " - " + subCourseName);
		}
		else{
			String subCourseName = subCourses.get(1).getName();
			caddyTitleBarText.setText(courseName + " - " + subCourseName);
		}
	}

	//\todo Add bogey/birdie images for scorecard
}

