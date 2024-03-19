package com.example.weathernow.utilities;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static final String BASE_URL_HOURLY_DATA = "https://pro.openweathermap.org/data/2.5/forecast/hourly?";
    private static final String BASE_URL_DAILY_DATA = "https://api.openweathermap.org/data/2.5/forecast/daily?";
    private static final String PARAM_API = "APPID";

    private static final String PARAM_QUERY = "q";

    private static final String PARAM_UNIT = "units";

    private static final String PARAM_COUNT = "cnt";

    private static final String PARAM_FORMAT = "mode";

    private static final String API_KEY = "API-key";

    private static final int hourly_count = 29;
    private static final int daily_count = 15;
    private static final String format = "json";
    private static SharedPreferences sharedPreferences;

    public static URL buildUrl_HourlyData(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Uri uri = Uri.parse(BASE_URL_HOURLY_DATA).buildUpon()
                .appendQueryParameter(PARAM_API, API_KEY)
                .appendQueryParameter(PARAM_QUERY, sharedPreferences.getString("location", "Nashik, MH"))
                .appendQueryParameter(PARAM_UNIT, sharedPreferences.getString("unit", "metric"))
                .appendQueryParameter(PARAM_COUNT, String.valueOf(hourly_count))
                .appendQueryParameter(PARAM_FORMAT, format)
                .build();

        URL url;
        try {
            url = new URL(uri.toString());
            Log.v("Built URL", url.toString());
            return url;
        } catch (MalformedURLException e) {
            Log.e(TAG, "An error occurred", e);
            return null;
        }
    }


    public static URL buildURL_DailyData(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Uri uri = Uri.parse(BASE_URL_DAILY_DATA).buildUpon()
                .appendQueryParameter(PARAM_API, API_KEY)
                .appendQueryParameter(PARAM_QUERY, sharedPreferences.getString("location", "Nashik, MH"))
                .appendQueryParameter(PARAM_UNIT, sharedPreferences.getString("unit", "metric"))
                .appendQueryParameter(PARAM_COUNT, String.valueOf(daily_count))
                .appendQueryParameter(PARAM_FORMAT, format)
                .build();
        URL url;
        try {
            url = new URL(uri.toString());
            Log.v("Built URL", url.toString());
            return url;
        } catch (MalformedURLException e) {
            Log.e(TAG, "An error occurred", e);
            return null;
        }
    }

    public static URL buildTestPreferenceLocation(String query) {
        Uri uri = Uri.parse(BASE_URL_HOURLY_DATA).buildUpon()
                .appendQueryParameter(PARAM_API, API_KEY)
                .appendQueryParameter(PARAM_QUERY, query)
                .build();
        URL url;
        try {
            url = new URL(uri.toString());
            Log.v("Built Test URL", url.toString());
            return url;
        } catch (MalformedURLException e) {
            Log.e(TAG, "An error occurred", e);
            return null;
        }
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }
}
