package com.example.android.ShotTracker;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.android.ShotTracker.db.RoundDAO;

public class PastRoundList extends ListActivity{
	
	private AlertDialog.Builder builder;
	
	private int longClickPosition = 0;
	private int listLength = 0;

    private RoundDAO roundDAO = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	   	super.onCreate(savedInstanceState);
		setContentView(R.layout.pastroundselector);
					
		//Displays the list of past rounds
		loadPastRoundsList();
			
		//Displays the delete round dialog
		buildDialog();
	}
	 
	//Displays the list of past rounds
	private void loadPastRoundsList(){
		ListView lv = getListView();
		
		//Sets a fading design as the divider line
		int[] colors = {0, 0xff347c12, 0};
        lv.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);

        //Sets the list items to be long clickable so they can be deleted
		lv.setLongClickable(true);
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
				//Saves the position of the long click
				longClickPosition = position;
				
				//Calls the dialog box that confirms the delete of the round
				builder.show();
				
				return false;
			}
		});

		String line;

		int fileNumber = 0;
			
		//Displays the past rounds by using a list of hash maps
		List<HashMap<String, String>> fillMaps = null;
			
		String[] from = new String[] {"col_1", "col_2"};
		int[] to = new int[] {R.id.item1, R.id.item2};
			
		try {
			try {
				fillMaps = new ArrayList<HashMap<String, String>>();

				InputStream  filereader = null;
		   		InputStreamReader inputreader = null;
		        BufferedReader bufferedreader = null;

		        InputStream  filereader2 = null;
		  		InputStreamReader inputreader2 = null;
		        BufferedReader bufferedreader2 = null;

		        AssetManager assetManager;
				
		        //Runs through all the past round files
				while(true){
					filereader = openFileInput("pastround"+fileNumber+".txt");							
		    		inputreader = new InputStreamReader(filereader);
		            bufferedreader = new BufferedReader(inputreader);
		            
		   			fileNumber++;						
				        
		   			//Creates a new hash map
		   			HashMap<String, String> map = new HashMap<String, String>();
				        
		   			//Reads in the course name
		   			line = bufferedreader.readLine();
		    			
		   			//Opens the course info file
		   			assetManager = getAssets();
		   			try {
		   				filereader2 = assetManager.open(line);
		   			} catch (IOException e1) {
		   				e1.printStackTrace();
		   			}													
		    		inputreader2 = new InputStreamReader(filereader2);
		    		bufferedreader2 = new BufferedReader(inputreader2);
		   		    
		    		//Puts the course name, score for the first player, and the date in the hash map
			        map.put("col_1", bufferedreader2.readLine());
			        map.put("col_2", bufferedreader.readLine());
			        map.put("col_3", bufferedreader.readLine());
			       
			        //Adds the hash map to the list
			        fillMaps.add(0,map);			        
				}
			} catch (FileNotFoundException e) {
				//Displays the list of hash maps to the screen
				SimpleAdapter adapter = new SimpleAdapter(PastRoundList.this, fillMaps, R.layout.pastroundgrid, from, to);
		        lv.setAdapter(adapter);
		        
		        listLength = fillMaps.size();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	//Asks the user for confirmation to delete the round
	private void buildDialog(){
		builder = new AlertDialog.Builder(this);
	    builder.setMessage("Would you like to delete this round?");
	    builder.setCancelable(true);
	    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		//Sets the fileNumber to the inverse of the position in the list
	    		int fileNumber = (listLength-1) - longClickPosition;
	    		
	    		//Deletes the file selected
	    		File file = new File(getFilesDir(), "pastround"+fileNumber+".txt");	
	    		file.delete();
	    		
	    		//Renames all the files after the one deleted by one less so there is no gap
	    		while(true){
	    			file = new File(getFilesDir(), "pastround"+(fileNumber+1)+".txt");
	    			
	    			if(file.exists()){
	    				file.renameTo(new File(getFilesDir(), "pastround"+fileNumber+".txt"));
	    			}
	    			else{
	    				break;
	    			}
	    			
	    			fileNumber++;
	    		}
				 
	    		//Displays the list of past rounds again
	    		loadPastRoundsList();
	    		
	    	}
	    });
	    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		//The dialog box is closed with no action
	    		dialog.cancel();
	        }
	    });
	}
	
	//Called when the user selects a round
	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		//Starts the activity that displays the past round.
		//The file number is passed to the activity.
		Intent myIntent = new Intent(v.getContext(), PastRound.class);
		myIntent.putExtra("Position", (listLength-1) - position);
        startActivity(myIntent);  
	}
	 
	//Closes the activity and the display returns to the home screen if the back button is pressed
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
	        	
	        return true;
		}
	    return super.onKeyDown(keyCode, event);
	}
}
