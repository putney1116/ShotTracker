package com.example.android.ShotTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ShotTracker.db.CourseDAO;

public class EnterPlayers extends Activity{
	
	private TextView[] playerNameText = new TextView[4];
	private EditText[] playerNameField = new EditText[4];;
	
	private Button startRoundButton;

	private long courseID;
	
	private int radioButtonNumber;
	
	private Vibrator vibe;

    private CourseDAO courseDAO = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enterplayers);
				
		//Loads the file name from the previous activity
		Intent myIntent = getIntent();
		courseID = myIntent.getLongExtra("Course ID",-1);

        Log.e("Test", "course ID = " + courseID);
		
		//Initialize Vibrate
    	vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		
		//Initializes the radio buttons which select the number of players
		selectNumberOfPlayers();
		
		//Initializes the button that starts the round
		startRound();
		
		//Displays the course name at the top of the screen
		setCourseName();
		
		//Sets the hints in the editable text boxes
		setHints();
	}
	
	private void setHints(){
		//Opens the shared preferences file with the saved settings
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		//Sets the hints to the defaults set by the settings page
		EditText playerName = (EditText)findViewById(R.id.player1);
		playerName.setHint(preferences.getString("Player 1 Name", "Me"));
		
		playerName = (EditText)findViewById(R.id.player2);
		playerName.setHint(preferences.getString("Player 2 Name", "Player 2"));
		
		playerName = (EditText)findViewById(R.id.player3);
		playerName.setHint(preferences.getString("Player 3 Name", "Player 3"));
		
		playerName = (EditText)findViewById(R.id.player4);
		playerName.setHint(preferences.getString("Player 4 Name", "Player 4"));
	}
	
	//Displays the course name at the top of the screen
	private void setCourseName(){
		courseDAO = new CourseDAO(this);

        String courseName = courseDAO.readCourseNamefromID(courseID);
		
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
					myIntent.putExtra("Course ID", courseID);
					myIntent.putExtra("Players", radioButtonNumber);
					
					List<String> playerList = new ArrayList<String>();
				
					//The player names are added to the player list
					//They are either what the user entered or the default player names if left blank
					for(int x=0;x<radioButtonNumber;x++){
						if(playerNameField[x].getText().toString().equals("")){
							//Checks to make sure there are no duplicate names
							if(playerList.contains(playerNameField[x].getHint().toString())){
								Toast.makeText(getBaseContext(), "Cannot have duplicate names", Toast.LENGTH_LONG).show();
								break;
							}
							else
								playerList.add(playerNameField[x].getHint().toString());
						}
						else{
							//Checks to make sure there are no duplicate names
							if(playerList.contains(playerNameField[x].getText().toString())){
								Toast.makeText(getBaseContext(), "Cannot have duplicate names", Toast.LENGTH_LONG).show();
								break;
							}
							else
								playerList.add(playerNameField[x].getText().toString());
						}
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
		playerNameField[0] = (EditText)findViewById(R.id.player1);
		playerNameText[1] = (TextView)findViewById(R.id.player2text);
		playerNameField[1] = (EditText)findViewById(R.id.player2);
		playerNameText[2] = (TextView)findViewById(R.id.player3text);
		playerNameField[2] = (EditText)findViewById(R.id.player3);
		playerNameText[3] = (TextView)findViewById(R.id.player4text);
		playerNameField[3] = (EditText)findViewById(R.id.player4);
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
						playerNameField[x].setVisibility(View.GONE);
					}
					radioButtonNumber = 1;
				} else if (checkedRadioButtonId == R.id.radioButton2) {
					playerNameText[1].setVisibility(View.VISIBLE);
					playerNameField[1].setVisibility(View.VISIBLE);
					for(int x=2;x<=3;x++){
						playerNameText[x].setVisibility(View.GONE);
						playerNameField[x].setVisibility(View.GONE);
					}
					radioButtonNumber = 2;
				} else if (checkedRadioButtonId == R.id.radioButton3) {
					for(int x=1;x<=2;x++){
						playerNameText[x].setVisibility(View.VISIBLE);
						playerNameField[x].setVisibility(View.VISIBLE);
					}
					playerNameText[3].setVisibility(View.GONE);
					playerNameField[3].setVisibility(View.GONE);
					radioButtonNumber = 3;
				} else if (checkedRadioButtonId == R.id.radioButton4) {
					for(int x=1;x<=3;x++){
						playerNameText[x].setVisibility(View.VISIBLE);
						playerNameField[x].setVisibility(View.VISIBLE);
					}
					radioButtonNumber = 4;
				} else {
					for(int x=0;x<=3;x++){
						playerNameText[x].setVisibility(View.GONE);
						playerNameField[x].setVisibility(View.GONE);
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
