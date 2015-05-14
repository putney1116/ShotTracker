package com.example.android.ShotTracker;

import android.content.Context;
import android.view.View;

import com.example.android.ShotTracker.objects.Shot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Justin_Ayvazian on 5/13/2015.
 */
public class ViewEditCurrentRound extends com.google.android.maps.MapActivity implements View.OnClickListener, GoogleMap.OnMapClickListener {
    private Context contextIn;
    private Shot shotTableIn;

    public ViewEditCurrentRound(Context context){
        contextIn = context;

    }
    public List[] ViewEditCurrentRoundMain(List[] shotList, double[][][] greenLocations, double[][] teeLocations){

        //greenLocations[latlng][0(front)/1(middle)/2(back)][hole number - human readable, 0 index is blank, hole numbers and index numbers are 1-18 for 19 length array]
        //teeLocations[latlng][hole number - same weirdness with indices as greenlocations]
        //When assigning something to shotTableOut(1), type must be List<Shot>
        //Going to need go use List methods to access indicies of arrays, both by hole and by shot within each hole
        return shotList;
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
