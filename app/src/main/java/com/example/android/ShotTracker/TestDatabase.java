package com.example.android.ShotTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import java.util.List;

import com.example.android.ShotTracker.db.DataBaseHelper;
import com.example.android.ShotTracker.db.PlayerDAO;
import com.example.android.ShotTracker.objects.Player;

/**
 * Activity for testing the database
 */
public class TestDatabase extends Activity {
    private AlertDialog.Builder builder;

    private static final String TAG = "TestDatabase";

    /** Called when the activity is first created */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //db = new DatabaseHelper(getApplicationContext());

        Log.d(TAG, "A comment to the log.");

        PlayerDAO pd = new PlayerDAO(getApplicationContext());

        pd.loadPlayers();

        List<Player> players = pd.readPlayers();

        Log.e(TAG, "Number of Players = " + players.size());

        for (Player player : players){
            Log.d(TAG, player.getName());
        }

        Log.d(TAG, "Finish------");
    }
}
