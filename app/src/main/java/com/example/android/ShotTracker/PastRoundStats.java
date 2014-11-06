package com.example.android.ShotTracker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class PastRoundStats extends ListActivity{
	
	private List<String> players = new ArrayList<String>();
	
	private double averageScore = 0;
	private double averagePlusMinus = 0;
	private double averageFront9Score = 0;
	private double averageFront9PlusMinus = 0;
	private double averageBack9Score = 0;
	private double averageBack9PlusMinus = 0;
	private int numberOfHoles = 0;
	private int numberOfPar3Holes = 0;
	private int numberOfPar4Holes = 0;
	private int numberOfPar5Holes = 0;
	private double albatrossCount = 0;
	private double eagleCount = 0;
	private double birdieCount = 0;
	private double parCount = 0;
	private double bogeyCount = 0;
	private double doubleBogeyCount = 0;
	private double tripleBogeyCount = 0;
	private double quadBogeyPlusCount = 0;
	private double par3EagleCount = 0;
	private double par3BirdieCount = 0;
	private double par3ParCount = 0;
	private double par3BogeyCount = 0;
	private double par3DoubleBogeyCount = 0;
	private double par3TripleBogeyCount = 0;
	private double par3QuadBogeyPlusCount = 0;
	private double par4AlbatrossCount = 0;
	private double par4EagleCount = 0;
	private double par4BirdieCount = 0;
	private double par4ParCount = 0;
	private double par4BogeyCount = 0;
	private double par4DoubleBogeyCount = 0;
	private double par4TripleBogeyCount = 0;
	private double par4QuadBogeyPlusCount = 0;
	private double par5AlbatrossCount = 0;
	private double par5EagleCount = 0;
	private double par5BirdieCount = 0;
	private double par5ParCount = 0;
	private double par5BogeyCount = 0;
	private double par5DoubleBogeyCount = 0;
	private double par5TripleBogeyCount = 0;
	private double par5QuadBogeyPlusCount = 0;
	
	private int pastRoundFileNumber = 0;
	
	private int par[] = new int[19];
	
	private DecimalFormat df = new DecimalFormat("#.##");
	
	//Called on start of activity
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pastroundstatslist);
    	
    	Intent myIntent = getIntent();
		
		//Loads the file number from the previous activity
		pastRoundFileNumber = myIntent.getIntExtra("Position", -1);
    	
    	//Populates the list of players
    	getPlayerList();
    	
    	//Initializes the spinner
    	playerSpinnerSetup();
	}
	
	//Initializes the player spinner
	private void playerSpinnerSetup(){
		Spinner spinner = (Spinner) findViewById(R.id.pastPlayerSpinner);
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(PastRoundStats.this, android.R.layout.simple_spinner_item, players);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	
    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    			calculatePlayerStats(pos);
    		}

    		public void onNothingSelected(AdapterView<?> parent) {
    		}
    	});    	
	}
	 
	//Runs through the scores for the round and increments the stat variables as necessary
	private void calculateScore(int playerNumber){
		InputStream filereader = null;
		
		try {
			//Opens the file for the round passed in
			filereader = openFileInput("pastround"+pastRoundFileNumber+".txt");
			InputStreamReader inputreader = new InputStreamReader(filereader);
			BufferedReader bufferedreader = new BufferedReader(inputreader);
			
			//Disregards the unnecessary info in the file
			for(int x=0;x<4;x++){
				bufferedreader.readLine();
			}
			
			//Disregards the scores for players that are not being displayed
			for(int x = 1;x<=18*(playerNumber-1);x++){
				bufferedreader.readLine();
			}
			
			int holeScore = 0;
			int roundScore = 0;
			int runningPar = 0;
			
			//Run through the scores for all 18 holes for that player
			for(int x=1;x<=18;x++){
				holeScore = Integer.valueOf(bufferedreader.readLine());
				
				//Stats only count if the player played that hole
				if(holeScore>0){
					//Increments different stat variables for future calculations
					runningPar += par[x];
					numberOfHoles++;
					roundScore += holeScore;
					
					if(x<=9){
						averageFront9Score += holeScore;
						averageFront9PlusMinus += holeScore - par[x];
					}
					else{
						averageBack9Score += holeScore;
						averageBack9PlusMinus += holeScore - par[x];
					}
					
					//Increments the counts for each score type
					if(holeScore-par[x]==-3)
						albatrossCount++;
					else if(holeScore-par[x]==-2)
						eagleCount++;
					else if(holeScore-par[x]==-1)
						birdieCount++;
					else if(holeScore-par[x]==0)
						parCount++;
					else if(holeScore-par[x]==1)
						bogeyCount++;
					else if(holeScore-par[x]==2)
						doubleBogeyCount++;
					else if(holeScore-par[x]==3)
						tripleBogeyCount++;
					else if(holeScore-par[x]>=4)
						quadBogeyPlusCount++;
					
					//Increments the counts for each score type on par 3's
					if(par[x]==3){
						numberOfPar3Holes++;
						
						if(holeScore-par[x]==-2)
							par3EagleCount++;
						else if(holeScore-par[x]==-1)
							par3BirdieCount++;
						else if(holeScore-par[x]==0)
							par3ParCount++;
						else if(holeScore-par[x]==1)
							par3BogeyCount++;
						else if(holeScore-par[x]==2)
							par3DoubleBogeyCount++;
						else if(holeScore-par[x]==3)
							par3TripleBogeyCount++;
						else if(holeScore-par[x]>=4)
							par3QuadBogeyPlusCount++;
					}
					//Increments the counts for each score type on par 4's
					else if(par[x]==4){
						numberOfPar4Holes++;
						
						if(holeScore-par[x]==-3)
							par4AlbatrossCount++;
						else if(holeScore-par[x]==-2)
							par4EagleCount++;
						else if(holeScore-par[x]==-1)
							par4BirdieCount++;
						else if(holeScore-par[x]==0)
							par4ParCount++;
						else if(holeScore-par[x]==1)
							par4BogeyCount++;
						else if(holeScore-par[x]==2)
							par4DoubleBogeyCount++;
						else if(holeScore-par[x]==3)
							par4TripleBogeyCount++;
						else if(holeScore-par[x]>=4)
							par4QuadBogeyPlusCount++;
					}
					//Increments the counts for each score type on par 5's
					else if(par[x]==5){
						numberOfPar5Holes++;
						
						if(holeScore-par[x]==-3)
							par5AlbatrossCount++;
						else if(holeScore-par[x]==-2)
							par5EagleCount++;
						else if(holeScore-par[x]==-1)
							par5BirdieCount++;
						else if(holeScore-par[x]==0)
							par5ParCount++;
						else if(holeScore-par[x]==1)
							par5BogeyCount++;
						else if(holeScore-par[x]==2)
							par5DoubleBogeyCount++;
						else if(holeScore-par[x]==3)
							par5TripleBogeyCount++;
						else if(holeScore-par[x]>=4)
							par5QuadBogeyPlusCount++;
					}
				}			
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		 
	//Calculates the total stats for the specified player
	private void calculatePlayerStats(int pos){
		InputStream filereader = null;
		InputStreamReader inputreader = null;
		BufferedReader bufferedreader = null;
		InputStream filereader2 = null;
		InputStreamReader inputreader2 = null;
		BufferedReader bufferedreader2 = null;
		AssetManager assetManager = getAssets();
		
		//Sets all the statistic variables back to 0
		initializeStatisticVariables();
	
		int numberOfPlayers = 0;
		String fileName = null;
		
		try {
			filereader = openFileInput("pastround"+pastRoundFileNumber+".txt");
			inputreader = new InputStreamReader(filereader);
			bufferedreader = new BufferedReader(inputreader);
			
			//Saves the course name to be opened later
			fileName = bufferedreader.readLine();
			
			//Disregards unnecessary info
			for(int x=0;x<2;x++){
				bufferedreader.readLine();
			}
			
			//Saves the number of players in the round
			numberOfPlayers = Integer.parseInt(bufferedreader.readLine());
			
			//Disregards unnecessary info
			for(int x=1;x<=18*numberOfPlayers;x++){
				bufferedreader.readLine();
			}
			
			filereader2 = assetManager.open(fileName);
			inputreader2 = new InputStreamReader(filereader2);
			bufferedreader2 = new BufferedReader(inputreader2);
			
			//Disregards unnecessary info
			bufferedreader2.readLine();
			
			//Saves the par for each hole on the course
			getCourseInfo(bufferedreader2);

			//Determines if any of the players in the round are the selected player
			for(int x=1;x<=numberOfPlayers;x++){
				if(bufferedreader.readLine().equals(players.get(pos))){
					//Calculates the stats for the round for the specified player
					calculateScore(x);
				}
			}
		}catch (FileNotFoundException e) {
			Log.d("calculatePlayerStats","File Not Found: pastround"+pastRoundFileNumber+".txt");
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		//Calls the method that displays the stats on the screen
		fillInList();
	}
	
	//Saves the par for each hole on the course specified
	private void getCourseInfo(BufferedReader bufferedreader){		    
		try {
			for(int x=0;x<19;x++){
				par[x]=Integer.parseInt(bufferedreader.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	//Displays the stats on the screen
	private void fillInList(){
		//Displays all unused stats as 0 instead of a division error
		if(numberOfHoles==0)
			numberOfHoles=1;
		if(numberOfPar3Holes==0)
			numberOfPar3Holes=1;
		if(numberOfPar4Holes==0)
			numberOfPar4Holes=1;
		if(numberOfPar5Holes==0)
			numberOfPar5Holes=1;
		
		//Calculates the total scores by dividing by the number of rounds that were applicable
		averageScore = averageFront9Score + averageBack9Score;
		averagePlusMinus = averageFront9PlusMinus + averageBack9PlusMinus;
		
		//Displays all of the stats to the screen by using a list of hash maps
		List<HashMap<String, String>> fillMaps = null;
		
		String[] from = new String[] {"col_1", "col_2", "col_3"};
		int[] to = new int[] {R.id.statisticsitem1, R.id.statisticsitem2, R.id.statisticsitem3};
		
		fillMaps = new ArrayList<HashMap<String, String>>();
		
		HashMap<String, String> map = new HashMap<String, String>();
        map.put("col_1", "Overall");
        map.put("col_2", "Average");
        map.put("col_3", "+/-");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Total Score");
        map.put("col_2", ""+df.format(averageScore));
        map.put("col_3", ""+df.format(averagePlusMinus));
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Front 9");
        map.put("col_2", ""+df.format(averageFront9Score));
        map.put("col_3", ""+df.format(averageFront9PlusMinus));
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Back 9");
        map.put("col_2", ""+df.format(averageBack9Score));
        map.put("col_3", ""+df.format(averageBack9PlusMinus));
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Score Per Hole");
        map.put("col_2", ""+df.format(averageScore/numberOfHoles));
        map.put("col_3", ""+df.format(averagePlusMinus/numberOfHoles));
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Scoring Breakdown");
        map.put("col_2", "Total");
        map.put("col_3", "%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Albatross");
        map.put("col_2", ""+df.format(albatrossCount));
        map.put("col_3", ""+df.format(albatrossCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Eagle");
        map.put("col_2", ""+df.format(eagleCount));
        map.put("col_3", ""+df.format(eagleCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Birdie");
        map.put("col_2", ""+df.format(birdieCount));
        map.put("col_3", ""+df.format(birdieCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par");
        map.put("col_2", ""+df.format(parCount));
        map.put("col_3", ""+df.format(parCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Bogey");
        map.put("col_2", ""+df.format(bogeyCount));
        map.put("col_3", ""+df.format(bogeyCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Double");
        map.put("col_2", ""+df.format(doubleBogeyCount));
        map.put("col_3", ""+df.format(doubleBogeyCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Triple");
        map.put("col_2", ""+df.format(tripleBogeyCount));
        map.put("col_3", ""+df.format(tripleBogeyCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Quadruple or Worse");
        map.put("col_2", ""+df.format(quadBogeyPlusCount));
        map.put("col_3", ""+df.format(quadBogeyPlusCount/numberOfHoles*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par 3");
        map.put("col_2", "Total");
        map.put("col_3", "%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Hole In One");
        map.put("col_2", ""+df.format(par3EagleCount));
        map.put("col_3", ""+df.format(par3EagleCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Birdie");
        map.put("col_2", ""+df.format(par3BirdieCount));
        map.put("col_3", ""+df.format(par3BirdieCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par");
        map.put("col_2", ""+df.format(par3ParCount));
        map.put("col_3", ""+df.format(par3ParCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Bogey");
        map.put("col_2", ""+df.format(par3BogeyCount));
        map.put("col_3", ""+df.format(par3BogeyCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Double");
        map.put("col_2", ""+df.format(par3DoubleBogeyCount));
        map.put("col_3", ""+df.format(par3DoubleBogeyCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Triple");
        map.put("col_2", ""+df.format(par3TripleBogeyCount));
        map.put("col_3", ""+df.format(par3TripleBogeyCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Quadruple or Worse");
        map.put("col_2", ""+df.format(par3QuadBogeyPlusCount));
        map.put("col_3", ""+df.format(par3QuadBogeyPlusCount/numberOfPar3Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par 4");
        map.put("col_2", "Total");
        map.put("col_3", "%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Hole In One");
        map.put("col_2", ""+df.format(par4AlbatrossCount));
        map.put("col_3", ""+df.format(par4AlbatrossCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Eagle");
        map.put("col_2", ""+df.format(par4EagleCount));
        map.put("col_3", ""+df.format(par4EagleCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Birdie");
        map.put("col_2", ""+df.format(par4BirdieCount));
        map.put("col_3", ""+df.format(par4BirdieCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par");
        map.put("col_2", ""+df.format(par4ParCount));
        map.put("col_3", ""+df.format(par4ParCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Bogey");
        map.put("col_2", ""+df.format(par4BogeyCount));
        map.put("col_3", ""+df.format(par4BogeyCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Double");
        map.put("col_2", ""+df.format(par4DoubleBogeyCount));
        map.put("col_3", ""+df.format(par4DoubleBogeyCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Triple");
        map.put("col_2", ""+df.format(par4TripleBogeyCount));
        map.put("col_3", ""+df.format(par4TripleBogeyCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Quadruple or Worse");
        map.put("col_2", ""+df.format(par4QuadBogeyPlusCount));
        map.put("col_3", ""+df.format(par4QuadBogeyPlusCount/numberOfPar4Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par 5");
        map.put("col_2", "Total");
        map.put("col_3", "%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Albatross");
        map.put("col_2", ""+df.format(par5AlbatrossCount));
        map.put("col_3", ""+df.format(par5AlbatrossCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Eagle");
        map.put("col_2", ""+df.format(par5EagleCount));
        map.put("col_3", ""+df.format(par5EagleCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Birdie");
        map.put("col_2", ""+df.format(par5BirdieCount));
        map.put("col_3", ""+df.format(par5BirdieCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Par");
        map.put("col_2", ""+df.format(par5ParCount));
        map.put("col_3", ""+df.format(par5ParCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Bogey");
        map.put("col_2", ""+df.format(par5BogeyCount));
        map.put("col_3", ""+df.format(par5BogeyCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Double");
        map.put("col_2", ""+df.format(par5DoubleBogeyCount));
        map.put("col_3", ""+df.format(par5DoubleBogeyCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Triple");
        map.put("col_2", ""+df.format(par5TripleBogeyCount));
        map.put("col_3", ""+df.format(par5TripleBogeyCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        map = new HashMap<String, String>();
        map.put("col_1", "Quadruple or Worse");
        map.put("col_2", ""+df.format(par5QuadBogeyPlusCount));
        map.put("col_3", ""+df.format(par5QuadBogeyPlusCount/numberOfPar5Holes*100)+"%");
        fillMaps.add(map);
        
        ListView lv = getListView();	
        
        //Sets a fading design as the divider line
        int[] colors = {0, 0xff347c12, 0};
        lv.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);
        
        //Displays the list using a special adapter
		SpecialAdapter adapter = new SpecialAdapter(PastRoundStats.this, fillMaps, R.layout.statisticsgrid, from, to);
		lv.setAdapter(adapter);
	}
	
	//Creates the list of players to be used by the player spinner
	private void getPlayerList(){    	    	
		InputStream filereader = null;
		InputStreamReader inputreader = null;
		BufferedReader bufferedreader = null;
	
		int numberOfPlayers = 0;
		
		//Opens the past round file to get the names of the players
		
		try {
			filereader = openFileInput("pastround"+pastRoundFileNumber+".txt");
			inputreader = new InputStreamReader(filereader);
			bufferedreader = new BufferedReader(inputreader);
			     
			//Disregards unnecessary info
			for(int x=0;x<3;x++){
				bufferedreader.readLine();
			}
			
			//Saves the number of players in the round
			numberOfPlayers = Integer.parseInt(bufferedreader.readLine());
			
			//Disregards unnecessary info
			for(int x=0;x<18*numberOfPlayers;x++){
				bufferedreader.readLine();
			}
			
			//Adds the player to the list
			for(int x=0;x<numberOfPlayers;x++){
				players.add(bufferedreader.readLine());
			}
		}catch (FileNotFoundException e) {
			Log.d("getPlayerList","File Not Found: pastround"+pastRoundFileNumber+".txt");
		}catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	//Sets all the statistic variables back to 0
	private void initializeStatisticVariables(){
		averageFront9Score = 0;
		averageFront9PlusMinus = 0;
		averageBack9Score = 0;
		averageBack9PlusMinus = 0;
		numberOfHoles = 0;
		numberOfPar3Holes = 0;
		numberOfPar4Holes = 0;
		numberOfPar5Holes = 0;
		albatrossCount = 0;
		eagleCount = 0;
		birdieCount = 0;
		parCount = 0;
		bogeyCount = 0;
		doubleBogeyCount = 0;
		tripleBogeyCount = 0;
		quadBogeyPlusCount = 0;
		par3EagleCount = 0;
		par3BirdieCount = 0;
		par3ParCount = 0;
		par3BogeyCount = 0;
		par3DoubleBogeyCount = 0;
		par3TripleBogeyCount = 0;
		par3QuadBogeyPlusCount = 0;
		par4AlbatrossCount = 0;
		par4EagleCount = 0;
		par4BirdieCount = 0;
		par4ParCount = 0;
		par4BogeyCount = 0;
		par4DoubleBogeyCount = 0;
		par4TripleBogeyCount = 0;
		par4QuadBogeyPlusCount = 0;
		par5AlbatrossCount = 0;
		par5EagleCount = 0;
		par5BirdieCount = 0;
		par5ParCount = 0;
		par5BogeyCount = 0;
		par5DoubleBogeyCount = 0;
		par5TripleBogeyCount = 0;
		par5QuadBogeyPlusCount = 0;
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
	
	//A special adapter used to give specific rows different visual properties
	public class SpecialAdapter extends SimpleAdapter{
		
		TextView t2 = (TextView)findViewById(R.id.pastdefaultColor);
		
		public SpecialAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
		    super(context, items, resource, from, to);
		}
		
		//Overrides the getView method so that the rows may be displayed differently
		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			TextView t1 = (TextView)view.findViewById(R.id.statisticsitem3);
			
			//Sets the +/- stats to a color of red, black, or black and "Even" depending on its value
			if(position<5&&position>0){
				if(Double.parseDouble(t1.getText().toString())<0){
					t1.setTextColor(Color.RED);
				}
				else if(Double.parseDouble(t1.getText().toString())==0){
					t1.setText("Even");
				}
				else{
					t1.setText("+"+t1.getText());
				}
			}
			else{
				t1.setTextColor(Color.BLACK);
			}
			
			//Sets the header rows to have a green background with gray bold text
			if (position==0 || position==5 || position==14 || position==22 || position==31){
				view.setBackgroundColor(0xff347c12);
				
				t1 = (TextView)view.findViewById(R.id.statisticsitem1);
				t1.setTextColor(t2.getTextColors().getDefaultColor());
				t1.setTypeface(null, Typeface.BOLD);
				
				t1 = (TextView)view.findViewById(R.id.statisticsitem2);
				t1.setTextColor(t2.getTextColors().getDefaultColor());
				t1.setTypeface(null, Typeface.BOLD);
					
				t1 = (TextView)view.findViewById(R.id.statisticsitem3);
				t1.setTextColor(t2.getTextColors().getDefaultColor());
				t1.setTypeface(null, Typeface.BOLD);
			}
			else{
				view.setBackgroundColor(0xffffffff);
				
				t1 = (TextView)view.findViewById(R.id.statisticsitem1);
				t1.setTextColor(0xff000000);
				t1.setTypeface(null, Typeface.NORMAL);
				
				t1 = (TextView)view.findViewById(R.id.statisticsitem2);
				t1.setTextColor(0xff000000);
				t1.setTypeface(null, Typeface.NORMAL);
					
				t1 = (TextView)view.findViewById(R.id.statisticsitem3);
				if(position>5){
					t1.setTextColor(0xff000000);
				}
				t1.setTypeface(null, Typeface.NORMAL);
			}
			
			return view;
		}
	}
}
