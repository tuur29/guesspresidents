package net.tuurlievens.guessthings;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.tuurlievens.guessthings.database.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThingListAdapter extends RecyclerView.Adapter<ThingListAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private RealQueryHandler realQueryHandler;
    private static final int LOADER_ID = 1;
    private final FragmentActivity parentActivity;

    private List<String> dismissedthings = new ArrayList<>();
    private final List<Thing> things = new ArrayList<>();
    private final boolean twoPane;
    private boolean loadAlreadyFinished = false;

    private Snackbar undoRemoveSnackBar;

    ThingListAdapter(FragmentActivity parent, Bundle savedInstanceState, boolean twoPane) {
        this.parentActivity = parent;
        this.twoPane = twoPane;
        this.realQueryHandler = new RealQueryHandler(parent);

        restartLoader(savedInstanceState);
    }

    public void restartLoader(Bundle lastinstanceState) {
        this.loadAlreadyFinished = false;
        this.things.clear();
        notifyDataSetChanged();
        parentActivity.getSupportLoaderManager().restartLoader(LOADER_ID, lastinstanceState, this);
    }

    // get data from database

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            ThingContract.Thing.Columns._ID,
            ThingContract.Thing.Columns.NAME,
            ThingContract.Thing.Columns.TAGS,
            ThingContract.Thing.Columns.DESCRIPTION,
            ThingContract.Thing.Columns.IMAGEURL
        };

        // filter out dismissed
        String selection = null;
        String[] selectionArgs = {};
        if (args != null && args.containsKey("dismissedthings")) {
            selectionArgs = args.getStringArray("dismissedthings");
            this.dismissedthings = new ArrayList<>(Arrays.asList(selectionArgs));
            selection = "_id NOT IN (";
            for (int i = 0; i < selectionArgs.length; i++) {
                selection += "?";
                if (i < selectionArgs.length-1)
                    selection += ",";
            }
            selection += ")";
        }

        String sortOrder = ThingContract.Thing.Columns._ID + " ASC";
        return new CursorLoader(parentActivity, ThingContract.Thing.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loadAlreadyFinished) return;
        loadAlreadyFinished = true;
        while (data.moveToNext()) {
            add( new Thing(
                    data.getInt(0),
                    data.getString(1),
                    data.getString(2),
                    data.getString(3),
                    data.getString(4)
            ) );
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public int getItemCount() {
        return this.things.size();
    }

    public int getPositionById(int id) {
        int i = 0;
        for (Thing thing : this.things) {
            if (thing.id == id) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public String[] getDismissedIds() {
        return dismissedthings.toArray(new String[dismissedthings.size()]);
    }

    // make views

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thing_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.nameView.setText(things.get(position).name);
        holder.descrView.setText(things.get(position).descr);

        if (!things.get(position).imageurl.isEmpty())
            Picasso.with(holder.itemView.getContext())
                .load(things.get(position).imageurl)
                .placeholder(R.drawable.ic_file_download_accent_24dp)
                .error(R.drawable.ic_error_red_24dp)
                .into(holder.imageView);

        holder.itemView.setTag(things.get(position));

        // load second panel or show new activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thing thing = (Thing) view.getTag();
                if (twoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ThingDetailFragment.ARG_ITEM_ID, thing.id);
                    ThingDetailFragment fragment = new ThingDetailFragment();
                    fragment.setArguments(arguments);
                    parentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.thing_detail_container, fragment)
                        .commit();
                } else {
                    Intent intent = new Intent(parentActivity, ThingDetailActivity.class);
                    intent.putExtra(ThingDetailFragment.ARG_ITEM_ID, thing.id);
                    parentActivity.startActivityForResult(intent, 1);
                }
            }
        });
    }

    // manage recyclerview

    public void add(Thing thing) {
        this.things.add(thing);
        this.notifyItemInserted(this.things.size()-1);
    }

    public void remove(int position) {
        this.things.remove(position);
        this.notifyItemRemoved(position);
    }

    public void dismiss(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
        final int position = viewHolder.getAdapterPosition();
        final Thing oldThing = this.things.get(position);
        this.dismissedthings.add(String.valueOf(oldThing.id));
        this.undoRemoveSnackBar = Snackbar
            .make(recyclerView.getRootView(), "Removed", Snackbar.LENGTH_LONG)
            .setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    things.add(position, oldThing);
                    notifyItemInserted(position);
                    recyclerView.scrollToPosition(position);
                }
            });
        remove(position);
        this.undoRemoveSnackBar.show();
    }

    public void reset() {
        this.things.clear();
        this.notifyDataSetChanged();
        if (this.undoRemoveSnackBar != null)
            this.undoRemoveSnackBar.dismiss();
        restartLoader(null);
    }

    public void insert(Thing thing) {
        this.realQueryHandler.insert(thing);
        // TODO: set thing to have new id
        add(thing);
    }

    public void update(Thing thing) {
        this.realQueryHandler.update(thing);
        // update recycler
        int pos = getPositionById(thing.id);
        this.things.set(pos, thing);
        notifyItemChanged(pos);
    }

    public void delete(int id) {
        this.realQueryHandler.delete(id);
        remove(getPositionById(id));
    }

    // viewholder

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView descrView;
        final ImageView imageView;

        ViewHolder(View view) {
            super(view);
            this.nameView = view.findViewById(R.id.name);
            this.descrView = view.findViewById(R.id.descr);
            this.imageView = view.findViewById(R.id.image);
        }
    }
}