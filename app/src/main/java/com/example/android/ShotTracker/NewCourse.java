package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Vibrator;

public class NewCourse extends Activity{
	
	private AlertDialog.Builder builder;
	
	private Vibrator vibe;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.homescreen);
    	
    	//Calls the method that initializes the buttons on the homescreen
    	//initializeHomescreen(); 
    	
    	//Builds the dialog that is displayed when the back button is pressed
    	//buildDialog();
    }
}
