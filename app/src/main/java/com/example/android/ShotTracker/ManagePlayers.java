package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.objects.Player;

import java.util.List;

/**
 * Created by damcglinchey on 5/27/15.
 */
public class ManagePlayers extends Activity {
    private AlertDialog.Builder builder;

    private String playerDefault = null;


    /**
     * Called when first created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manageplayers);

        //Initializes the dialog box that is displayed when the back button is pressed
        buildDialog();

        //Sets the hints in the editable text boxes
        setDefaultPlayerSpinner();

        //Initializes the button that saves the settings
        saveSettingsButtonInitializer();



    }

    //Sets the list of players in the spinner
    private void setDefaultPlayerSpinner() {
        PlayerDAO playerDAO = new PlayerDAO(this);
        final List<String> players = playerDAO.readListofPlayerNameswDefaultFirst();


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManagePlayers.this, android.R.layout.simple_spinner_item, players);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //player 1
        Spinner spinner1 = (Spinner) findViewById(R.id.defaultplayer);
        spinner1.setAdapter(adapter);

        //Sets the map location to the correct hole when the hole number is changed
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                playerDefault = players.get(pos);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    //Saves the settings and returns to the home screen
    private void saveSettingsButtonInitializer(){
        final Button saveSettingsButton = (Button)findViewById(R.id.savesettingsbutton);

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();

                //Closes the present activity and returns the display to the home screen
                finish();
            }
        });
    }

    private void saveSettings() {
        //First get the current default player
        PlayerDAO playerDAO = new PlayerDAO(this);
        Player currentdef = playerDAO.readUserDefaultPlayer();

        //Test if if the selected name is different
        if (currentdef.getName() != playerDefault && playerDefault != null) {
            // Get the newly selected player
            long pID = playerDAO.readIDFromName(playerDefault);
            if (pID > 0) {
                Player newdef = new Player();
                newdef.setID(pID);
                newdef = playerDAO.readPlayer(newdef);

                // unset the old default and update
                currentdef.setUserDefault(false);
                playerDAO.update(currentdef);

                // set the new default and update
                newdef.setUserDefault(true);
                playerDAO.update(newdef);
            }
        }
    }


    //If the back button is pressed, the dialog to save the settings is called if changes have been made
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //If changes have been made, the dialog box asking for save confirmation is displayed
            builder.show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Dialog asks the user if they would like to save the settings
    private void buildDialog(){
        builder = new AlertDialog.Builder(ManagePlayers.this);
        builder.setMessage("Would you like to save your settings?");
        builder.setCancelable(true);

        //If the user would like to save the settings
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                saveSettings();

                //Closes the present activity and returns the display to the home screen
                finish();
            }
        });

        //If the user would not like to save the settings
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Closes the present activity and returns the display to the home screen
                finish();
            }
        });

        //If the user would like to cancel pressing the back button
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //The dialog is closed
                dialog.cancel();
            }
        });
    }


}
