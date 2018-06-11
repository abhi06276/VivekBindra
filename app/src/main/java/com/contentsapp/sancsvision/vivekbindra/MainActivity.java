package com.contentsapp.sancsvision.vivekbindra;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Connectivity.ConnectivityReceiverListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    DrawerLayout mDrawerLayout = null;
    ListView listView = null;
    ProgressBar progressBar = null;
    SwipeRefreshLayout pullToRefresh = null;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainApplication.configureDefaultImageLoader(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#00ffffff'>Vivek Bindra </font>"));
        listView = findViewById(R.id.list_view);
        progressBar = findViewById(R.id.progressBar);
        pullToRefresh = findViewById(R.id.pullToRefresh);



        this.callAPIToGetContentList();
        this.showAds();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorAccent);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {


            @Override
            public void onRefresh() {
                callAPIToGetContentList();
            }
        });


        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        if(menuItem.toString().equals("Contact Us")){
                            launchEmailApp();
                        }

                        else if(menuItem.toString().equals("Rate the App")){
                            reviewApp();
                        }

                        return true;
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        Connectivity connectivityReceiver = new Connectivity();
        registerReceiver(connectivityReceiver, intentFilter);

        /*register connection status listener*/
        if(MainApplication.getInstance() != null) {
            MainApplication.getInstance().setConnectivityListener(this);
        }

        if (mAdView != null) {
            mAdView.resume();
        }
    }


    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    void launchEmailApp(){
        String[] recipients={"sancsvision1@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Please send us your query.");
//        emailIntent.putExtra(Intent.EXTRA_TEXT,"Body of the content here...");
        emailIntent.setType("message/rfc822");

        emailIntent.setType("text/plain");
        startActivity(emailIntent);
    }

    void reviewApp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //----------------------------------------------------API Calls --------------------------------------------------------------

    void callAPIToGetContentList(){

        if(progressBar != null){
            progressBar.bringToFront();
            progressBar.setVisibility(View.VISIBLE);
            pullToRefresh.setRefreshing(true);

        }
        Log.d(TAG,"API to call is "+Constants.BASE_URL+Constants.GET_CONTENTS_LIST);
        AndroidNetworking.get(Constants.BASE_URL+Constants.GET_CONTENTS_LIST).
                setPriority(Priority.HIGH).
                build().getAsJSONArray(new JSONArrayRequestListener() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG,"Response is "+response);
                initListContents(response);
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                    pullToRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onError(ANError anError) {
                Log.d(TAG,"Error is "+anError);
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                }

            }
        });
    }

    void initListContents(JSONArray response){
        final ArrayList<String> listdata = new ArrayList<String>();
        JSONArray jArray = (JSONArray)response;
        if (jArray != null) {
            for (int i=jArray.length()-1;i>0;i--){
                try {
                    listdata.add(jArray.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        ListViewAdapter adapter = new ListViewAdapter(listdata,getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,ContentDetailsActivity.class);
                intent.putExtra("selectedData",listdata.get(i));
                startActivity(intent);
            }
        });

    }

    //-------------------------------- Show Ads ------------------------------------------------

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

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = Connectivity.isConnected();
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        if(!isConnected) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

    }
}
