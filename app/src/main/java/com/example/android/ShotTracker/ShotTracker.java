package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.android.ShotTracker.db.DataBaseHelper;

public class ShotTracker extends Activity{
		
	private AlertDialog.Builder builder;
	
	private Vibrator vibe;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.homescreen);

		//Initializes the database
        DataBaseHelper dbHelper;
        SQLiteDatabase db = null;

        dbHelper = new DataBaseHelper(this);
        dbHelper.initializeDataBase();
    	
    	//Calls the method that initializes the buttons on the homescreen
    	initializeHomescreen(); 
    	
    	//Builds the dialog that is displayed when the back button is pressed
    	buildDialog();
    }
    
    private void initializeHomescreen(){
    	//Initializes the button that calls the activity to display the list of courses
    	Button startRoundButton = (Button)findViewById(R.id.startRoundButton);
    	
    	//Initialize Vibrate
    	vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    	
    	startRoundButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Sets the vibrate time
				vibe.vibrate(15);
				
				try{
					Intent myIntent = new Intent(v.getContext(), StartRoundList.class);
	                startActivity(myIntent);
					
				}catch(Exception e) {
				}
			}
    	});
    	
    	//Initializes the button that calls the activity to display the list of past rounds
    	Button pastRoundButton = (Button)findViewById(R.id.viewPastRoundButton);
    	
    	pastRoundButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Sets the vibrate time
				vibe.vibrate(15);
				
				try{
					Intent myIntent = new Intent(v.getContext(), PastRoundList.class);
	                startActivity(myIntent);
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
    	});
    	
    	//Initializes the button that calls the activity to display the playing statistics
    	Button statisticsButton = (Button)findViewById(R.id.statisticsButton);
    	
    	statisticsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Sets the vibrate time
				vibe.vibrate(15);
				
				try{
					Intent myIntent = new Intent(v.getContext(), Statistics.class);
	                startActivity(myIntent);
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
    	});
    	
    	//Initializes the button that calls the activity to display the course catalog
    	Button courseCatalogButton = (Button)findViewById(R.id.courseCatalogButton);
    	
    	courseCatalogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Sets the vibrate time
				vibe.vibrate(15);
				
				try{
					Intent myIntent = new Intent(v.getContext(), CourseCatalogList.class);
	                startActivity(myIntent);
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
    	});

        //Initializes the button that calls the activity to add a course
        Button addCourseButton = (Button)findViewById(R.id.addCourseButton);

        addCourseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sets the vibrate time
                vibe.vibrate(15);

                try{
                    Intent myIntent = new Intent(v.getContext(), AddCourse.class);
                    startActivity(myIntent);

                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    	
    	//Initializes the button that calls the activity to display the settings
    	Button settingsButton = (Button)findViewById(R.id.settingsButton);
    	
    	settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Sets the vibrate time
				vibe.vibrate(15);
				
				try{
					Intent myIntent = new Intent(v.getContext(), Settings.class);
	                startActivity(myIntent);
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
    	});

    	//Initializes the button that exits the application
    	Button exitButton = (Button)findViewById(R.id.exitButton);
    	
    	exitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Sets the vibrate time
				vibe.vibrate(15);
				
				try{
					finish();
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
    	});
    }
    
    //If the back button is pressed, the dialog to quit the application is called
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	//Sets the vibrate time
			vibe.vibrate(15);
			
			builder.show();
        	
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    //The dialog confirms if the user would like to exit the application or not
    private void buildDialog(){
		builder = new AlertDialog.Builder(ShotTracker.this);
    	builder.setMessage("Are you sure you would like to exit?");
    	builder.setCancelable(true);
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {     	        	   
    			//Sets the vibrate time
    			vibe.vibrate(15);
    			
    			finish();
    		}
    	});
    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			//Sets the vibrate time
    			vibe.vibrate(15);
    			
    			dialog.cancel();
    	    }
    	});
	}
}