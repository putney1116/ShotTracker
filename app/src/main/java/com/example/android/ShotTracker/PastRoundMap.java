package com.example.android.ShotTracker;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.CourseHoleDAO;
import com.example.android.ShotTracker.db.CourseHoleInfoDAO;
import com.example.android.ShotTracker.db.RoundDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.db.SubRoundDAO;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;
import com.example.android.ShotTracker.objects.Round;
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

import java.util.ArrayList;
import java.util.List;

public class PastRoundMap extends com.google.android.maps.MapActivity implements OnClickListener, OnMapClickListener{
	
	private int pastRoundFileNumber = 0;
	
	double lat[] = new double[2];
	double lng[] = new double[2];
	
	private Location greenLocation = new Location("");
	private Location playerLocation = new Location("");
	private Location tapLocation = new Location("");
	
	private int padding = 0; // offset from edges of the map in pixels
	private CameraUpdate cu;
	
	private int middleDistance;
	private int distance;
	
	private GoogleMap map;
	private Marker pinMarker;
	private Marker playerMarker;
	private Marker mapClickMarker;
	private Polyline mapPolyline;
	
	//greenlocations[lat,long][front,middle,back][holenumber]
	private double greenLocations[][][] = new double[2][3][19];
	
	//teelocations[lat,long][holenumber]
	private double teeLocations[][] = new double[2][19];
	
	public void onCreate(Bundle savedInstanceState) {
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.pastmapview);
    	
    	//Loads the gps coordinates of the greens
    	loadGreenLocations();
    			
    	//Initiliazes the map
		mapInitializer();
		
		//Initializes the map spinner
		mapSpinnerSetup();
	}
	
	//Loads the gps coordinates of the greens
	private void loadGreenLocations(){
        CourseDAO courseDAO = new CourseDAO(this);
        SubCourseDAO subCourseDAO = new SubCourseDAO(this);
        CourseHoleDAO courseHoleDAO = new CourseHoleDAO(this);
        CourseHoleInfoDAO courseHoleInfoDAO = new CourseHoleInfoDAO(this);
        SubRoundDAO subRoundDAO = new SubRoundDAO(this);
        RoundDAO roundDAO = new RoundDAO(this);

        Intent myIntent = getIntent();

        //Loads the course name from the previous activity
        Long roundID = myIntent.getLongExtra("RoundID", -1);

        Round round = roundDAO.readRoundFromID(roundID);

        List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
        List <SubCourse> subCourses = new ArrayList<SubCourse>();

        for (SubRound subRound : subRounds){
            subCourses.add(subCourseDAO.readSubCoursefromID(subRound.getSubCourseID()));
        }
        Long subCourseID = subCourses.get(0).getID();
        Course course = courseDAO.readCourseFromID(subCourseID);

        for (SubCourse subCourse : subCourses){
            List<CourseHole> courseHoles = courseHoleDAO.readListofCourseHoles(subCourse);

            for (CourseHole courseHole : courseHoles){
                List<CourseHoleInfo> courseHoleInfos = courseHoleInfoDAO.readListofCourseHoleInfos(courseHole);
                courseHole.setCourseHoleInfoList(courseHoleInfos);

                for(CourseHoleInfo courseHoleInfo : courseHoleInfos){

                    if (courseHoleInfo.getInfo().equals("Green Front")){
                        greenLocations[0][0][courseHole.getHoleNumber()] = courseHoleInfo.getLatitude();
                        greenLocations[1][0][courseHole.getHoleNumber()] = courseHoleInfo.getLongitude();
                    }else if (courseHoleInfo.getInfo().equals("Green Middle")) {
                        greenLocations[0][1][courseHole.getHoleNumber()] = courseHoleInfo.getLatitude();
                        greenLocations[1][1][courseHole.getHoleNumber()] = courseHoleInfo.getLongitude();
                    }else if (courseHoleInfo.getInfo().equals("Green Back")) {
                        greenLocations[0][2][courseHole.getHoleNumber()] = courseHoleInfo.getLatitude();
                        greenLocations[1][2][courseHole.getHoleNumber()] = courseHoleInfo.getLongitude();
                    }else if (courseHoleInfo.getInfo().equals("Tee")) {
                        teeLocations[0][courseHole.getHoleNumber()] = courseHoleInfo.getLatitude();
                        teeLocations[1][courseHole.getHoleNumber()] = courseHoleInfo.getLongitude();
                    }
                }
            }
        }
	}

    //\todo deal with hole numbers being out of order in the same way as start round
	
	//Initializes the map spinner
	private void mapSpinnerSetup(){    	
    	String[] items = {"Hole 1","Hole 2","Hole 3","Hole 4","Hole 5","Hole 6",
    					"Hole 7","Hole 8","Hole 9","Hole 10","Hole 11","Hole 12",
    					"Hole 13","Hole 14","Hole 15","Hole 16","Hole 17","Hole 18"};
    	Spinner spinner = (Spinner) findViewById(R.id.pastmapspinner);
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(PastRoundMap.this, android.R.layout.simple_spinner_item, items);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	
    	//Sets the map location to the correct hole when the hole number is changed
    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    			setMapLocation(pos+1);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});    	
	}

	@Override
	public void onClick(View arg0) {		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	//Shows the save round confirmation dialog if the back button is pressed
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	finish();
        	
        	return true;
        }
 	
        return super.onKeyDown(keyCode, event);
    }
	
	//Initializes the map view tab
	private void mapInitializer(){
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.pastmaptab))
		        .getMap();
			
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		map.setOnMapClickListener(this);
	}
	
	//Sets the zoom and center of the map view
	//Runs when the map view tab is selected
	private void setMapLocation(int holeNumber){
		//Test values
		//greenLocations[0][2][1]=42.351185;
		//greenLocations[1][2][1]=-71.137167;
			
		map.clear();
		
		//Loads the tee location and the location of the middle of the green for the current hole
		lat[0] = (teeLocations[0][holeNumber]);
		lat[1] = (greenLocations[0][1][holeNumber]);
		lng[0] = (teeLocations[1][holeNumber]);
		lng[1] = (greenLocations[1][1][holeNumber]);
				
		//Loads the location of the middle of the green and displays the distance in yards from the current location
		greenLocation.setLatitude(lat[1]);
		greenLocation.setLongitude(lng[1]);
		playerLocation.setLatitude(lat[0]);
		playerLocation.setLongitude(lng[0]);
		    	
		middleDistance = (int)Math.round(greenLocation.distanceTo(playerLocation) * 1.09361);
		
		pinMarker = map.addMarker(new MarkerOptions()
	   		.position(new LatLng(lat[1],lng[1]))
	   		.title(Integer.toString(middleDistance)+" Yds")
	   		.snippet("Hole " + holeNumber)
	   		.icon(BitmapDescriptorFactory
	   		.fromResource(R.drawable.pinmarker))
	   		.anchor((float)0.37, (float)1.0));
		
		pinMarker.showInfoWindow();
		
		playerMarker = map.addMarker(new MarkerOptions()
	       	.position(new LatLng(lat[0],lng[0]))
	       	.title("Tee")
	       	.snippet("Hole " + holeNumber)
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
	public void onMapClick(LatLng point){		
		if(mapClickMarker!=null){
			mapPolyline.remove();
			mapClickMarker.remove();
		}
		
		tapLocation.setLatitude(point.latitude);
		tapLocation.setLongitude(point.longitude);
		
		distance = (int)Math.round(tapLocation.distanceTo(playerLocation) * 1.09361);
		middleDistance = (int)Math.round(tapLocation.distanceTo(greenLocation) * 1.09361);
		
		mapClickMarker = map.addMarker(new MarkerOptions()
		.position(point)
		.title(distance +" yds away")
	   	.snippet(middleDistance + " yds to pin")
	   	.icon(BitmapDescriptorFactory
	    .fromResource(R.drawable.golfball)));
		
		mapClickMarker.showInfoWindow();
			
		mapPolyline = map.addPolyline(new PolylineOptions()
	    .add(new LatLng(lat[0],lng[0]))
	    .add(new LatLng(point.latitude, point.longitude))
	    .add(new LatLng(lat[1],lng[1]))
	    .color(Color.GRAY)
	    .width(5));
		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener(){
			public void onInfoWindowClick(Marker marker){
				marker.hideInfoWindow();
			}
		});
		
		map.setOnMarkerClickListener(new OnMarkerClickListener(){
			public boolean onMarkerClick(Marker marker){
				if(!marker.equals(mapClickMarker))
					marker.showInfoWindow();
					
				mapClickMarker.remove();
				mapPolyline.remove();
				
				return true;
			}
		});
	}
}