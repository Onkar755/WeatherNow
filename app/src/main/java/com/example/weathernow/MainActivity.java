package com.example.weathernow;

import static android.content.ContentValues.TAG;
import static com.example.weathernow.utilities.WeatherUtils.formatTemperature;
import static com.example.weathernow.utilities.WeatherUtils.toTitleCase;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.weathernow.daily_data.DailyWeatherEntry;
import com.example.weathernow.hourly_data.HourlyWeatherData;
import com.example.weathernow.utilities.JsonUtils;
import com.example.weathernow.utilities.NetworkUtils;
import com.example.weathernow.utilities.NotificationService;

public class MainActivity extends AppCompatActivity {
    ProgressBar mProgressBar1;
    ProgressBar mProgressBar2;
    String mWeatherJsonHourly = null;
    String mWeatherJsonDaily = null;
    private HourlyWeatherAdapter mHourlyWeatherAdapter;
    private DailyWeatherAdapter mDailyWeatherAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static List<HourlyWeatherData> mHourlyWeatherData;
    private static List<DailyWeatherEntry> mDailyWeatherData;

    private TextView mCurrentTemp;
    private TextView mCurrentLow;
    private TextView mCurrentHigh;
    private TextView mLocation;
    private TextView mDescription;

    private ConstraintLayout mWeatherData;

    private TextView mNoInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mProgressBar1 = findViewById(R.id.hourly_weather_progress);
        mProgressBar2 = findViewById(R.id.daily_weather_progress);
        RecyclerView mHourlyRecycleView = findViewById(R.id.recyclerView);
        RecyclerView mDailyRecycleView = findViewById(R.id.daily_weather_data_recycleview);

        mCurrentTemp =  findViewById(R.id.temperature);
        mCurrentHigh =  findViewById(R.id.high);
        mCurrentLow =  findViewById(R.id.low);
        mLocation =  findViewById(R.id.location);
        mDescription =  findViewById(R.id.description);

        mNoInternet =  findViewById(R.id.no_internet);
        mWeatherData = findViewById(R.id.weather_data);

        LinearLayoutManager hourlyData_LayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mHourlyRecycleView.setLayoutManager(hourlyData_LayoutManager);
        mHourlyRecycleView.setHasFixedSize(true);

        LinearLayoutManager dailyData_LayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDailyRecycleView.setLayoutManager(dailyData_LayoutManager);
        mDailyRecycleView.setHasFixedSize(true);

        mHourlyWeatherAdapter = new HourlyWeatherAdapter(this);
        mHourlyRecycleView.setAdapter(mHourlyWeatherAdapter);

        mDailyWeatherAdapter = new DailyWeatherAdapter(this);
        mDailyRecycleView.setAdapter(mDailyWeatherAdapter);
        run();

        // Check if the NotificationService is running
        if (!isServiceRunning()) {
            startService(new Intent(this, NotificationService.class));
        }
    }

    // Method to check if a service is running
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
                if (NotificationService.class.getName().equals(serviceInfo.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void run() {
        if (isConnectedToInternet()) {
            mWeatherData.setVisibility(View.VISIBLE);
            mNoInternet.setVisibility(View.INVISIBLE);
            fetchHourlyData();
            fetchDailyData();
        } else {
            mWeatherData.setVisibility(View.INVISIBLE);
            mNoInternet.setVisibility(View.VISIBLE);
        }
    }

    private void fetchHourlyData() {
        executorService.execute(() -> {
            try {
                URL githubUrl = NetworkUtils.buildUrl_HourlyData(MainActivity.this);
                if (githubUrl != null) {
                    mWeatherJsonHourly = NetworkUtils.getResponseFromHttpUrl(githubUrl);
                }
            } catch (IOException e) {
                Log.e(TAG, "An error occurred", e);
                return;
            }
            runOnUiThread(() -> {
                try {
                    mProgressBar1.setVisibility(View.INVISIBLE);
                    mHourlyWeatherData = JsonUtils.parseHourlyWeatherData(mWeatherJsonHourly);
                    mHourlyWeatherAdapter.setWeatherDataList(mHourlyWeatherData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private void fetchDailyData() {
        executorService.execute(() -> {
            try {
                URL githubUrl = NetworkUtils.buildURL_DailyData(MainActivity.this);
                if (githubUrl != null) {
                    mWeatherJsonDaily = NetworkUtils.getResponseFromHttpUrl(githubUrl);
                }
            } catch (IOException e) {
                Log.e(TAG, "An error occurred", e);
                return;
            }
            runOnUiThread(() -> {
                try {
                    mProgressBar2.setVisibility(View.INVISIBLE);
                    mDailyWeatherData = JsonUtils.parseDailyWeatherData(mWeatherJsonDaily);
                    mDailyWeatherAdapter.setWeatherDataList(mDailyWeatherData);
                    updateCurrentWeather();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }

    private void updateCurrentWeather() {
        String xCurrentTemp = formatTemperature(this, mHourlyWeatherData.get(0).getMain().getTemp());
        mCurrentTemp.setText(xCurrentTemp);
        String minTemp = "L:" + formatTemperature(this, mDailyWeatherData.get(0).getTemperature().getMin());
        mCurrentLow.setText(minTemp);
        String maxTemp = "H:" + formatTemperature(this, mDailyWeatherData.get(0).getTemperature().getMax());
        mCurrentHigh.setText(maxTemp);
        mDescription.setText(toTitleCase(mHourlyWeatherData.get(0).getWeather().getDescription()));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPreferences.getString("location", "Nashik, MH");
        mLocation.setText(toTitleCase(location));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}