package com.example.android.ShotTracker;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.objects.Course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StartRoundList extends ListActivity{
	
	private Vibrator vibe;

    private CourseDAO courseDAO = null;

    private List<Course> courses = null;

    //\todo Change Stow Acres North and South to one 36 hole course. Same w/ Blue Hills.
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	   	super.onCreate(savedInstanceState);
		setContentView(R.layout.startroundselector);
		
		//Initialize Vibrate
    	vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    	
		//Calls the method that fills in the list of courses
		loadStartRoundList();
	}
	 
	private void loadStartRoundList(){
		ListView lv = getListView();
		
		//Sets a fading design as the divider line
		int[] colors = {0, 0xff347c12, 0};
        lv.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);

        courseDAO = new CourseDAO(this);
			
		String line;
			
		//Displays the course options by using a list of hash maps
		List<HashMap<String, String>> fillMaps = null;
		
		String[] from = new String[] {"col_1"};
		int[] to = new int[] {R.id.startitem1};
			
		try {

            fillMaps = new ArrayList<HashMap<String, String>>();

            //Reads all the course names from the file

            courses = courseDAO.readListofCourses();

            for (Course course : courses) {
                //Creates a new hash map
                HashMap<String, String> map = new HashMap<String, String>();

                //Adds the course name and the par for the course to the hash map
                map.put("col_1", course.getName());

                //Adds the hash map to the list
                fillMaps.add(map);
            }

            //Displays the list of hash maps to the screen
            SimpleAdapter adapter = new SimpleAdapter(StartRoundList.this, fillMaps, R.layout.startroundgrid, from, to);
            lv.setAdapter(adapter);
        } catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	 
	//Called when a course is selected
	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		//Sets the vibrate time
		vibe.vibrate(15);
		
		//The file name is sent to the next activity which is entering the players names
		Intent myIntent = new Intent(v.getContext(), SubCourseSelector.class);
		myIntent.putExtra("Course ID", loadCourseID(position));
        startActivity(myIntent);  
         
        //The activity is then closed
        finish();
	}
	
	//Loads the file name for the course selected
	private long loadCourseID(int position){
		//Opens the list of courses

	    long id = courses.get(position).getID();//currently casted to int

	    //Returns the file name
		return id;
	}
	 
	//Closes the activity and returns to the home screen when the back button is pressed
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
