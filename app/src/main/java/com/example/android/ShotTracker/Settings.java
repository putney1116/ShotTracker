package com.example.android.ShotTracker;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.objects.Player;

import java.util.List;

public class Settings extends Activity{

	private AlertDialog.Builder builder;

    private String playerDefault = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.settings);
	    	
        //Initializes the button to manage players
        managePlayersButtonInitializer();

        //Initializes the button to manage players bag of clubs
        manageBagButtonInitializer();

	}


    //Opens new activity to manage the clubs in a players bag
    private void managePlayersButtonInitializer() {
        final Button managePlayersButton = (Button)findViewById(R.id.manageplayers);

        managePlayersButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    Intent myIntent = new Intent(v.getContext(), ManagePlayers.class);
                    startActivity(myIntent);

                }catch(Exception e) {
                }
            }
        });
    }

    //Opens new activity to manage the clubs in a players bag
    private void manageBagButtonInitializer() {
        final Button manageBagsButton = (Button)findViewById(R.id.managebag);

        manageBagsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    Intent myIntent = new Intent(v.getContext(), ManageBags.class);
                    startActivity(myIntent);

                }catch(Exception e) {
                }
            }
        });
    }

}
