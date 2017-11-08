package net.tuurlievens.guessthings.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.tuurlievens.guessthings.database.ThingContract.Thing.Columns;

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

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_Thing);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // on upgrade drop older tables
        sqLiteDatabase.execSQL(DROP_TABLE_Thing);
        // create new tables
        onCreate(sqLiteDatabase);
    }
}
