package net.tuurlievens.guessthings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import net.tuurlievens.guessthings.database.ThingContract;

import java.util.ArrayList;

public class ThingListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private boolean twoPane;
    private RecyclerView recyclerView;
    private ThingListAdapter adapter;
    private static final int LOADER_ID = 1;
    private Bundle lastinstanceState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thing_list);
        this.lastinstanceState = savedInstanceState;

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.thing_detail_container) != null)
            twoPane = true;

        // setup recycler view
        this.recyclerView = findViewById(R.id.thing_list);
        this.adapter = new ThingListAdapter(this, new ArrayList<Thing>(), twoPane);
        this.recyclerView.setAdapter(this.adapter);

        // allow swiping away
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adapter.remove(viewHolder, recyclerView);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = findViewById(R.id.fab);
        // new thing on fab click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (twoPane) {
                    Bundle arguments = new Bundle();
                    ThingDetailFragment fragment = new ThingDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.thing_detail_container, fragment)
                        .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ThingDetailActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        // reset list on long press fab
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ThingListActivity.this);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        adapter.reset();
                        getSupportLoaderManager().restartLoader(LOADER_ID, null, ThingListActivity.this);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.create();
                dialog.setCancelable(true);
                dialog.setMessage(getResources().getString(R.string.refresh));
                dialog.show();

                return false;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putStringArray("possiblethings", this.adapter.getIds());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            ThingContract.Thing.Columns._ID,
            ThingContract.Thing.Columns.NAME,
            ThingContract.Thing.Columns.TAGS,
            ThingContract.Thing.Columns.IMAGEURL
        };

        // TODO: Move to ThingProvider?
        String selection = null;
        String[] selectionArgs = {};
        if (args != null && args.containsKey("possiblethings")) {
            selectionArgs = args.getStringArray("possiblethings");
            selection = "_id IN (";
            for (int i = 0; i < selectionArgs.length; i++) {
                selection += "?";
                if (i < selectionArgs.length-1)
                    selection += ",";
            }
            selection += ")";
        }

        String sortOrder = ThingContract.Thing.Columns._ID + " ASC";
        return new CursorLoader(this, ThingContract.Thing.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.adapter.reset();
        while (data.moveToNext()) {
            this.adapter.add( new Thing(
                data.getInt(0),
                data.getString(1),
                data.getString(2),
                null,
                data.getString(3)
            ) );
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(LOADER_ID, this.lastinstanceState, this);
    }
}
