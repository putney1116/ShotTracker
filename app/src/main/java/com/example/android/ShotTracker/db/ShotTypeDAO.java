package com.example.android.ShotTracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.android.ShotTracker.objects.ShotType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ewjensen on 2/7/15.
 */
public class ShotTypeDAO extends ShotTrackerDBDAO {

    private static final String WHERE_SHOTTYPEID_EQUALS = DataBaseHelper.SHOTTYPEID_COLUMN
            + "=?";

    public ShotTypeDAO(Context context){ super(context);}

    /**
     * creates a new shottype entry in the database
     * @param shotType
     * @return
     */
    public long createShotType(ShotType shotType){
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.SHOTTYPE_COLUMN, shotType.getType());

        return database.insert(DataBaseHelper.SHOTTYPE_TABLE, null, values);
    }

    /**
     * update shotType entry in database
     * @param shotType
     * @return
     */
    public long updateShotType(ShotType shotType){
        if (shotType.getID() < 0){
            throw new RuntimeException("shottype ID not set in updateShotType()");
        }

        ContentValues values =  new ContentValues();

        values.put(DataBaseHelper.SHOTTYPE_COLUMN, shotType.getType());

        return database.update(DataBaseHelper.SHOTTYPE_TABLE, values,
                WHERE_SHOTTYPEID_EQUALS,
                new String[]{String.valueOf(shotType.getID())});
    }

    /**
     * delete shotType entry in db
     * @param shotType
     * @return
     */
    public long deleteShotType(ShotType shotType){
        if (shotType.getID() < 0){
            throw new RuntimeException("shottype ID not set in updateShotType()");
        }
        return database.delete(DataBaseHelper.SHOTTYPE_TABLE,
                WHERE_SHOTTYPEID_EQUALS,
                new String[]{String.valueOf(shotType.getID())});
    }

    /**
     * returns a shotType given a shotType ID
     * @param shotType
     * @return
     */
    public ShotType readShotType(ShotType shotType){
        if (shotType.getID() < 0){
            throw new RuntimeException("shottype ID not set in updateShotType()");
        }

        Cursor cursor = database.query(DataBaseHelper.SHOTTYPE_TABLE,
                new String[]{DataBaseHelper.SHOTTYPEID_COLUMN,
                    DataBaseHelper.SHOTTYPE_COLUMN},
                WHERE_SHOTTYPEID_EQUALS,
                new String[]{String.valueOf(shotType.getID())},
                null,null,null);

        if (cursor.getCount() != 1){
            throw new RuntimeException("DB query returned " + cursor.getCount() +
            ", expected 1 in readShotType()");
        }

        shotType.setType(cursor.getString(1));
        cursor.close();

        return shotType;
    }

    /**
     * returns a list of all shottypes
     * @return
     */
    public List<ShotType> readListofShotTypes(){
        List<ShotType> shotTypes = new ArrayList<ShotType>();

        Cursor cursor = database.query(DataBaseHelper.SHOTTYPE_TABLE,
                new String[]{DataBaseHelper.SHOTTYPEID_COLUMN,
                        DataBaseHelper.SHOTTYPE_COLUMN},
                null,null,null,null,null);

        while (cursor.moveToNext()){
            ShotType shotType = new ShotType();
            shotType.setID(cursor.getLong(0));
            shotType.setType(cursor.getString(1));

            shotTypes.add(shotType);
        }
        cursor.close();

        return shotTypes;
    }
}
