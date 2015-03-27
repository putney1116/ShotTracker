package com.example.android.ShotTracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.ShotTracker.db.CourseDAO;
import com.example.android.ShotTracker.db.CourseHoleDAO;
import com.example.android.ShotTracker.db.CourseHoleInfoDAO;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.db.RoundDAO;
import com.example.android.ShotTracker.db.RoundHoleDAO;
import com.example.android.ShotTracker.db.SubCourseDAO;
import com.example.android.ShotTracker.db.SubRoundDAO;
import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.CourseHoleInfo;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;
import com.example.android.ShotTracker.objects.SubCourse;
import com.example.android.ShotTracker.objects.SubRound;

import java.util.ArrayList;
import java.util.List;

public class PastRoundScorecard extends Activity implements OnClickListener{
	
	private TextView scoreEntryScorecard;
	private TextView scorecardTotalText;
	private TextView scorecardPlusMinusText;
	private RelativeLayout scorecardTab; 
	private int pastRoundFileNumber;
	private boolean frontActive = true;
	private int holeScore[][] = new int[5][19];
	String playerName[] = {"","","","",""};	
	
	private String courseName = "";
	private String fileName = "";
	private int[] par = new int[19];
	private int blueYardage[] = new int[19];
	private int whiteYardage[] = new int[19];
	private int redYardage[] = new int[19];
	private int menHandicap[] = new int[19];
	private int womenHandicap[] = new int[19];
	private int numberOfPlayers = 0;

    private Round round = null;
    private List <SubRound> subRounds = null;

    private RoundDAO roundDAO = null;
    private SubRoundDAO subRoundDAO = null;
    
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
    	setContentView(R.layout.pastscorecardview);
    	
    	//Loads the player names and course information
    	loadCourseInfo();
    	
    	//Loads the round's information
    	loadPastRound();
    	
    	//Initializes the scorecard tab
    	scorecardInitializer();
    	
    	//Loads the front 9 values
    	frontBackViewSwitcher();
	}
	
	//Loads the player names and course information
    //Loads the player names and course information
    private void loadCourseInfo(){

        CourseDAO courseDAO = new CourseDAO(this);
        SubCourseDAO subCourseDAO = new SubCourseDAO(this);
        CourseHoleDAO courseHoleDAO = new CourseHoleDAO(this);
        CourseHoleInfoDAO courseHoleInfoDAO = new CourseHoleInfoDAO(this);
        subRoundDAO = new SubRoundDAO(this);
        roundDAO = new RoundDAO(this);

        Intent myIntent = getIntent();

        //Loads the course name from the previous activity
        Long roundID = myIntent.getLongExtra("RoundID", -1);

        round = roundDAO.readRoundFromID(roundID);

        subRounds = subRoundDAO.readListofSubRounds(round);
        List <SubCourse> subCourses = new ArrayList<SubCourse>();

        for (SubRound subRound : subRounds){
            subCourses.add(subCourseDAO.readSubCourseFromID(subRound.getSubCourseID()));
        }

        Course course = courseDAO.readCourseFromID(subCourses.get(0).getCourseID());

        //Saves the official course name
        courseName = course.getName();

        numberOfPlayers = myIntent.getIntExtra("Players", 0);

        //Loads the player names from the previous activity
        for(int x=1;x<=numberOfPlayers;x++){
            playerName[x] = myIntent.getStringExtra("Player"+(x-1));
        }

        for (SubCourse subCourse : subCourses){
            List<CourseHole> courseHoles = courseHoleDAO.readListofCourseHoles(subCourse);

            for (CourseHole courseHole : courseHoles){
                List<CourseHoleInfo> courseHoleInfos = courseHoleInfoDAO.readListofCourseHoleInfos(courseHole);
                courseHole.setCourseHoleInfoList(courseHoleInfos);

                par[courseHole.getHoleNumber()] = courseHole.getPar();
                blueYardage[courseHole.getHoleNumber()] = courseHole.getBlueYardage();
                whiteYardage[courseHole.getHoleNumber()] = courseHole.getWhiteYardage();
                redYardage[courseHole.getHoleNumber()] = courseHole.getRedYardage();
                menHandicap[courseHole.getHoleNumber()] = courseHole.getMenHandicap();
                womenHandicap[courseHole.getHoleNumber()] = courseHole.getWomenHandicap();
            }
        }
    }
	
	//Loads the past round's information
	private void loadPastRound(){

        RoundHoleDAO roundHoleDAO = new RoundHoleDAO(this);
        CourseHoleDAO courseHoleDAO = new CourseHoleDAO(this);
        PlayerDAO playerDAO = new PlayerDAO(this);

        //\todo Get unique player list here, might want to be for each unique player number, add that player to the list to account for comments below
        //Issue with just unique player list is if the same player wants to have their name twice. We currently have no way to account for this
        //We could solve this by adding a player number column to the round hole. it would be 1 - 4.
        //Also, would make sure players stay in the same order when viewed in past rounds as they were when originally played
        List <Player> players = new ArrayList<Player>();
        Long id = playerDAO.readIDFromName("Darren");
        Player player1 = new Player();
        player1.setID(id);
        player1 = playerDAO.readPlayer(player1);
        players.add(player1);
        id = playerDAO.readIDFromName("Eric Putney");
        player1 = new Player();
        player1.setID(id);
        player1 = playerDAO.readPlayer(player1);
        players.add(player1);
        id = playerDAO.readIDFromName("Erik Jensen");
        player1 = new Player();
        player1.setID(id);
        player1 = playerDAO.readPlayer(player1);
        players.add(player1);
        id = playerDAO.readIDFromName("Justin");
        player1 = new Player();
        player1.setID(id);
        player1 = playerDAO.readPlayer(player1);
        players.add(player1);

        numberOfPlayers = players.size();

        int player_number = 0;

        for ( Player player : players) {
            player_number++;

            playerName[player_number] = player.getName();

            for (SubRound subRound : subRounds){

                List <RoundHole> roundHoles = roundHoleDAO.readListofRoundHoleRoundPlayer(subRound, player);

                for (RoundHole roundHole : roundHoles){

                    CourseHole courseHole = courseHoleDAO.readCourseHoleFromID(roundHole.getCourseHoleID());

                    holeScore[player_number][courseHole.getHoleNumber()] = roundHole.getScore();
                }
            }
        }
	}
	
	//Called when the front9 space is selected on the scorecard tab
	public void front9ButtonHandler(View view){		
		//Calls the methods that displays the values on the scorecard for the front 9
		frontActive = true;
		frontBackViewSwitcher();
		updateScorecard();
		
		//Calls the method that calculates the total score for each player
		for(int x=1;x<=numberOfPlayers;x++)
			updateScorecardTotals(x);
    }
	
	//Called when the back9 space is selected on the scorecard tab
    public void back9ButtonHandler(View view){		
  		//Calls the methods that display the values on the scorecard for the back 9
    	frontActive = false;
		frontBackViewSwitcher();
		updateScorecard();
		
		//Calls the method that calculates the total score for each player
		for(int x=1;x<=numberOfPlayers;x++)
			updateScorecardTotals(x);
    }
    
    //Updates the player score values on the scorecard tab
    private void updateScorecard(){
    	if(frontActive){   		
    		for(int x=1;x<=numberOfPlayers;x++){
    			for(int y=1;y<=9;y++){
    				//Loads the correct view from the scorecard tab
    				setTextViewHoleNumber(x,y);
		
    				//Displays the value on the scorecard if it is greater than 0
    				if(holeScore[x][y]==0)
    					scoreEntryScorecard.setText("");
    				else
    					scoreEntryScorecard.setText(Integer.toString(holeScore[x][y]));
    			}
    		}
    	}
    	else{
    		for(int x=1;x<=numberOfPlayers;x++){
    			for(int y=10;y<=18;y++){
    				//Loads the correct view from the scorecard tab
    				setTextViewHoleNumber(x,y);
		
    				//Displays the value on the scorecard if it is greater than 0
    				if(holeScore[x][y]==0)
    					scoreEntryScorecard.setText("");
    				else
    					scoreEntryScorecard.setText(Integer.toString(holeScore[x][y]));
    			}
    		}
    	}
    }
    
    //Updates the player score totals on the scorecard tab
    private void updateScorecardTotals(int playerNumber){
    	int nineHoles = 0, parTotal = 0, plusMinus = 0;
    	
    	//Calculates the total score and par if the score is greater than 0
    	if(frontActive){
			for(int x=1;x<=9;x++){
				if(holeScore[playerNumber][x]!=0)
					parTotal += par[x];
			
				nineHoles+=holeScore[playerNumber][x];
			}
		}
    	//Calculates the total score and par if the score is greater than 0
		else{
			for(int x=10;x<=18;x++){
				if(holeScore[playerNumber][x]!=0)
					parTotal += par[x];
				
				nineHoles+=holeScore[playerNumber][x];
			}
		}
    	
    	//Loads the correct view from the scorecard tab
    	setTextViewTotalPlayerNumber(playerNumber);
		
    	//The scorecard does not display 0 if there are no valid scores
		if(nineHoles==0){

			scorecardTotalText.setText("");
			scorecardPlusMinusText.setText("");
		}
		else{		
			//Displays the total score on the scorecard
			scorecardTotalText.setText(Integer.toString(nineHoles));
			
			//Calculates the total score to par
			plusMinus = nineHoles - parTotal;
	
			//Displays the score to par on the scorecard as either red, "Even", or black
			if(plusMinus==0){
				scorecardPlusMinusText.setTextColor(Color.BLACK);
				scorecardPlusMinusText.setText("Even");
			}
			else
				if(plusMinus<0){
					scorecardPlusMinusText.setTextColor(Color.RED);
					scorecardPlusMinusText.setText(Integer.toString(plusMinus));
				}
				else{
					scorecardPlusMinusText.setTextColor(Color.BLACK);
					scorecardPlusMinusText.setText("+"+Integer.toString(plusMinus));
				}
		}
    }
    
    //Loads the total and plus minus view from the scorecard tab for a player
    private void setTextViewTotalPlayerNumber(int playerNumber){
    	switch(playerNumber){
    		case 1:
    			scorecardTotalText = (TextView)findViewById(R.id.pasttotalPlayer1Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.pastplusMinusPlayer1Score);
    			break;
    		case 2:
    			scorecardTotalText = (TextView)findViewById(R.id.pasttotalPlayer2Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.pastplusMinusPlayer2Score);
    			break;
    		case 3:
    			scorecardTotalText = (TextView)findViewById(R.id.pasttotalPlayer3Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.pastplusMinusPlayer3Score);
    			break;
    		case 4:
    			scorecardTotalText = (TextView)findViewById(R.id.pasttotalPlayer4Score);
    			scorecardPlusMinusText = (TextView)findViewById(R.id.pastplusMinusPlayer4Score);
    			break;
    		default:
    			break;
    	}
    }
    
  //Loads the views for the cell, edit score buttons, and views above and below
    private void setTextViewHoleNumber(int player, int hole) {
    	switch(player){
    		case 1:
    			switch(hole) {
    				case 1:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole1Score);
    					break;
    				case 2:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole2Score); 
    					break;
    				case 3:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole3Score); 
    					break;
    				case 4:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole4Score); 
    					break;
    				case 5:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole5Score); 
    					break;
    				case 6:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole6Score); 
    					break;
    				case 7:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole7Score); 
    					break;
    				case 8:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole8Score); 
    					break;
    				case 9:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole9Score); 
    					break;
    				case 10:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole1Score); 
    					break;
    				case 11:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole2Score); 
    					break;
    				case 12:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole3Score); 
    					break;
    				case 13:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole4Score); 
    					break;
    				case 14:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole5Score); 
    					break;
    				case 15:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole6Score); 
    					break;
    				case 16:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole7Score); 
    					break;
    				case 17:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole8Score); 
    					break;
    				case 18:
    					scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole9Score); 
    					break;
    				default:
    					break;
    			}
    			break;
    		case 2:
        		switch(hole) {
        			case 1:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole1Score); 
        				break;
        			case 2:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole2Score); 
        				break;
        			case 3:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole3Score); 
        				break;
        			case 4:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole4Score); 
        				break;
        			case 5:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole5Score); 
        				break;
        			case 6:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole6Score); 
        				break;
        			case 7:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole7Score); 
        				break;
        			case 8:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole8Score); 
        				break;
        			case 9:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole9Score); 
        				break;
        			case 10:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole1Score); 
        				break;
        			case 11:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole2Score); 
        				break;
        			case 12:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole3Score); 
        				break;
        			case 13:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole4Score); 
        				break;
        			case 14:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole5Score); 
        				break;
        			case 15:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole6Score); 
        				break;
        			case 16:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole7Score); 
        				break;
        			case 17:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole8Score); 
        				break;
        			case 18:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer2Hole9Score); 
        				break;
        			default:
        				break;
        		}
        		break;
        	case 3:
            	switch(hole) {
        			case 1:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole1Score); 
        				break;
        			case 2:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole2Score); 
        				break;
        			case 3:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole3Score); 
        				break;
        			case 4:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole4Score); 
        				break;
        			case 5:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole5Score); 
        				break;
        			case 6:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole6Score); 
        				break;
        			case 7:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole7Score); 
        				break;
        			case 8:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole8Score); 
        				break;
        			case 9:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole9Score); 
        				break;
        			case 10:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole1Score); 
        				break;
        			case 11:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole2Score); 
        				break;
        			case 12:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole3Score); 
        				break;
        			case 13:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole4Score); 
        				break;
        			case 14:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole5Score); 
        				break;
        			case 15:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole6Score); 
        				break;
        			case 16:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole7Score); 
        				break;
        			case 17:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole8Score); 
        				break;
        			case 18:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer3Hole9Score); 
        				break;
        			default:
        				break;
            	}
        		break;
        	case 4:
            	switch(hole) {
            		case 1:
            			scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole1Score); 
            			break;
            		case 2:
            			scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole2Score); 
            			break;
        			case 3:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole3Score); 
        				break;
        			case 4:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole4Score); 
        				break;
        			case 5:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole5Score); 
        				break;
        			case 6:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole6Score); 
        				break;
        			case 7:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole7Score); 
        				break;
        			case 8:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole8Score); 
        				break;
        			case 9:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole9Score); 
        				break;
        			case 10:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole1Score); 
        				break;
        			case 11:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole2Score); 
        				break;
        			case 12:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole3Score); 
        				break;
        			case 13:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole4Score); 
        				break;
        			case 14:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole5Score);
        				break;
        			case 15:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole6Score); 
        				break;
        			case 16:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole7Score); 
        				break;
        			case 17:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole8Score); 
        				break;
        			case 18:
        				scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer4Hole9Score); 
        				break;
        			default:
        				break;
        		}
        		break;
        	default:
        		break;
    	}
    }
    
    //Loads the scorecard values for the front and back nines
    private void frontBackViewSwitcher(){
    	int nineHoles = 0;
    	TextView scorecardText;
    	RelativeLayout scorecardFrontBackButton;
    	
    	if(frontActive){
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.pastfront9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.pastback9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
    	}
    	else{
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.pastfront9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFCCCCCC);
    		scorecardFrontBackButton = (RelativeLayout)findViewById(R.id.pastback9Button);
    		scorecardFrontBackButton.setBackgroundColor(0xFFFFC775);
    	}
    	
    	//Loads and displays the blue tee yardage row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1Blue);
    		scorecardText.setText(Integer.toString(blueYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Blue);
    		scorecardText.setText(Integer.toString(blueYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Blue);
    		scorecardText.setText(Integer.toString(blueYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Blue);
    		scorecardText.setText(Integer.toString(blueYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Blue);
    		scorecardText.setText(Integer.toString(blueYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Blue);
    		scorecardText.setText(Integer.toString(blueYardage[6]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7Blue);
    		scorecardText.setText(Integer.toString(blueYardage[7]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8Blue);
    		scorecardText.setText(Integer.toString(blueYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Blue);
    		scorecardText.setText(Integer.toString(blueYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=blueYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1Blue);
    		scorecardText.setText(Integer.toString(blueYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Blue);
    		scorecardText.setText(Integer.toString(blueYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Blue);
    		scorecardText.setText(Integer.toString(blueYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Blue);
    		scorecardText.setText(Integer.toString(blueYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Blue);
    		scorecardText.setText(Integer.toString(blueYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Blue);
    		scorecardText.setText(Integer.toString(blueYardage[15]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7Blue);
    		scorecardText.setText(Integer.toString(blueYardage[16]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8Blue);
    		scorecardText.setText(Integer.toString(blueYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Blue);
    		scorecardText.setText(Integer.toString(blueYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=blueYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.pasttotalBlueText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the white tee yardage row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1White);
    		scorecardText.setText(Integer.toString(whiteYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2White);
    		scorecardText.setText(Integer.toString(whiteYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3White);
    		scorecardText.setText(Integer.toString(whiteYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4White);
    		scorecardText.setText(Integer.toString(whiteYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5White);
    		scorecardText.setText(Integer.toString(whiteYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6White);
    		scorecardText.setText(Integer.toString(whiteYardage[6]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7White);
    		scorecardText.setText(Integer.toString(whiteYardage[7]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8White);
    		scorecardText.setText(Integer.toString(whiteYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9White);
    		scorecardText.setText(Integer.toString(whiteYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=whiteYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1White);
    		scorecardText.setText(Integer.toString(whiteYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2White);
    		scorecardText.setText(Integer.toString(whiteYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3White);
    		scorecardText.setText(Integer.toString(whiteYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4White);
    		scorecardText.setText(Integer.toString(whiteYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5White);
    		scorecardText.setText(Integer.toString(whiteYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6White);
    		scorecardText.setText(Integer.toString(whiteYardage[15]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7White);
    		scorecardText.setText(Integer.toString(whiteYardage[16]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8White);
    		scorecardText.setText(Integer.toString(whiteYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9White);
    		scorecardText.setText(Integer.toString(whiteYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=whiteYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.pasttotalWhiteText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the men's handicap row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[1]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[2]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[3]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[4]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[5]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[6]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[7]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[8]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[9]));
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[10]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[11]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[12]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[13]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[14]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[15]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[16]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[17]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Hndcp);
    		scorecardText.setText(Integer.toString(menHandicap[18]));
    	}
    	
    	//Loads and displays the par row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1Par);
    		scorecardText.setText(Integer.toString(par[1]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Par);
    		scorecardText.setText(Integer.toString(par[2]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Par);
    		scorecardText.setText(Integer.toString(par[3]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Par);
    		scorecardText.setText(Integer.toString(par[4]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Par);
    		scorecardText.setText(Integer.toString(par[5]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Par);
    		scorecardText.setText(Integer.toString(par[6]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7Par);
    		scorecardText.setText(Integer.toString(par[7]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8Par);
    		scorecardText.setText(Integer.toString(par[8]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Par);
    		scorecardText.setText(Integer.toString(par[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=par[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1Par);
    		scorecardText.setText(Integer.toString(par[10]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Par);
    		scorecardText.setText(Integer.toString(par[11]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Par);
    		scorecardText.setText(Integer.toString(par[12]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Par);
    		scorecardText.setText(Integer.toString(par[13]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Par);
    		scorecardText.setText(Integer.toString(par[14]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Par);
    		scorecardText.setText(Integer.toString(par[15]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7Par);
    		scorecardText.setText(Integer.toString(par[16]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8Par);
    		scorecardText.setText(Integer.toString(par[17]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Par);
    		scorecardText.setText(Integer.toString(par[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=par[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.pasttotalParText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the red tee yardage row
    	nineHoles = 0;
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1Red);
    		scorecardText.setText(Integer.toString(redYardage[1]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Red);
    		scorecardText.setText(Integer.toString(redYardage[2]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Red);
    		scorecardText.setText(Integer.toString(redYardage[3]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Red);
    		scorecardText.setText(Integer.toString(redYardage[4]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Red);
    		scorecardText.setText(Integer.toString(redYardage[5]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Red);
	    	scorecardText.setText(Integer.toString(redYardage[6]));
	    	scorecardText = (TextView)findViewById(R.id.pasthole7Red);
	    	scorecardText.setText(Integer.toString(redYardage[7]));
	    	scorecardText = (TextView)findViewById(R.id.pasthole8Red);
    		scorecardText.setText(Integer.toString(redYardage[8]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Red);
    		scorecardText.setText(Integer.toString(redYardage[9]));
    		for(int x=1;x<=9;x++)
    			nineHoles+=redYardage[x];
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1Red);
    		scorecardText.setText(Integer.toString(redYardage[10]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2Red);
    		scorecardText.setText(Integer.toString(redYardage[11]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3Red);
    		scorecardText.setText(Integer.toString(redYardage[12]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4Red);
    		scorecardText.setText(Integer.toString(redYardage[13]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5Red);
    		scorecardText.setText(Integer.toString(redYardage[14]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6Red);
	    	scorecardText.setText(Integer.toString(redYardage[15]));
	    	scorecardText = (TextView)findViewById(R.id.pasthole7Red);
	    	scorecardText.setText(Integer.toString(redYardage[16]));
	    	scorecardText = (TextView)findViewById(R.id.pasthole8Red);
    		scorecardText.setText(Integer.toString(redYardage[17]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9Red);
    		scorecardText.setText(Integer.toString(redYardage[18]));
    		for(int x=10;x<=18;x++)
    			nineHoles+=redYardage[x];
    	}
    	scorecardText = (TextView)findViewById(R.id.pasttotalRedText);
    	scorecardText.setText(Integer.toString(nineHoles));
    	
    	//Loads and displays the women's handicap row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[1]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[2]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[3]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[4]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[5]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[6]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[7]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[8]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[9]));
    	}
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[10]));
    		scorecardText = (TextView)findViewById(R.id.pasthole2WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[11]));
    		scorecardText = (TextView)findViewById(R.id.pasthole3WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[12]));
    		scorecardText = (TextView)findViewById(R.id.pasthole4WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[13]));
    		scorecardText = (TextView)findViewById(R.id.pasthole5WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[14]));
    		scorecardText = (TextView)findViewById(R.id.pasthole6WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[15]));
    		scorecardText = (TextView)findViewById(R.id.pasthole7WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[16]));
    		scorecardText = (TextView)findViewById(R.id.pasthole8WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[17]));
    		scorecardText = (TextView)findViewById(R.id.pasthole9WHndcp);
    		scorecardText.setText(Integer.toString(womenHandicap[18]));
    	}
    	
    	//Loads and displays the top row
    	if(frontActive){
    		scorecardText = (TextView)findViewById(R.id.pasthole1Text);
        	scorecardText.setText("1");
        	scorecardText = (TextView)findViewById(R.id.pasthole2Text);
        	scorecardText.setText("2");
        	scorecardText = (TextView)findViewById(R.id.pasthole3Text);
        	scorecardText.setText("3");
        	scorecardText = (TextView)findViewById(R.id.pasthole4Text);
        	scorecardText.setText("4");
        	scorecardText = (TextView)findViewById(R.id.pasthole5Text);
        	scorecardText.setText("5");
        	scorecardText = (TextView)findViewById(R.id.pasthole6Text);
        	scorecardText.setText("6");
        	scorecardText = (TextView)findViewById(R.id.pasthole7Text);
        	scorecardText.setText("7");
        	scorecardText = (TextView)findViewById(R.id.pasthole8Text);
        	scorecardText.setText("8");
        	scorecardText = (TextView)findViewById(R.id.pasthole9Text);
        	scorecardText.setText("9");
    	}	
    	else{
    		scorecardText = (TextView)findViewById(R.id.pasthole1Text);
        	scorecardText.setText("10");
        	scorecardText = (TextView)findViewById(R.id.pasthole2Text);
        	scorecardText.setText("11");
        	scorecardText = (TextView)findViewById(R.id.pasthole3Text);
        	scorecardText.setText("12");
        	scorecardText = (TextView)findViewById(R.id.pasthole4Text);
        	scorecardText.setText("13");
        	scorecardText = (TextView)findViewById(R.id.pasthole5Text);
        	scorecardText.setText("14");
        	scorecardText = (TextView)findViewById(R.id.pasthole6Text);
        	scorecardText.setText("15");
        	scorecardText = (TextView)findViewById(R.id.pasthole7Text);
        	scorecardText.setText("16");
        	scorecardText = (TextView)findViewById(R.id.pasthole8Text);
        	scorecardText.setText("17");
        	scorecardText = (TextView)findViewById(R.id.pasthole9Text);
        	scorecardText.setText("18");
    	}
    }
    
    //Initializes the scorecard tab with the course name and players names
    private void scorecardInitializer() {  	
    	//Displays the course name in the top left corner
    	TextView scorecardText = (TextView)findViewById(R.id.pasttopLeftCorner);
        scorecardText.setText(courseName);
    	
    	//Displays the player names
    	scorecardText = (TextView)findViewById(R.id.pastplayer1Name);
    	scorecardText.setText(playerName[1]);
    	scorecardText = (TextView)findViewById(R.id.pastplayer2Name);
    	scorecardText.setText(playerName[2]);
    	scorecardText = (TextView)findViewById(R.id.pastplayer3Name);
    	scorecardText.setText(playerName[3]);
    	scorecardText = (TextView)findViewById(R.id.pastplayer4Name);
    	scorecardText.setText(playerName[4]);
    	
    	//Loads the scorecard view for later use
    	scorecardTab = (RelativeLayout)findViewById(R.id.pasttab1);
    	
    	//Initializes the scorecard views
    	scoreEntryScorecard = (TextView)findViewById(R.id.pastplayer1Hole1Score);
    
   		updateScorecard();
   		for(int x=1;x<=numberOfPlayers;x++){
   			updateScorecardTotals(x);
   		}
	}
    
    @Override
	public void onClick(View arg0) {
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
}

