package com.swg_games_lab.nanicki.artguide.activity.attraction_info;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.swg_games_lab.nanicki.artguide.R;
import com.swg_games_lab.nanicki.artguide.activity.MapActivity;
import com.swg_games_lab.nanicki.artguide.csv.CSVreader;
import com.swg_games_lab.nanicki.artguide.model.Place;
import com.swg_games_lab.nanicki.artguide.util.PermissionUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class wikiAttractionActivity extends AppCompatActivity {

    private static final String TAG = "wikiAttractionActivity";
    ImageView imageView;
    TextView titleTW, descrTW;
    Button listenBTN, bottomBTN, showOnMap;
    private GifImageView guideSpeaker;
    private GifDrawable gifFromResource;
    private Drawable playImage;
    private Drawable pauseImage;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_wiki__attraction);

        bottomBTN = (Button) findViewById(R.id.wiki_attr_bottomBT);
        bottomBTN.setOnClickListener(v -> finish());
        imageView = (ImageView) findViewById(R.id.wiki_attr_Image);
        titleTW = (TextView) findViewById(R.id.wiki_attr_titleTW);
        descrTW = (TextView) findViewById(R.id.wiki_attr_descriptionTW);
        listenBTN = (Button) findViewById(R.id.wiki_attr_listenBTN);
        showOnMap = (Button) findViewById(R.id.wiki_attr_show_on_mapBT);

        playImage = getResources().getDrawable(R.drawable.listen_play);
        pauseImage = getResources().getDrawable(R.drawable.listen_pause);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ru"));
                Set<Voice> voices = textToSpeech.getVoices();
                for (Voice voice : voices) {
                    if (voice.getName().equals("ru-ru-x-dfc#male_1-local"))
                        textToSpeech.setVoice(voice);
                }
            }
        });

        ///////////
        guideSpeaker = (GifImageView) findViewById(R.id.wiki_attr_speaking_heroIV);
        try {
            gifFromResource = new GifDrawable(getResources(), R.drawable.gif_speaking_hero);
            gifFromResource.stop();
            guideSpeaker.setImageDrawable(gifFromResource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        listenBTN.setOnClickListener(v -> {
            boolean running = gifFromResource.isRunning();
            if (running) {
                gifFromResource.pause();
                listenBTN.setCompoundDrawablesWithIntrinsicBounds(playImage, null, null, null);
                textToSpeech.stop();
            } else {
                gifFromResource.start();
                listenBTN.setCompoundDrawablesWithIntrinsicBounds(pauseImage, null, null, null);
                textToSpeech.speak(descrTW.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }
        });

        if (bundle == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                int placeId = extras.getInt("TAG");
                Place place = CSVreader.getPlaceById(placeId);
                // get data
                int placeImageBig = place.getImageBig();
                String title = place.getTitle();
                String description = place.getDescription();
                // set data
                imageView.setImageResource(placeImageBig);
                titleTW.setText(title);
                descrTW.setText(description);
                showOnMap.setOnClickListener(v -> {
                    if (PermissionUtil.hasMapRequiredPermissions(this)) {
                        Log.d(TAG, place.getTitle());
                        Intent intent = new Intent(this, MapActivity.class);
                        intent.putExtra("TAG", place.getId());
                        startActivity(intent);
                    } else
                        PermissionUtil.requestMapRequiredPermissions(this);

                });
            } else
                throw new RuntimeException("No Bundle Here (:");
        }

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onStop() {
        super.onStop();
        gifFromResource.stop();
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

    private void setInfo(int mDrawable, String title, String description) {
        imageView.setImageResource(mDrawable);
        titleTW.setText(title);
        descrTW.setText(description);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
