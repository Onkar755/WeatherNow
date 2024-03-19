package com.example.weathernow.utilities;

import static com.example.weathernow.utilities.WeatherUtils.getNotificationText;
import static com.example.weathernow.utilities.WeatherUtils.isNight;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.weathernow.MainActivity;
import com.example.weathernow.R;
import com.example.weathernow.hourly_data.HourlyWeatherData;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final long INTERVAL = 4 * 60 * 60 * 1000; // 4 hours in milliseconds
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "WeatherChannel";
    private static final String CHANNEL_NAME = "Weather Channel";
    private final Handler handler = new Handler();
    private ExecutorService executorService;
    private NotificationManager notificationManager;
    private HourlyWeatherData mWeatherData;

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            fetchWeatherDataAsync();
            handler.postDelayed(this, INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        notificationManager = getSystemService(NotificationManager.class);
        handler.post(task); // Start periodic task
        if (mWeatherData != null) {
            startForeground(NOTIFICATION_ID, createNotification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(task);
        stopForeground(true);
        executorService.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void fetchWeatherDataAsync() {
        executorService.execute(() -> {
            mWeatherData = fetchWeatherData();
            if (mWeatherData != null) {
                Log.e(TAG, "fetchWeatherDataAsync " + mWeatherData.getWeather().getDescription());
                createNotification();
            } else {
                Log.e(TAG, "Failed to fetch weather data.");
            }
        });
    }

    private HourlyWeatherData fetchWeatherData() {
        String mWeatherJsonHourly = null;
        List<HourlyWeatherData> mHourlyWeatherData = null;

        Log.v(TAG, "Fetching weather data...");
        try {
            URL githubUrl = NetworkUtils.buildUrl_HourlyData(this);
            if (githubUrl != null) {
                mWeatherJsonHourly = NetworkUtils.getResponseFromHttpUrl(githubUrl);
            }
            mHourlyWeatherData = JsonUtils.parseHourlyWeatherData(mWeatherJsonHourly);

        } catch (Exception e) {
            Log.e(TAG, "Error fetching weather data: " + e.getMessage());
        }

        if (mHourlyWeatherData != null && mHourlyWeatherData.size() > 3) {
            return mHourlyWeatherData.get(0);
        } else {
            return null;
        }
    }

    private Notification createNotification() {
        if (mWeatherData == null) {
            Log.e(TAG, "Weather data is null. Cannot create notification.");
            return null;
        }
        // Create notification channel (required for API level >= 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        int weather_id = mWeatherData.getWeather().getId();
        String time = WeatherUtils.convertEpochToTime(mWeatherData.getDt());
        int weatherIconId = WeatherUtils.getResourceIdForWeatherCondition(weather_id, isNight(time));
        String weatherText = getNotificationText(this, mWeatherData, weather_id);
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(weatherIconId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(weatherText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create a task stack builder to navigate to the app when the notification is clicked
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        // Show notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        return builder.build();
    }
}