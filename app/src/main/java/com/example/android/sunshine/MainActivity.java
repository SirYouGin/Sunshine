package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.data.WeatherDbHelper;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, new DetailFragment())
                    .commit();

                WeatherDbHelper db = new WeatherDbHelper(this);
                db.onUpgrade(db.getWritableDatabase(),1,2);
            }
        }
        else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);


    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane)
        {
            Bundle args = new Bundle();

            args.putString(DetailFragment.DATE_KEY, date);
            //args.putInt(ForecastFragment.POSITION_KEY, mPosition);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment)
                    .commit();
        } else {

            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.DATE_KEY, date);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);

            return true;
        }
        if (id == R.id.action_map) {

            OpenPreferredLocationInMap();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void OpenPreferredLocationInMap() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
            } else {
            Log.d(LOG_TAG, "Couldn't call "+ location + ", no geo scheme installed");
        }
    }


}
