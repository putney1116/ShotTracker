package com.example.android.ShotTracker;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.RoundDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.db.SubRoundDAO;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.SubCourse;
import com.example.android.ShotTracker.objects.SubRound;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PastRoundList extends ListActivity{
	
	private AlertDialog.Builder builder;
	
	private int longClickPosition = 0;
	private int listLength = 0;

    private RoundDAO roundDAO = null;
    private SubRoundDAO subRoundDAO = null;
    private SubCourseDAO subCourseDAO = null;
    private CourseDAO courseDAO = null;
	
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
				
				return true;
			}
		});

		String line;

		int fileNumber = 0;
			
		//Displays the past rounds by using a list of hash maps
		List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
			
		String[] from = new String[] {"col_1", "col_2"};
		int[] to = new int[] {R.id.item1, R.id.item2};

        roundDAO = new RoundDAO(this);
        subRoundDAO = new SubRoundDAO(this);
        subCourseDAO = new SubCourseDAO(this);
        courseDAO = new CourseDAO(this);

        List<Round> rounds = roundDAO.readListofRounds();

        listLength = rounds.size();

		for (Round round : rounds) {
            //Creates a new hash map
            HashMap<String, String> map = new HashMap<String, String>();

            List <SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            SubCourse subCourse = subCourseDAO.readSubCourseFromID(subRounds.get(0).getSubCourseID());
            String courseName = courseDAO.readCourseNameFromID(subCourse.getCourseID());

            Date date = round.getDate();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

            //Puts the course name and the date in the hash map
            map.put("col_1", courseName);
            map.put("col_2", df.format(date));

            //Adds the hash map to the list
            fillMaps.add(0,map);
        }

        SimpleAdapter adapter = new SimpleAdapter(PastRoundList.this, fillMaps, R.layout.pastroundgrid, from, to);
        lv.setAdapter(adapter);
	}
	
	//Asks the user for confirmation to delete the round
	private void buildDialog(){
		builder = new AlertDialog.Builder(this);
	    builder.setMessage("Would you like to delete this round?");
	    builder.setCancelable(true);
	    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		//Sets the roundNumber to the inverse of the position in the list
                int roundNumber = (listLength-1) - longClickPosition;
	    		
	    		//Deletes the file selected
                List<Round> rounds = roundDAO.readListofRounds();
                Round round = rounds.get(roundNumber);

                //\todo Add utility to delete a round and everything below it and call it here
                //DAOUtility.deleteRound(round);

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
		//The round id is passed to the activity.

        List<Round> rounds = roundDAO.readListofRounds();
        Long roundID = rounds.get(position).getID();

		Intent myIntent = new Intent(v.getContext(), PastRound.class);
		myIntent.putExtra("RoundID", roundID);
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
