package net.tuurlievens.guesspresidents;

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

public class PresidentAdapter extends RecyclerView.Adapter<PresidentAdapter.ViewHolder> {

    private final PresidentListActivity parentActivity;
    private final List<PresidentContent.President> presidents;
    private final boolean twoPane;
    private Snackbar undoRemoveSnackBar;

    PresidentAdapter(PresidentListActivity parent, List<PresidentContent.President> items, boolean twoPane) {
        this.presidents = items;
        this.parentActivity = parent;
        this.twoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.president_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.nameView.setText(presidents.get(position).name);

        Picasso.with(holder.itemView.getContext())
                .load(presidents.get(position).imageurl)
                .placeholder(R.drawable.ic_file_download_accent_24dp)
                .error(R.drawable.ic_error_red_24dp)
                .into(holder.imageView);

        holder.itemView.setTag(presidents.get(position));

        // load second panel or show new activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PresidentContent.President president = (PresidentContent.President) view.getTag();
                if (twoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(PresidentDetailFragment.ARG_ITEM_ID, president.id);
                    PresidentDetailFragment fragment = new PresidentDetailFragment();
                    fragment.setArguments(arguments);
                    parentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.president_detail_container, fragment)
                        .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PresidentDetailActivity.class);
                    intent.putExtra(PresidentDetailFragment.ARG_ITEM_ID, president.id);
                    context.startActivity(intent);
                }
            }
        });
    }

    public void remove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
        final int position = viewHolder.getAdapterPosition();
        final PresidentContent.President oldPresident = this.presidents.get(position);

        this.undoRemoveSnackBar = Snackbar
            .make(recyclerView.getRootView(), "Removed", Snackbar.LENGTH_LONG)
            .setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presidents.add(position, oldPresident);
                    notifyItemInserted(position);
                    recyclerView.scrollToPosition(position);
                }
            });
        this.undoRemoveSnackBar.show();

        this.presidents.remove(position);
        this.notifyItemRemoved(position);
    }

    public void reset(List<PresidentContent.President> list) {
        this.presidents.clear();
        for (PresidentContent.President president : list)
            this.presidents.add(president);
        this.notifyDataSetChanged();
        if (this.undoRemoveSnackBar != null)
            this.undoRemoveSnackBar.dismiss();
    }

    @Override
    public int getItemCount() {
        return presidents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final ImageView imageView;

        ViewHolder(View view) {
            super(view);
            this.nameView = view.findViewById(R.id.name);
            this.imageView = view.findViewById(R.id.photo);
        }
    }
}