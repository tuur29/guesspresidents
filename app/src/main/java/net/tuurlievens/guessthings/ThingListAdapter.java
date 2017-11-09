package net.tuurlievens.guessthings;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import net.tuurlievens.guessthings.database.ThingContract;

import java.util.ArrayList;
import java.util.List;

public class ThingListAdapter extends RecyclerView.Adapter<ThingListAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private final ThingListActivity parentActivity;
    private final List<Thing> things = new ArrayList<>();
    private final boolean twoPane;
    private static final int LOADER_ID = 1;
    private Snackbar undoRemoveSnackBar;

    ThingListAdapter(ThingListActivity parent, Bundle savedInstanceState, boolean twoPane) {
        this.parentActivity = parent;
        this.twoPane = twoPane;
        restartLoader(savedInstanceState);
    }

    public void restartLoader(Bundle lastinstanceState) {
        parentActivity.getSupportLoaderManager().restartLoader(LOADER_ID, lastinstanceState, this);
    }

    // get data from database

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
        return new CursorLoader(parentActivity, ThingContract.Thing.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        reset();
        while (data.moveToNext()) {
            add( new Thing(
                    data.getInt(0),
                    data.getString(1),
                    data.getString(2),
                    null,
                    data.getString(3)
            ) );
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public int getItemCount() {
        return this.things.size();
    }

    public String[] getIds() {
        String[] ids = new String[this.things.size()];
        int i = 0;
        for (Thing thing: this.things)
            ids[i++] = String.valueOf(thing.id);

        return ids;
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
        holder.tagsView.setText(things.get(position).tags);

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
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ThingDetailActivity.class);
                    intent.putExtra(ThingDetailFragment.ARG_ITEM_ID, thing.id);
                    context.startActivity(intent);
                }
            }
        });
    }

    // manage data

    public void add(Thing item) {
        this.things.add(item);
        this.notifyItemInserted(this.things.size()-1);
    }

    public void remove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
        final int position = viewHolder.getAdapterPosition();
        final Thing oldThing = this.things.get(position);

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
        this.undoRemoveSnackBar.show();

        this.things.remove(position);
        this.notifyItemRemoved(position);
    }

    public void reset() {
        this.things.clear();
        this.notifyDataSetChanged();
        if (this.undoRemoveSnackBar != null)
            this.undoRemoveSnackBar.dismiss();
    }

    // viewholder

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView tagsView;
        final ImageView imageView;

        ViewHolder(View view) {
            super(view);
            this.nameView = view.findViewById(R.id.name);
            this.tagsView = view.findViewById(R.id.tags);
            this.imageView = view.findViewById(R.id.image);
        }
    }
}