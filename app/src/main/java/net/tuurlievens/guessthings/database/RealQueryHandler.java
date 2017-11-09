package net.tuurlievens.guessthings.database;

import android.app.Activity;
import android.content.ContentValues;

import net.tuurlievens.guessthings.Thing;

public class RealQueryHandler {

    private final QueryHandler queryHandler;

    public RealQueryHandler(Activity parentActivity) {
        this.queryHandler = new QueryHandler(parentActivity, null);
    }

    public void insert(Thing thing) {
        ContentValues values = convertToContentValues(thing);
        this.queryHandler.startInsert(QueryHandler.OperationToken.TOKEN_INSERT,
                null, ThingContract.Thing.CONTENT_URI, values);
    }

    public void update(Thing thing) {
        ContentValues values = convertToContentValues(thing);

        // make query
        String selection = ThingContract.Thing.Columns._ID + " = ?";
        String[] selectionArg = {String.valueOf(thing.id)};
        this.queryHandler.startUpdate(QueryHandler.OperationToken.TOKEN_UPDATE,
                null, ThingContract.Thing.CONTENT_URI, values, selection, selectionArg);
    }

    public void delete(int id) {
        String selection = ThingContract.Thing.Columns._ID + " = ?";
        this.queryHandler.startDelete(QueryHandler.OperationToken.TOKEN_DELETE,
                null, ThingContract.Thing.CONTENT_URI, selection, new String[]{String.valueOf(id)});
    }

    private ContentValues convertToContentValues(Thing thing) {
        ContentValues values = new ContentValues();
        values.put(ThingContract.Thing.Columns.NAME, thing.name);
        values.put(ThingContract.Thing.Columns.DESCRIPTION, thing.descr);
        values.put(ThingContract.Thing.Columns.TAGS, thing.tags);
        values.put(ThingContract.Thing.Columns.IMAGEURL, thing.imageurl);
        return values;
    }
}
