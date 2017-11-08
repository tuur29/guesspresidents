package net.tuurlievens.guessthings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;

public class ThingDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thing_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putBoolean("seperateactivity",true);
            if (getIntent().hasExtra(ThingDetailFragment.ARG_ITEM_ID))
                arguments.putInt(ThingDetailFragment.ARG_ITEM_ID, getIntent().getIntExtra(ThingDetailFragment.ARG_ITEM_ID, 0));
            ThingDetailFragment fragment = new ThingDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.thing_detail_container, fragment)
                .commit();
        }
    }

}
