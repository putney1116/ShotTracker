package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.example.android.ShotTracker.db.BagDAO;
import com.example.android.ShotTracker.db.ClubDAO;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.objects.Club;
import com.example.android.ShotTracker.objects.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by damcglinchey on 5/27/15.
 */
public class ManageBags extends Activity {

    private AlertDialog.Builder builder;

    private String selectedPlayer = null;
//    List<CheckBox> checkClubs = null;
    Map<CheckBox, Club> checkClubMap= null;

    /**
     * Called when first created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managebags);

        //Initializes the dialog box that is displayed when the back button is pressed
        buildDialog();

        //initialize the list of check boxes
//        checkClubs = new ArrayList<CheckBox>();
        checkClubMap = new HashMap<CheckBox, Club>();

        //Set up the player spinner
        setPlayerSpinner();

        //set up the check boxes for all clubs
        setupCheckBoxes();

        //set up a save button on the bottom
        saveBagButtonInitializer();
    }

    //Sets the list of players in the spinner
    private void setPlayerSpinner() {
        PlayerDAO playerDAO = new PlayerDAO(this);
        final List<String> players = playerDAO.readListofPlayerNameswDefaultFirst();

        //set the default
        selectedPlayer = players.get(0);
        updateCheckBoxes();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageBags.this, android.R.layout.simple_spinner_item, players);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.bagplayer);
        spinner.setAdapter(adapter);

        //Sets the map location to the correct hole when the hole number is changed
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedPlayer = players.get(pos);
                updateCheckBoxes();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }




    //Generate the list of buttons
    private void setupCheckBoxes() {

        // Put the club check boxes in a scrollable field
        ScrollView sv = (ScrollView)findViewById(R.id.clubscroll);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        // Read all clubs in the DB
        ClubDAO clubDAO = new ClubDAO(this);
        final List<Club> clubs = clubDAO.readListofClubs();


        // Make a radio button for every club in the DB
        for (Club club : clubs) {
            CheckBox cb = new CheckBox(this);
            cb.setPadding(20, 10, 10, 10);
            cb.setText(club.getClub());
            cb.setChecked(false);
            checkClubMap.put(cb, club);
            ll.addView(cb);
        }
    }

    //Update the checkboxes for the given players clubs
    private void updateCheckBoxes() {

        // Get the Player from the spinner
        PlayerDAO playerDAO = new PlayerDAO(this);
        long pID = playerDAO.readIDFromName(selectedPlayer);
        Player player = new Player();
        player.setID(pID);

        // Get the clubs in the players bag
        BagDAO bagDAO = new BagDAO(this);
        List<Club> playerClubs = bagDAO.readClubsInBag(player);

        // Run through all the buttons
        for (Map.Entry<CheckBox, Club> entry : checkClubMap.entrySet()) {
                //reset all to unchecked
                entry.getKey().setChecked(false);
                for (Club pclub : playerClubs) {
                    if (pclub.getID() == entry.getValue().getID())
                    {
                        entry.getKey().setChecked(true);
                        break;
                    }
                }
        }

    }


    //Saves the settings and returns to the home screen
    private void saveBagButtonInitializer(){
        final Button saveSettingsButton = (Button)findViewById(R.id.savebagbutton);

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBag();

                //Closes the present activity and returns the display to the home screen
                finish();
            }
        });
    }


    private void saveBag() {


        // Get the Player from the spinner
        PlayerDAO playerDAO = new PlayerDAO(this);
        long pID = playerDAO.readIDFromName(selectedPlayer);
        Player player = new Player();
        player.setID(pID);

        // Get the clubs in the players bag
        BagDAO bagDAO = new BagDAO(this);
        List<Club> playerClubs = bagDAO.readClubsInBag(player);

        // Run through all the buttons and add and remove clubs
        for (Map.Entry<CheckBox, Club> entry : checkClubMap.entrySet()) {
            if (entry.getKey().isChecked()) {
                boolean exists = false;
                for (Club pclub : playerClubs) {
                    if (pclub.getID() == entry.getValue().getID())
                    {
                        exists = true;
                        break;
                    }
                }

                // if the club isn't already in the bag, add it
                if (!exists) {
                    bagDAO.createClubToBag(player, entry.getValue());
                }
            }
            else
            {
                boolean exists = false;
                for (Club pclub : playerClubs) {
                    if (pclub.getID() == entry.getValue().getID())
                    {
                        exists = true;
                        break;
                    }
                }

                //if the club was unchecked delete it from the DB
                if (exists) {
                    bagDAO.deleteClubFromBag(player, entry.getValue());
                }
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
        builder = new AlertDialog.Builder(ManageBags.this);
        builder.setMessage("Would you like to save the Bag?");
        builder.setCancelable(true);

        //If the user would like to save the settings
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                saveBag();

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
