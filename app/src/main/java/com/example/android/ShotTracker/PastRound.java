package com.example.android.ShotTracker;

import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class PastRound extends TabActivity{
	
	private TabHost tabHost;
    
	public void onCreate(Bundle savedInstanceState) {
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
				
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.pastroundmainscreen);
    	
    	//Initializes the tab controller
    	tabSetup();   	
	}
    
    //Initializes the tab views
    private void tabSetup() {
    	tabHost = getTabHost();
    	
    	Resources res = getResources();
    	
    	TabSpec spec1;
    	TabSpec spec2;
    	TabSpec spec3;
    	
    	Intent myIntent = getIntent();
    	
    	long roundID = myIntent.getLongExtra("RoundID", -1);

    	//Initializes the scorecard tab
    	spec1=tabHost.newTabSpec("Tab 1");
    	spec1.setIndicator("Score Card", res.getDrawable(R.drawable.ic_tab3));
    	Intent in1 = new Intent(this, PastRoundScorecard.class);
    	in1.putExtra("RoundID", roundID);
    	spec1.setContent(in1);
    	
    	//Initializes the map view tab
    	spec2=tabHost.newTabSpec("Tab 2");
    	spec2.setIndicator("Map", res.getDrawable(R.drawable.ic_tab2));
    	Intent in2 = new Intent(this, PastRoundMap.class);
    	in2.putExtra("RoundID", roundID);
    	spec2.setContent(in2);
    	
    	//Initializes the statistics tab
    	spec3=tabHost.newTabSpec("Tab 3");
    	spec3.setIndicator("Stats", res.getDrawable(R.drawable.ic_tab4));
    	Intent in3 = new Intent(this, PastRoundStats.class);
    	in3.putExtra("RoundID", roundID);
    	spec3.setContent(in3);
    	
    	tabHost.addTab(spec1);
    	tabHost.addTab(spec2);
    	tabHost.addTab(spec3);
    	
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	
    	//Handles when tabs are changed
    	tabHost.setOnTabChangedListener(new OnTabChangeListener() {
    	       @Override
    	       public void onTabChanged(String arg0) {
    	    	   	if(tabHost.getCurrentTab()==0){
    	    	   		//Changes the layout to landscape if the scorecard tab is selected
    	    	   		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	    	   	}
    	    	   	else{
    	    	   		//Changes the layout to portrait if any other tab is selected
	    	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	    	   	}	
    	       }     
    	 });  	
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

