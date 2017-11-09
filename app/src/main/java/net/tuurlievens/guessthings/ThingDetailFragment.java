package net.tuurlievens.guessthings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;

import com.squareup.picasso.Picasso;

import net.tuurlievens.guessthings.database.ThingContract;
import net.tuurlievens.guessthings.database.QueryHandler;

// TODO: Change image url field with native image picker

public class ThingDetailFragment extends Fragment implements QueryHandler.AsyncQueryListener{

    public static final String ARG_ITEM_ID = "thingID";
    public static final int NEW_ID = 999999999;

    private int id = NEW_ID;
    private boolean dualpane = true;
    private ThingDetailFragmentListener listener;

    private EditText nameView;
    private EditText descrView;
    private EditText tagsView;
    private EditText imageUrlView;
    private ImageView imageView;

    public ThingDetailFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ThingDetailFragmentListener)
            listener = (ThingDetailFragmentListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement ThingDetailFragmentListener");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey("seperateactivity"))
            this.dualpane = false;

        // get data

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            this.id = getArguments().getInt(ARG_ITEM_ID);
            Uri uri = ThingContract.Thing.buildRowUri(getArguments().getInt(ARG_ITEM_ID));
            QueryHandler handler = new QueryHandler(getContext(), this);
            handler.startQuery(QueryHandler.OperationToken.TOKEN_QUERY, null, uri, null, null, null, null);
        }
    }

    // make view

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.thing_detail, container, false);
        this.nameView = rootView.findViewById(R.id.thing_name);
        this.descrView = rootView.findViewById(R.id.thing_descr);
        this.tagsView = rootView.findViewById(R.id.thing_tags);
        this.imageUrlView = rootView.findViewById(R.id.thing_imageurl);
        this.imageView = rootView.findViewById(R.id.thing_image);

        if (this.id != NEW_ID) {
            Button deletebutton = rootView.findViewById(R.id.delete_button);
            deletebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int i)
                        {
                            listener.deleteThing(id);
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

            deletebutton.setVisibility(View.VISIBLE);
        } else {
            ((ViewGroup) this.imageView.getParent()).removeView(this.imageView);
        }

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameView.getText().toString();
                String tags = tagsView.getText().toString();
                String descr = descrView.getText().toString();
                String imageurl = imageUrlView.getText().toString();

                // form validation
                boolean errors = false;
                if (name.isEmpty()) {
                    ((TextInputLayout) rootView.findViewById(R.id.thing_name_layout)).setError(getString(R.string.nameempty));
                    errors = true;
                }
                if ( !imageurl.isEmpty() && !URLUtil.isValidUrl(imageurl) ) {
                    ((TextInputLayout) rootView.findViewById(R.id.thing_imageurl_layout)).setError(getString(R.string.invalidurl));
                    errors = true;
                }
                if (errors) return;

                // save
                Thing thing = new Thing(id, name, tags, descr, imageurl);
                listener.updateThing(thing);

                // close panel
                if (!dualpane)
                    getActivity().finish();
                else if (id == NEW_ID)
                    getActivity().getSupportFragmentManager().beginTransaction().remove(ThingDetailFragment.this).commit();
            }
        });

        return rootView;
    }

    // fill view with data

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
            else if (this.id != NEW_ID)
                ((ViewGroup) this.imageView.getParent()).removeView(this.imageView);

            cursor.close();
        }
    }

    public interface ThingDetailFragmentListener {
        void updateThing(Thing thing);
        void deleteThing(int id);
    }

}
