package com.example.android.ShotTracker;

// D. McGlinchey - First comment!!

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Settings extends Activity{
//hello
	private AlertDialog.Builder builder;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.settings);
	    	
	    //Initializes the dialog box that is displayed when the back button is pressed
	    buildDialog();
	    
	    //Sets the hints in the editable text boxes
	    setHints();
	    
	    //Initializes the button that saves the settings
	    saveSettingsButtonInitializer();
	}
	
	//Saves the settings and returns to the home screen
	private void saveSettingsButtonInitializer(){
		Button saveSettingsButton = (Button)findViewById(R.id.savesettingsbutton);
		
		saveSettingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					//Opens the shared preferences file
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    			SharedPreferences.Editor editor = preferences.edit();
	    			
	    			//Saves either the entered value or the default value to the file
	    			EditText input = (EditText)findViewById(R.id.player1defaulttext);
	    			if(input.getText().toString().equals("")){
	    				editor.putString("Player 1 Name", input.getHint().toString());
					}
					else{
						editor.putString("Player 1 Name", input.getText().toString());
					}
	    			
	    			//Saves either the entered value or the default value to the file
	    			input = (EditText)findViewById(R.id.player2defaulttext);
	    			if(input.getText().toString().equals("")){
	    				editor.putString("Player 2 Name", input.getHint().toString());
					}
					else{
						editor.putString("Player 2 Name", input.getText().toString());
					}
	    			
	    			//Saves either the entered value or the default value to the file
	    			input = (EditText)findViewById(R.id.player3defaulttext);
	    			if(input.getText().toString().equals("")){
	    				editor.putString("Player 3 Name", input.getHint().toString());
					}
					else{
						editor.putString("Player 3 Name", input.getText().toString());
					}
	    			
	    			//Saves either the entered value or the default value to the file
	    			input = (EditText)findViewById(R.id.player4defaulttext);
	    			if(input.getText().toString().equals("")){
	    				editor.putString("Player 4 Name", input.getHint().toString());
					}
					else{
						editor.putString("Player 4 Name", input.getText().toString());
					}
	    			
	    			//Saves the file
	    			editor.commit();
			         
	    			//Closes the present activity and returns the display to the home screen
			        finish();
				}catch(Exception e) {
				}
			}
    	});
	}
	
	//Sets the hints in the editable text boxes
	private void setHints(){
		//Opens the shared preferences file
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		//Sets the hints to the defaults
		EditText input = (EditText)findViewById(R.id.player1defaulttext);
		input.setHint(preferences.getString("Player 1 Name", "Me"));
		
		input = (EditText)findViewById(R.id.player2defaulttext);
		input.setHint(preferences.getString("Player 2 Name", "Player 2"));
		
		input = (EditText)findViewById(R.id.player3defaulttext);
		input.setHint(preferences.getString("Player 3 Name", "Player 3"));
		
		input = (EditText)findViewById(R.id.player4defaulttext);
		input.setHint(preferences.getString("Player 4 Name", "Player 4"));
	}
	
	//If the back button is pressed, the dialog to save the settings is called if changes have been made
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			//Checks if any changes have been made
			EditText input = (EditText)findViewById(R.id.player1defaulttext);
			if(input.getText().toString().equals("")){
				input = (EditText)findViewById(R.id.player2defaulttext);
				if(input.getText().toString().equals("")){
					input = (EditText)findViewById(R.id.player3defaulttext);
					if(input.getText().toString().equals("")){
						input = (EditText)findViewById(R.id.player4defaulttext);
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
			}
			else{
				//If changes have been made, the dialog box asking for save confirmation is displayed 
	        	builder.show();
			}
        	
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    //Dialog asks the user if they would like to save the settings
    private void buildDialog(){
		builder = new AlertDialog.Builder(Settings.this);
    	builder.setMessage("Would you like to save your settings?");
    	builder.setCancelable(true);
    	
    	//If the user would like to save the settings
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id){ 
    			//Opens the shared preferences file
    			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    			SharedPreferences.Editor editor = preferences.edit();
    			
    			//Saves either the entered value or the default value to the file
    			EditText input = (EditText)findViewById(R.id.player1defaulttext);
    			if(input.getText().toString().equals("")){
    				editor.putString("Player 1 Name", input.getHint().toString());
				}
				else{
					editor.putString("Player 1 Name", input.getText().toString());
				}
    			
    			//Saves either the entered value or the default value to the file
    			input = (EditText)findViewById(R.id.player2defaulttext);
    			if(input.getText().toString().equals("")){
    				editor.putString("Player 2 Name", input.getHint().toString());
				}
				else{
					editor.putString("Player 2 Name", input.getText().toString());
				}
    			
    			//Saves either the entered value or the default value to the file
    			input = (EditText)findViewById(R.id.player3defaulttext);
    			if(input.getText().toString().equals("")){
    				editor.putString("Player 3 Name", input.getHint().toString());
				}
				else{
					editor.putString("Player 3 Name", input.getText().toString());
				}
    			
    			//Saves either the entered value or the default value to the file
    			input = (EditText)findViewById(R.id.player4defaulttext);
    			if(input.getText().toString().equals("")){
    				editor.putString("Player 4 Name", input.getHint().toString());
				}
				else{
					editor.putString("Player 4 Name", input.getText().toString());
				}
    			
    			//Saves the file
    			editor.commit();
    			
    			//Closes the present activity and returns the display to the home screen
    			finish();
    		}
    	});
    	
    	//If the user would not like to save the settings
    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			//Closes the present activity and returns the display to the home screen
    			finish();
    	    }
    	});
    	
    	//If the user would like to cancel pressing the back button
    	builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			//The dialog is closed
    			dialog.cancel();
    	    }
    	});
	}
}
