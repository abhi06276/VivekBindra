package com.contentsapp.sancsvision.vivekbindra;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.contentsapp.sancsvision.vivekbindra.utils.FullScreenManager;
import com.contentsapp.sancsvision.vivekbindra.utils.YouTubeDataEndpoint;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pierfrancescosoffritti.youtubeplayer.player.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerInitListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.youtubeplayer.ui.PlayerUIController;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ContentDetailsActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    JSONObject currentObj  = null;
    String videoId = null;
    YouTubePlayerView youTubePlayerView = null;
    private FullScreenManager fullScreenManager = new FullScreenManager(this);
    TextView publishedOn = null;
    TextView title = null;
    TextView description = null;
    ImageButton shareButton = null;
    private AdView mAdView;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_details);

        publishedOn= findViewById(R.id.textView_published_on);
        title= findViewById(R.id.textView_title);
        description= findViewById(R.id.textView_video_details);
        shareButton = findViewById(R.id.imageButton);
        description.setMovementMethod(new ScrollingMovementMethod());


        String data = getIntent().getExtras().getString("selectedData");
        try {
            currentObj = new JSONObject(data);
            videoId = currentObj.getString("video_id");
            initData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        initYouTubePlayerView();
        this.showAds();


        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorAccent));
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);




        youTubePlayerView.initialize(new YouTubePlayerInitListener() {
            @Override
            public void onInitSuccess(final YouTubePlayer initializedYouTubePlayer) {
                initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        initializedYouTubePlayer.loadVideo(videoId, 0);
                    }
                });
            }
        }, true);
    }

    private void initData() throws JSONException {
        String videoUrl = null;
        if(!(currentObj.isNull("published_on"))){
            publishedOn.setText("Published on "+currentObj.getString("published_on"));
        }

        if(!(currentObj.isNull("title"))){
            title.setText(currentObj.getString("title"));
        }

        if(!(currentObj.isNull("description"))){
            description.setText(currentObj.getString("description"));
        }

        if(!(currentObj.isNull("url"))){
            videoUrl=currentObj.getString("url");
        }

        String finalVideoUrl = videoUrl;
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"setOnClickListener");
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Shared from my favourite app.!!");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, ""+ finalVideoUrl);
                startActivity(Intent.createChooser(sharingIntent,"Share using"));
            }
        });
    }
    private void initYouTubePlayerView() {

        // The player will automatically release itself when the activity is destroyed.
        // The player will automatically pause when the activity is paused
        // If you don't add YouTubePlayerView as a lifecycle observer, you will have to release it manually.
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.initialize(youTubePlayer -> {

            youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady() {
                    loadVideo(youTubePlayer, videoId);
                }
            });

            addFullScreenListenerToPlayer(youTubePlayer);

        }, true);
    }

    private void loadVideo(YouTubePlayer youTubePlayer, String videoId) {
        if(getLifecycle().getCurrentState() == Lifecycle.State.RESUMED)
            youTubePlayer.loadVideo(videoId, 0);
        else
            youTubePlayer.cueVideo(videoId, 0);

    }

    private void addFullScreenListenerToPlayer(final YouTubePlayer youTubePlayer) {
        youTubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullScreenManager.enterFullScreen();

                addCustomActionToPlayer(youTubePlayer);
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fullScreenManager.exitFullScreen();

                removeCustomActionFromPlayer();
            }
        });
    }


     /* This method adds a new custom action to the player.
     * Custom actions are shown next to the Play/Pause button in the middle of the player.
            */
    private void addCustomActionToPlayer(YouTubePlayer youTubePlayer) {
        Drawable customActionIcon = ContextCompat.getDrawable(this, R.drawable.ic_pause_36dp);

        youTubePlayerView.getPlayerUIController().setCustomAction1(customActionIcon, view -> {
            if(youTubePlayer != null) youTubePlayer.pause();
        });
    }

    private void removeCustomActionFromPlayer() {
        youTubePlayerView.getPlayerUIController().showCustomAction1(false);
    }

    private void setVideoTitle(PlayerUIController playerUIController, String videoId) {

        Single<String> observable = YouTubeDataEndpoint.getVideoTitleFromYouTubeDataAPIs(videoId);

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        videoTitle -> playerUIController.setVideoTitle(videoTitle),
                        error -> { Log.e(getClass().getSimpleName(), "Can't retrieve video title, are you connected to the internet?"); }
                );
    }


    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        youTubePlayerView.getPlayerUIController().getMenu().dismiss();
    }

    @Override
    public void onBackPressed() {
        if (youTubePlayerView.isFullScreen())
            youTubePlayerView.exitFullScreen();
        else
            super.onBackPressed();
    }


    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        youTubePlayerView.release();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    void showAds(){
        mAdView = findViewById(R.id.adView);
//        mAdView.setAdSize(AdSize.BANNER);
//        mAdView.setAdUnitId(getString(R.string.banner_home_footer));
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                .addTestDevice("652F3E726F4068AD8851F5B9D40E8C7E")
                .build();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdClosed() {
//                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
//                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
//                Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });

        mAdView.loadAd(adRequest);
    }
}
