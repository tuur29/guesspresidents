package net.tuurlievens.guessthings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class ThingListActivity extends AppCompatActivity implements ThingDetailFragment.ThingDetailFragmentListener {

    private boolean twoPane;
    private RecyclerView recyclerView;
    private ThingListAdapter adapter;
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
        this.adapter = new ThingListAdapter(this, savedInstanceState, twoPane);
        this.recyclerView.setAdapter(this.adapter);

        // allow swiping away
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adapter.dismiss(viewHolder, recyclerView);
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
                        adapter.restartLoader(null);
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
    protected void onResume() {
        super.onResume();
        this.adapter.restartLoader(lastinstanceState);
    }

    // detail fragment listener

    @Override
    public void updateThing(Thing thing) {
        if (thing.id == ThingDetailFragment.NEW_ID) {
            this.adapter.insert(thing);
        } else {
            this.adapter.update(thing);
        }
    }

    @Override
    public void deleteThing(int id) {
        this.adapter.delete(id);
    }
}
