package com.example.android.ShotTracker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.objects.Course;

public class CourseCatalogList extends ListActivity{

    //\todo Vibrator?!

    private CourseDAO courseDAO = null;

    private List<Course> courses = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	   	super.onCreate(savedInstanceState);
		setContentView(R.layout.coursecataloglistselector);
					
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
		int[] to = new int[] {R.id.coursecatalogitem1};


        try {

            fillMaps = new ArrayList<HashMap<String, String>>();
		
/*				//Opens the file that contains the list of courses
				AssetManager assetManager = getAssets();
				InputStream filereader = null;
				try {
					filereader = assetManager.open("enumlist");
				} catch (IOException e1) {
					e1.printStackTrace();
				}										
		    	InputStreamReader inputreader = new InputStreamReader(filereader);
		        BufferedReader bufferedreader = new BufferedReader(inputreader);
		        
		        InputStream filereader2 = null;
		        InputStreamReader inputreader2 = null;
		        BufferedReader bufferedreader2 = null;
		        */

            //Reads all the course names from the file


            //while((line = bufferedreader.readLine()) != null){
            courses = courseDAO.readListofCourses();

            for (Course course : courses){
		        	/*//Opens the course info file
		        	try {
		        		filereader2 = assetManager.open(line);
		        	} catch (IOException e1) {
		        		e1.printStackTrace();
		        	}										
		        	inputreader2 = new InputStreamReader(filereader2);
		        	bufferedreader2 = new BufferedReader(inputreader2);*/

                //Creates a new hash map
                HashMap<String, String> map = new HashMap<String, String>();

                //Adds the course name and the par for the course to the hash map
                map.put("col_1", course.getName());

                //Adds the hash map to the list
                fillMaps.add(map);
            }

            //Displays the list of hash maps to the screen
            SimpleAdapter adapter = new SimpleAdapter(CourseCatalogList.this, fillMaps, R.layout.coursecataloglistgrid, from, to);
            lv.setAdapter(adapter);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
	}
	 
	//Called when a course is selected
	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		//The file name is sent to the next activity which is the detailed course information pages
		Intent myIntent = new Intent(v.getContext(), CourseCatalogSelector.class);
		myIntent.putExtra("Course ID", loadCourseID(position));
        startActivity(myIntent);

        finish();
	}
	
	//Loads the file name for the course selected
	private long loadCourseID(int position){
		//Opens the list of courses

        long id = courses.get(position).getID();

	    //Returns the file name
		return id;
	}
	 
	//Closes the activity and returns to the home screen when the back button is pressed
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
	        	
	        return true;
		}
	    return super.onKeyDown(keyCode, event);
	}
}
