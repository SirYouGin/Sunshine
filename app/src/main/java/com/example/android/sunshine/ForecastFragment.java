package com.example.android.sunshine;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;
import com.example.android.sunshine.service.SunshineService;

import java.util.Date;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ForecastAdapter mForecastAdapter;

    private ListView mListView;

    private static final int FORECAST_LOADER = 0;

    public static final String SELECTED_KEY = "forecast_position";

    private int mPosition;

    private  boolean mUseTodayLayout;

    private String mLocation;



    // For the forecast view we're showing only a small subset of the stored data.
// Specify the columns we need.
    public static final String[] FORECAST_COLUMNS = {
// In this case the id needs to be fully qualified with a table name, since
// the content provider joins the location & weather tables in the background
// (both have an _id column)
// On the one hand, that's annoying. On the other, you can search the weather table
// using the location set by the user, which is only in the Location table.
// So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            //WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these
// must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_CONDITION_ID = 5;
    public static final int COL_LOCATION_SETTING = 6;

    /* A callback interface that all activities containing this fragment must
        * implement. This mechanism allows activities to be notified of item
        * selections.
        */
        public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
            public void onItemSelected(String date);
        }

        public ForecastFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.forecastfragment, menu);
        }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_refresh){
                updateWeather();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void setUseTodayLayout(boolean useTodayLayout)
        {
            mUseTodayLayout = useTodayLayout;
            if (mForecastAdapter != null){
                mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
            }
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mListView = (ListView)rootView.findViewById(R.id.listview_forecast);


            mListView.setAdapter(mForecastAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Cursor cursor = mForecastAdapter.getCursor();
                    if (cursor != null && cursor.moveToPosition(position)) {
                        /*
                        Intent intent = new Intent(getActivity(), DetailActivity.class)
                                //.putExtra(Intent.EXTRA_TEXT, cursor.getString(COL_WEATHER_DATE));
                                .putExtra(DetailFragment.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
                        startActivity(intent);
                        */
                        ((Callback)getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                    }
                    mPosition = position;
                }

            });

            if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
            {
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }

            return rootView;
        }

        private void updateWeather(){

            Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
            alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));

//Wrap in a pending intent which only fires once.
            PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);

            AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

//Set the AlarmManager to wake up the system.
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
            /*
            Intent intent = new Intent(getActivity(), SunshineService.class);
            intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                    Utility.getPreferredLocation(getActivity()));
            getActivity().startService(intent);

            FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
            String location = Utility.getPreferredLocation(getActivity());
            weatherTask.execute(location);
            */
        }

        @Override
        public void onStart() {
            super.onStart();

        }

        @Override
        public void onResume() {
            super.onResume();
            if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            }
        }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created. This
// fragment only uses one loader, so we don't care about checking the id.

// To only show current and future dates, get the String representation for today,
// and filter the query to return weather only for dates after or including today.
// Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

// Sort order: Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

// Now create and return a CursorLoader that will take care of
// creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mForecastAdapter.swapCursor(data);

        if (mPosition != ListView.INVALID_POSITION){
            mListView.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }
}
