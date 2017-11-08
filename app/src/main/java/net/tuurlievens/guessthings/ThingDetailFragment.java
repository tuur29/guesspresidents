package net.tuurlievens.guessthings;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;

import com.squareup.picasso.Picasso;

import net.tuurlievens.guessthings.database.ThingContract;
import net.tuurlievens.guessthings.database.QueryHandler;

// TODO: saving or adding thing in twopane mode adds whole list

public class ThingDetailFragment extends Fragment implements QueryHandler.AsyncQueryListener{

    public static final String ARG_ITEM_ID = "thingID";
    public int id = 999999999;
    private boolean dualpane = true;

    private EditText nameView;
    private EditText descrView;
    private EditText tagsView;
    private EditText imageUrlView;
    private ImageView imageView;

    public ThingDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey("seperateactivity"))
            this.dualpane = false;

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            this.id = getArguments().getInt(ARG_ITEM_ID);
            Uri uri = ThingContract.Thing.buildRowUri(getArguments().getInt(ARG_ITEM_ID));
            QueryHandler handler = new QueryHandler(getContext(), this);
            handler.startQuery(QueryHandler.OperationToken.TOKEN_QUERY, null, uri, null, null, null, null);
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {

        if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
            // set name
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null)
                appBarLayout.setTitle(cursor.getString(cursor.getColumnIndex(ThingContract.Thing.Columns.NAME)));

            // other fields
            this.nameView.setText(cursor.getString(cursor.getColumnIndex(ThingContract.Thing.Columns.NAME)));
            this.descrView.setText(cursor.getString(cursor.getColumnIndex(ThingContract.Thing.Columns.DESCRIPTION)));
            this.tagsView.setText(cursor.getString(cursor.getColumnIndex(ThingContract.Thing.Columns.TAGS)));

            String imageurl = cursor.getString(cursor.getColumnIndex(ThingContract.Thing.Columns.IMAGEURL));
            this.imageUrlView.setText(imageurl);

            if (!imageurl.isEmpty())
                Picasso.with(getContext())
                    .load(imageurl)
                    .placeholder(R.drawable.ic_file_download_accent_24dp)
                    .error(R.drawable.ic_error_red_24dp)
                    .into(this.imageView);
            else if (this.id != 999999999)
                ((ViewGroup) this.imageView.getParent()).removeView(this.imageView);

            cursor.close();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.thing_detail, container, false);
        this.nameView = rootView.findViewById(R.id.thing_name);
        this.descrView = rootView.findViewById(R.id.thing_descr);
        this.tagsView = rootView.findViewById(R.id.thing_tags);
        this.imageUrlView = rootView.findViewById(R.id.thing_imageurl);
        this.imageView = rootView.findViewById(R.id.thing_image);

        if (this.id != 999999999) {
            Button deletebutton = new Button(getContext());
            deletebutton.setBackgroundColor(Color.RED);
            deletebutton.setTextColor(Color.WHITE);
            deletebutton.setText(R.string.delete);
            deletebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int i)
                        {
                            String selection = ThingContract.Thing.Columns._ID + " = ?";
                            QueryHandler queryHandler = new QueryHandler(getContext(), null);
                            queryHandler.startDelete(QueryHandler.OperationToken.TOKEN_DELETE, null,
                                    ThingContract.Thing.CONTENT_URI, selection, new String[]{String.valueOf(id)});
                            if (!dualpane)
                                getActivity().finish();
                            else
                                getActivity().getSupportFragmentManager().beginTransaction().remove(ThingDetailFragment.this).commit();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(true);
                    dialog.setMessage(getResources().getString(R.string.remove));
                    dialog.show();
                }
            });

            ((ViewGroup) rootView.findViewById(R.id.buttons)).addView(deletebutton,0);
        } else {
            ((ViewGroup) this.imageView.getParent()).removeView(this.imageView);
        }

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QueryHandler queryHandler = new QueryHandler(getContext(), null);
                String name = nameView.getText().toString();
                String descr = descrView.getText().toString();
                String tags = tagsView.getText().toString();
                String imageurl = imageUrlView.getText().toString();

                ContentValues values = new ContentValues();
                values.put(ThingContract.Thing.Columns.NAME, name);
                values.put(ThingContract.Thing.Columns.DESCRIPTION, descr);
                values.put(ThingContract.Thing.Columns.TAGS, tags);
                values.put(ThingContract.Thing.Columns.IMAGEURL, imageurl);
                if (id != 999999999) {
                    String selection = ThingContract.Thing.Columns._ID + " = ?";
                    String[] selectionArg = {String.valueOf(id)};
                    queryHandler.startUpdate(QueryHandler.OperationToken.TOKEN_UPDATE, null, ThingContract
                        .Thing.CONTENT_URI, values, selection, selectionArg);
                } else {
                    queryHandler.startInsert(QueryHandler.OperationToken.TOKEN_INSERT, null, ThingContract
                        .Thing.CONTENT_URI, values);
                }
                if (!dualpane)
                    getActivity().finish();
                else if (id == 999999999)
                    getActivity().getSupportFragmentManager().beginTransaction().remove(ThingDetailFragment.this).commit();
            }
        });

        return rootView;
    }

}
