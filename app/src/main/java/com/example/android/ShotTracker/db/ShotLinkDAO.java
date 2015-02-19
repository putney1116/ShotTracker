package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.Shot;
import com.example.android.ShotTracker.objects.ShotType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ewjensen on 2/7/15.
 */
public class ShotLinkDAO extends ShotTrackerDBDAO {

    private static final String WHERE_SHOTID_EQUALS = DataBaseHelper.SHOTID_COLUMN
            + "=?";

    public ShotLinkDAO(Context context){ super(context);}

    /**
     * create a shotlink table entry given shot and shottype IDs
     * @param shot
     * @param shotType
     * @return
     */
    public long createShotLink(Shot shot, ShotType shotType){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.SHOTID_COLUMN, shot.getID());
        values.put(DataBaseHelper.SHOTTYPEID_COLUMN, shotType.getID());

        return database.insert(DataBaseHelper.SHOTLINK_TABLE, null, values);
    }

    public List<ShotType> readListofShotTypesShot(Shot shot){
        if (shot.getID() < 0){
            throw new RuntimeException("shotID not set in ShotLinkDAO::readListofShotTypesShot");
        }

        List<ShotType> shotTypes = new ArrayList<ShotType>();

        Cursor cursor = database.query(DataBaseHelper.SHOTLINK_TABLE,
                new String[]{DataBaseHelper.SHOTLINKID_COLUMN,
                DataBaseHelper.SHOTID_COLUMN,
                DataBaseHelper.SHOTTYPEID_COLUMN},
                WHERE_SHOTID_EQUALS,
                new String[]{String.valueOf(shot.getID())},
                null,null,null);

        while (cursor.moveToNext()){
            ShotType shotType = new ShotType();
            shotType.setID(cursor.getInt(0));
            shotType.setType(cursor.getString(1));

            shotTypes.add(shotType);
        }
        cursor.close();
        return shotTypes;
    }
}
