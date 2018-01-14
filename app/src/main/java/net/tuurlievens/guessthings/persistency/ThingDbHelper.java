package net.tuurlievens.guessthings.persistency;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.tuurlievens.guessthings.persistency.ThingContract.Thing.Columns;

public class ThingDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "things.db";

    private static final String CREATE_TABLE_Thing = "CREATE TABLE " + ThingContract.Thing.TABLE_NAME + "("
        + Columns._ID + " INTEGER PRIMARY KEY,"
        + Columns.NAME + " TEXT NOT NULL,"
        + Columns.DESCRIPTION + " TEXT NOT NULL,"
        + Columns.TAGS + " TEXT NOT NULL,"
        + Columns.IMAGEURL + " TEXT NOT NULL)";

    private static final String DROP_TABLE_Thing = "DROP TABLE IF EXISTS " + ThingContract.Thing.TABLE_NAME;

    public ThingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private String generateInsertQuery(String name, String description, String tags, String imageurl) {
        return "INSERT INTO " + ThingContract.Thing.TABLE_NAME
                + "(" + Columns.NAME + "," + Columns.DESCRIPTION + "," + Columns.TAGS + "," + Columns.IMAGEURL + ")"
                + "VALUES ('"+name+"', '"+description+"','"+tags+"','"+imageurl+"')";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_Thing);
        sqLiteDatabase.execSQL(generateInsertQuery("Lamp","A magic device that generates light","home,device,light,electricity","https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/White_lamp.JPG/352px-White_lamp.JPG"));
        sqLiteDatabase.execSQL(generateInsertQuery("Duck","A bird that lives on water and makes squawking noises","animal,bird,water","https://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/Anas_falcata.JPG/480px-Anas_falcata.JPG"));
        sqLiteDatabase.execSQL(generateInsertQuery("Machu Picchu","An ancient ruin high up in the mountains","place,inca,ruin","https://upload.wikimedia.org/wikipedia/commons/thumb/0/01/80_-_Machu_Picchu_-_Juin_2009_-_edit.2.jpg/492px-80_-_Machu_Picchu_-_Juin_2009_-_edit.2.jpg"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // on upgrade drop older tables
        sqLiteDatabase.execSQL(DROP_TABLE_Thing);
        // create new tables
        onCreate(sqLiteDatabase);
    }
}
