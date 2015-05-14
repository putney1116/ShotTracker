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

import com.example.android.ShotTracker.db.DAOUtilities;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.db.StatistisDAO;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;

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
	 

	//Calculates the total stats for the specified player
	private void calculatePlayerStats(int pos){

        //Sets all the statistic variables back to 0
        initializeStatisticVariables();

        StatistisDAO statDAO = new StatistisDAO(PastRoundStats.this);
        DAOUtilities daoUtil = new DAOUtilities(PastRoundStats.this);
        PlayerDAO playerDAO = new PlayerDAO(PastRoundStats.this);

        Player player = new Player();
        long id = playerDAO.readIDFromName(players.get(pos));
        player.setID(id);

        //Loads the course name from the previous activity
        Intent myIntent = getIntent();
        Long roundID = myIntent.getLongExtra("RoundID", -1);

        Round round = new Round();
        round.setID(roundID);

        // get the number of each holes
        numberOfPar3Holes = statDAO.getNHolesPar(3, player, round);
        numberOfPar4Holes = statDAO.getNHolesPar(4, player, round);
        numberOfPar5Holes = statDAO.getNHolesPar(5, player, round);
        numberOfHoles = numberOfPar3Holes
                + numberOfPar4Holes
                + numberOfPar5Holes;

        // get the counts for par 3's
        par3EagleCount = statDAO.getNHolesParScore(3, -2, player, round);
        par3BirdieCount = statDAO.getNHolesParScore(3, -1, player, round);
        par3ParCount = statDAO.getNHolesParScore(3, 0, player, round);
        par3BogeyCount = statDAO.getNHolesParScore(3, 1, player, round);
        par3DoubleBogeyCount = statDAO.getNHolesParScore(3, 2, player, round);
        par3TripleBogeyCount = statDAO.getNHolesParScore(3, 3, player, round);
        //\todo this currently only gets +4, get everything >= +4
        par3QuadBogeyPlusCount = statDAO.getNHolesParScore(3, 4, player, round);

        // get the counts for par 4's
        par4AlbatrossCount = statDAO.getNHolesParScore(4, -3, player, round);
        par4EagleCount = statDAO.getNHolesParScore(4, -2, player, round);
        par4BirdieCount = statDAO.getNHolesParScore(4, -1, player, round);
        par4ParCount = statDAO.getNHolesParScore(4, 0, player, round);
        par4BogeyCount = statDAO.getNHolesParScore(4, 1, player, round);
        par4DoubleBogeyCount = statDAO.getNHolesParScore(4, 2, player, round);
        par4TripleBogeyCount = statDAO.getNHolesParScore(4, 3, player, round);
        par4QuadBogeyPlusCount = statDAO.getNHolesParScore(4, 4, player, round);

        // get the counts for the par 5's
        par5AlbatrossCount = statDAO.getNHolesParScore(5, -3, player, round);
        par5EagleCount = statDAO.getNHolesParScore(5, -2, player, round);
        par5BirdieCount = statDAO.getNHolesParScore(5, -1, player, round);
        par5ParCount = statDAO.getNHolesParScore(5, 0, player, round);
        par5BogeyCount = statDAO.getNHolesParScore(5, 1, player, round);
        par5DoubleBogeyCount = statDAO.getNHolesParScore(5, 2, player, round);
        par5TripleBogeyCount = statDAO.getNHolesParScore(5, 3, player, round);
        par5QuadBogeyPlusCount = statDAO.getNHolesParScore(5, 4, player, round);

        // sum various scores
        albatrossCount = par4AlbatrossCount + par5AlbatrossCount;
        eagleCount = par3EagleCount + par4EagleCount + par5EagleCount;
        birdieCount = par3BirdieCount + par4BirdieCount + par5BirdieCount;
        parCount = par3ParCount + par4ParCount + par5ParCount;
        bogeyCount = par3BogeyCount + par4BogeyCount + par5BogeyCount;
        doubleBogeyCount = par3DoubleBogeyCount
                + par4DoubleBogeyCount
                + par5DoubleBogeyCount;
        tripleBogeyCount = par3TripleBogeyCount
                + par4TripleBogeyCount
                + par5TripleBogeyCount;
        quadBogeyPlusCount = par3QuadBogeyPlusCount
                + par4QuadBogeyPlusCount
                + par5QuadBogeyPlusCount;






		//Calls the method that displays the stats on the screen
		fillInList();
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

        PlayerDAO playerDAO = new PlayerDAO(PastRoundStats.this);
        players = playerDAO.readListofPlayerNameswDefaultFirst();

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
