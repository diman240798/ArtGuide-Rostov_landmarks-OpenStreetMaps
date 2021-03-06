package com.swg_games_lab.nanicki.artguide.activity.attraction_info;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.swg_games_lab.nanicki.artguide.R;
import com.swg_games_lab.nanicki.artguide.adapters.WikiAdapter;
import com.swg_games_lab.nanicki.artguide.csv.CSVreader;
import com.swg_games_lab.nanicki.artguide.enums.AttractionType;
import com.swg_games_lab.nanicki.artguide.model.Place;
import com.swg_games_lab.nanicki.artguide.ui.BottomNavigationBehavior;

import java.util.List;

public class WikiActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private Button bt_museum, bt_theatre, bt_memorial, bt_stadium, bt_park;
    private WikiAdapter wikiAdapter;
    private List<Place> places;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki);

        places = getPlaces();

        initSortingButtons();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.wiki_bottom_navig_with_buttons);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupAdapter();
        // TODO: ENABLE IN THE END
        //mRecyclerView.setHasFixedSize(true);
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
    }

    private void initSortingButtons() {
        bt_museum = (Button) findViewById(R.id.wiki_bt_museum);
        bt_theatre = (Button) findViewById(R.id.wiki_bt_theatre);
        bt_memorial = (Button) findViewById(R.id.wiki_bt_memorial);
        bt_stadium = (Button) findViewById(R.id.wiki_bt_stadium);
        bt_park = (Button) findViewById(R.id.wiki_bt_park);

        bt_museum.setOnClickListener(this);
        bt_theatre.setOnClickListener(this);
        bt_memorial.setOnClickListener(this);
        bt_stadium.setOnClickListener(this);
        bt_park.setOnClickListener(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }

    private void setupAdapter() {

        wikiAdapter = new WikiAdapter(places);
        mRecyclerView.setAdapter(wikiAdapter);
    }

    private List<Place> getPlaces() {
        return CSVreader.getData(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        setDefaultImages();

        if (id == R.id.wiki_bt_museum) {
            bt_museum.setBackgroundResource(R.drawable.item_museum_chosen);
            wikiAdapter.sortList(places, AttractionType.Museum);
        } else if (id == R.id.wiki_bt_stadium) {
            bt_stadium.setBackgroundResource(R.drawable.item_stadium_chosen);
            wikiAdapter.sortList(places, AttractionType.Stadium);
        } else if (id == R.id.wiki_bt_memorial) {
            bt_memorial.setBackgroundResource(R.drawable.item_memorial_chosen);
            wikiAdapter.sortList(places, AttractionType.Memorial);
        } else if (id == R.id.wiki_bt_theatre) {
            bt_theatre.setBackgroundResource(R.drawable.item_theatre_chosen);
            wikiAdapter.sortList(places, AttractionType.Theatre);
        } else if (id == R.id.wiki_bt_park) {
            bt_park.setBackgroundResource(R.drawable.item_park_chosen);
            wikiAdapter.sortList(places, AttractionType.Park);
        }
    }

    private void setDefaultImages() {
        bt_theatre.setBackgroundResource(R.drawable.item_theatre);
        bt_museum.setBackgroundResource(R.drawable.item_museum);
        bt_memorial.setBackgroundResource(R.drawable.item_memorial);
        bt_stadium.setBackgroundResource(R.drawable.item_stadium);
        bt_park.setBackgroundResource(R.drawable.item_park);
    }

}
