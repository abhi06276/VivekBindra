package com.contentsapp.sancsvision.vivekbindra;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    DrawerLayout mDrawerLayout = null;
    ListView listView = null;
    ProgressBar progressBar = null;
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



        this.callAPIToGetContentList();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorAccent);

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
                        return true;
                    }
                });

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
            for (int i=0;i<jArray.length();i++){
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


}
