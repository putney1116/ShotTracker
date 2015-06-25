package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ShotTracker.db.BagDAO;
import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.SubCourse;

import java.util.ArrayList;
import java.util.List;

public class EnterPlayers extends Activity{

    private TextView[] playerNameText = new TextView[4];
    private Spinner[] playerNameSpinner = new Spinner[4];
	private String[] playerNames = new String[4];

	private Button startRoundButton;

	private long front9SubCourseID;
    private long back9SubCourseID;
	
	private int radioButtonNumber;
	
	private Vibrator vibe;

    private CourseDAO courseDAO = null;
    private SubCourseDAO subCourseDAO = null;

    private AlertDialog.Builder builder;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enterplayers);
				
		//Loads the file name from the previous activity
		Intent myIntent = getIntent();
		front9SubCourseID = myIntent.getLongExtra("Front 9 SubCourseID", -1);
        back9SubCourseID = myIntent.getLongExtra("Back 9 SubCourseID", -1);
		
		//Initialize Vibrate
    	vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		
		//Initializes the radio buttons which select the number of players
		selectNumberOfPlayers();

        //Set up adding a new player
        addPlayer();

        //Sets the spinners for the drop down players
        setPlayerSpinners(false);

        //Displays the course name at the top of the screen
        setCourseName();

		//Initializes the button that starts the round
		startRound();
		

	}

    private void setPlayerSpinners(boolean onAddPlayer){
        PlayerDAO playerDAO = new PlayerDAO(this);
        final List<String> players = playerDAO.readListofPlayerNameswDefaultFirst();
		Log.e("Test","Lengh of List: " + players.size());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EnterPlayers.this, android.R.layout.simple_spinner_item, players);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //player 1
        Spinner spinner1 = (Spinner) findViewById(R.id.player1);
        spinner1.setAdapter(adapter);

		if(onAddPlayer) {
			spinner1.setSelection(adapter.getPosition(playerNames[0]));
		}

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                playerNames[0] = players.get(pos);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //player 2
        Spinner spinner2 = (Spinner) findViewById(R.id.player2);
        spinner2.setAdapter(adapter);

		if(onAddPlayer) {
			spinner2.setSelection(adapter.getPosition(playerNames[1]));
		}

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                playerNames[1] = players.get(pos);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //player 3
        Spinner spinner3 = (Spinner) findViewById(R.id.player3);
        spinner3.setAdapter(adapter);

		if(onAddPlayer) {
			spinner3.setSelection(adapter.getPosition(playerNames[2]));
		}

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                playerNames[2] = players.get(pos);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //player 4
        Spinner spinner4 = (Spinner) findViewById(R.id.player4);
        spinner4.setAdapter(adapter);

		if(onAddPlayer) {
			spinner4.setSelection(adapter.getPosition(playerNames[3]));
		}

        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                playerNames[3] = players.get(pos);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    private void addPlayer(){
        Button savePlayerButton = (Button)findViewById(R.id.addplayerbutton);

        savePlayerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText input = (EditText)findViewById(R.id.addplayername);
                    //Make sure there is text in the field
                    if(input.getText().toString().equals("")){
                        //pop up text box
                        CharSequence text = "Please enter a name.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(EnterPlayers.this, text, duration);
                        toast.show();

                    } else {
                        Player player = new Player();
                        player.setName(input.getText().toString());
						player.setUserDefault(false);
						//\todo make UserDefault "Not Nullable"
                        //Try uploading to the DB
                        PlayerDAO playerDAO = new PlayerDAO(EnterPlayers.this);
                        BagDAO bagDAO = new BagDAO(EnterPlayers.this);
						long pID = -1;
                        if (playerDAO.readIDFromName(player.getName()) > -1) {
							CharSequence text = "Name already exists. Please enter a unique name.";
							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(EnterPlayers.this, text, duration);
							toast.show();

							input.setText("");

                        } else {
							pID = playerDAO.create(player);
							// Add default clubs to the new players bag
							player.setID(pID);
							bagDAO.createDefaultBag(player);
							input.setText("");
							setPlayerSpinners(true);
                        }
                    }
                } catch (Exception e) {
					Log.e("TEST","" + e);
                }
            }
        });
    }

	//Displays the course name at the top of the screen
	private void setCourseName(){
		courseDAO = new CourseDAO(this);
        subCourseDAO = new SubCourseDAO(this);

        SubCourse front9SubCourse = subCourseDAO.readSubCoursefromID(front9SubCourseID);

        String courseName = courseDAO.readCourseNameFromID(front9SubCourse.getCourseID());
		
		//Displays the course name at the top of the screen
		TextView courseNameText = (TextView)findViewById(R.id.coursename);
		courseNameText.setText(courseName);
	}
	
	//Initializes the button that starts the round
	private void startRound(){
		startRoundButton = (Button)findViewById(R.id.startroundactivitybutton);
		
		startRoundButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					//Sets the vibrate time
					vibe.vibrate(15);
					
					//Calls the activity that is the main screen during play
					//The course file name and number of players are passed along
					Intent myIntent = new Intent(v.getContext(), StartRound.class);
                    myIntent.putExtra("Front 9 SubCourseID", front9SubCourseID);
                    myIntent.putExtra("Back 9 SubCourseID", back9SubCourseID);
					myIntent.putExtra("Players", radioButtonNumber);
					
					List<String> playerList = new ArrayList<String>();
				
					//The player names are added to the player list
					//They are either what the user entered or the default player names if left blank
					for(int x=0;x<radioButtonNumber;x++){
						playerList.add(playerNames[x]);
					}
					
					//The player names are passed to the next activity.
					for(int x=0;x<radioButtonNumber;x++)
						myIntent.putExtra("Player"+x,playerList.get(x));
					
					//The activity is started
			        startActivity(myIntent);  
			         
			        //The present activity is closed
			        finish();
				}catch(Exception e) {
				}
			}
    	});
	}
	
	//Displays the text fields based on the number of players selected
	private void selectNumberOfPlayers(){
		RadioGroup radiogroup = (RadioGroup)findViewById(R.id.radiobuttons);
		
		playerNameText[0] = (TextView)findViewById(R.id.player1text);
		playerNameSpinner[0] = (Spinner)findViewById(R.id.player1);
		playerNameText[1] = (TextView)findViewById(R.id.player2text);
		playerNameSpinner[1] = (Spinner)findViewById(R.id.player2);
		playerNameText[2] = (TextView)findViewById(R.id.player3text);
		playerNameSpinner[2] = (Spinner)findViewById(R.id.player3);
		playerNameText[3] = (TextView)findViewById(R.id.player4text);
		playerNameSpinner[3] = (Spinner)findViewById(R.id.player4);
		radioButtonNumber = 1;
		
		//Called when a radio button is selected
		radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
		    public void onCheckedChanged(RadioGroup rGroup, int checkedId)
		    {
		    	//Sets the vibrate time
				vibe.vibrate(15);
				
		    	int checkedRadioButtonId = rGroup.getCheckedRadioButtonId();
				if (checkedRadioButtonId == R.id.radioButton1) {
					for(int x=1;x<=3;x++){
						playerNameText[x].setVisibility(View.GONE);
						playerNameSpinner[x].setVisibility(View.GONE);
					}
					radioButtonNumber = 1;
				} else if (checkedRadioButtonId == R.id.radioButton2) {
					playerNameText[1].setVisibility(View.VISIBLE);
					playerNameSpinner[1].setVisibility(View.VISIBLE);
					for(int x=2;x<=3;x++){
						playerNameText[x].setVisibility(View.GONE);
						playerNameSpinner[x].setVisibility(View.GONE);
					}
					radioButtonNumber = 2;
				} else if (checkedRadioButtonId == R.id.radioButton3) {
					for(int x=1;x<=2;x++){
						playerNameText[x].setVisibility(View.VISIBLE);
						playerNameSpinner[x].setVisibility(View.VISIBLE);
					}
					playerNameText[3].setVisibility(View.GONE);
					playerNameSpinner[3].setVisibility(View.GONE);
					radioButtonNumber = 3;
				} else if (checkedRadioButtonId == R.id.radioButton4) {
					for(int x=1;x<=3;x++){
						playerNameText[x].setVisibility(View.VISIBLE);
						playerNameSpinner[x].setVisibility(View.VISIBLE);
					}
					radioButtonNumber = 4;
				} else {
					for(int x=0;x<=3;x++){
						playerNameText[x].setVisibility(View.GONE);
						playerNameSpinner[x].setVisibility(View.GONE);
					}
					radioButtonNumber = 0;
				}
		    }
		});
	}
	
	//Closes the activity and the display returns to the home screen if the back button is pressed
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			//Sets the vibrate time
			vibe.vibrate(15);
			
			finish();
	        	
	        return true;
		}
	    return super.onKeyDown(keyCode, event);
	}
}
