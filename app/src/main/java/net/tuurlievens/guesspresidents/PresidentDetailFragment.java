package net.tuurlievens.guesspresidents;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PresidentDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "presidentID";
    private PresidentContent.President president;

    public PresidentDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            president = PresidentContent.getPresidentData().get(getArguments().getInt(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(president.name);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.president_detail, container, false);
        if (president != null) {
            ((TextView) rootView.findViewById(R.id.president_descr)).setText(president.descr);
            ((TextView) rootView.findViewById(R.id.president_lifespan)).setText(String.valueOf(president.birthyear) + " - " + String.valueOf(president.deathyear));

            ImageView img = rootView.findViewById(R.id.president_photo);
            Picasso.with(getContext())
                .load(president.imageurl)
                .placeholder(R.drawable.ic_file_download_accent_24dp)
                .error(R.drawable.ic_error_red_24dp)
                .into(img);
        }
        return rootView;
    }
}
