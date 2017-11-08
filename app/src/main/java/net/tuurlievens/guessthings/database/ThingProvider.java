package net.tuurlievens.guessthings.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ThingProvider extends ContentProvider {
    private static final int Thing_TABLE_ID = 100;
    private static final int Thing_ROW_ID = 101;
    private ThingDbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ThingContract.CONTENT_AUTHORITY, ThingContract.RELATIVE_Thing_URI, Thing_TABLE_ID);
        sUriMatcher.addURI(ThingContract.CONTENT_AUTHORITY, ThingContract.RELATIVE_Thing_URI + "/#", Thing_ROW_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ThingDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case Thing_TABLE_ID:
                return ThingContract.Thing.CONTENT_TYPE;

            case Thing_ROW_ID:
                return ThingContract.Thing.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case Thing_TABLE_ID:
                retCursor = db.query(ThingContract.Thing.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case Thing_ROW_ID:
                long _id = ContentUris.parseId(uri);
                retCursor = db.query(ThingContract.Thing.TABLE_NAME, projection, ThingContract.Thing
                    .Columns._ID + " = ?", new String[]{String.valueOf(_id)}, null, null,
                    sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // send notification to any attached content observers.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long _id;
        Uri returnUri;

        if (sUriMatcher.match(uri) == Thing_TABLE_ID) {
            _id = db.insert(ThingContract.Thing.TABLE_NAME, null, contentValues);
            if (_id > 0) {
                returnUri = ThingContract.Thing.buildRowUri(_id);
            } else {
                throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
            }
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rows; // Number of rows effected
        if (sUriMatcher.match(uri) == Thing_TABLE_ID) {
            rows = db.delete(ThingContract.Thing.TABLE_NAME, selection, selectionArgs);
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because null could delete all rows:
        if (selection == null || rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rows = 0;
        if (sUriMatcher.match(uri) == Thing_TABLE_ID) {
            rows = db.update(ThingContract.Thing.TABLE_NAME, values, selection, selectionArgs);
        } else {
            throwUnsupportedUriException(uri);
        }
        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    private void throwUnsupportedUriException(Uri uri) {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
}
