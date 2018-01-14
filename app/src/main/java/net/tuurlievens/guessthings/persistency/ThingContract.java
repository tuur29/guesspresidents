package net.tuurlievens.guessthings.persistency;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ThingContract {
    public static final String CONTENT_AUTHORITY = "net.tuurlievens.guessthings.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Tables specific path:
    public static final String RELATIVE_Thing_URI = "Thing";

    public static class Thing {
        // URI for the table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(RELATIVE_Thing_URI).build();

        // Entire table
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.net.tuurlievens.guessthings.provider.Thing";
        // Single row within the table
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.net.tuurlievens.guessthings.provider.Thing";

        // Table name
        public static final String TABLE_NAME = "Thing";

        // Define table columns
        public interface Columns extends BaseColumns {
            String NAME = "name";
            String DESCRIPTION = "description";
            String TAGS = "tags";
            String IMAGEURL = "imageurl";
        }

        public static Uri buildRowUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
