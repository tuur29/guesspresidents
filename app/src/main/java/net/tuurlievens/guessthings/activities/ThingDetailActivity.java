package net.tuurlievens.guessthings.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;

import net.tuurlievens.guessthings.R;
import net.tuurlievens.guessthings.fragments.ThingDetailFragment;
import net.tuurlievens.guessthings.models.Thing;
import net.tuurlievens.guessthings.persistency.RealQueryHandler;

public class ThingDetailActivity extends AppCompatActivity implements ThingDetailFragment.ThingDetailFragmentListener {

    private RealQueryHandler realQueryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thing_detail);
        this.realQueryHandler = new RealQueryHandler(this);
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

    @Override
    public void updateThing(Thing thing) {
        if (thing.id == ThingDetailFragment.NEW_ID)
            this.realQueryHandler.insert(thing);
        else
            this.realQueryHandler.update(thing);

        Intent result = new Intent();
        result.putExtra("update","all");
        setResult(Activity.RESULT_OK, result);

        finish();
    }

    @Override
    public void deleteThing(int id) {
        this.realQueryHandler.delete(id);

        Intent result = new Intent();
        result.putExtra("delete",id);
        setResult(Activity.RESULT_OK, result);

        finish();
    }
}
