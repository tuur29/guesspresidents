package net.tuurlievens.guessthings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ThingListAdapter extends RecyclerView.Adapter<ThingListAdapter.ViewHolder> {

    private final ThingListActivity parentActivity;
    private final List<Thing> things;
    private final boolean twoPane;
    private Snackbar undoRemoveSnackBar;

    ThingListAdapter(ThingListActivity parent, List<Thing> items, boolean twoPane) {
        this.things = items;
        this.parentActivity = parent;
        this.twoPane = twoPane;
    }

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

    public void add(Thing item) {
        this.things.add(item);
        this.notifyItemInserted(this.things.size()-1);
    }

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