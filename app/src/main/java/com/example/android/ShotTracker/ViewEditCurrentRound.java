package com.example.android.ShotTracker;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Justin_Ayvazian on 5/13/2015.
 */
public class ViewEditCurrentRound extends com.google.android.maps.MapActivity implements View.OnClickListener, GoogleMap.OnMapClickListener {
    private Context contextIn;
    private Activity activityIn;


    private GoogleMap map;
    private double maxlat;
    private double maxlng;
    private double minlat;
    private double minlng;
    private int screenState = 0;
    //0: View/Edit Shots; 1: Add Shots
    private int addShotState = 0;
    //0: Select Shot start; 1: Select shot end
    private int currentHole = 0;

    public ViewEditCurrentRound(Context context, Activity activity){
        contextIn = context;
        activityIn = activity;

    }
    public List[] ViewEditCurrentRoundMain(List[] shotList, double[][][] greenLocations, double[][] teeLocations, int currentHole, int[] holeNumberText, boolean eighteenHoleRound){

        //greenLocations[latlng][0(front)/1(middle)/2(back)][hole number - human readable, 0 index is blank, hole numbers and index numbers are 1-18 for 19 length array]
        //teeLocations[latlng][hole number - same weirdness with indices as greenlocations]
        //When assigning something to shotTableOut(1), type must be List<Shot>
        //Going to need go use List methods to access indicies of arrays, both by hole and by shot within each hole
        Log.d("Test", "This is working2");
        activityIn.setContentView(R.layout.vieweditcurrentround);
        Log.d("Test", "This is working3");
        initializeHoleSpinner(holeNumberText, eighteenHoleRound);
        initializeAddShotButton();
        initializeMap();
        populateMap(currentHole);

        //Justin you are going to want to move this to a finish button somewhere. We might have to
        // re-think how to get the data back to me because you need to stay in this class in order
        // to wait for user clicks...I think, we need to discuss this
        return shotList;
    }

    public void initializeHoleSpinner(int[] holeNumberText, boolean eighteenHoleRound){
        Log.d("Test", "This is working");
        Spinner holeSpinner = (Spinner) activityIn.findViewById(R.id.vieweditcurrentroundholespinner);
        Log.d("Test", "This is working4");
        ArrayAdapter<String> adapter;

        //\todo set adapter to string list of hole numbers being played in current round
        String[] textHoleNumbers = null;
        if(eighteenHoleRound){
            textHoleNumbers = new String[18];
            for(int x = 1; x < 19; x++){
                textHoleNumbers[x-1] = "Hole " + Integer.toString(holeNumberText[x]);
            }
        }
        else{
            textHoleNumbers = new String[9];
            for(int x = 1; x < 10; x++){
                textHoleNumbers[x-1] = "Hole " + Integer.toString(holeNumberText[x]);
            }
        }
        adapter = new ArrayAdapter<String>(contextIn, android.R.layout.simple_spinner_item, textHoleNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holeSpinner.setAdapter(adapter);

        holeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                populateMap(pos + 1);
                if (pos + 1 != currentHole)
                    addShotState = 0;

                currentHole = pos + 1;

            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    public void initializeAddShotButton(){

        Button addShotButton = (Button) findViewById(R.id.vieweditcurrentroundaddshot);
        //do not clear map
        //select shot start
        //select shot end

        //in on click, set screenState to 1
        //in return (button OR back button) set back to 0
        addShotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change button to done adding shots
                //change info text to select shot start
                screenState = 1;

                //change button back to add shots
                addShotState = 0;
                screenState = 0;
            }
        });
    }

    public void initializeMap(){
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.vieweditcurrentroundmapfragment)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setOnMapClickListener(this);
        //set bounds and do rotation here

    }

    public void populateMap(int currentHoleNumber){

        //map.clear();

    }




    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        switch(screenState){
            case 0:
                //if line, edit shot info
                //if node, build dialog to delete node
                //otherwise, ignore
                break;
            case 1:
                switch(addShotState){
                    case 0:
                        //record location, change info text to select shot end
                        addShotState = 1;
                        break;
                    case 1:
                        //hide the map fragment and buttons
                        //launch edit shot info dialog
                        //change text back to select shot start location
                        break;
                }
                break;
        }

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
