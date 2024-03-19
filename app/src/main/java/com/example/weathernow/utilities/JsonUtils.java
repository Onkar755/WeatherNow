package com.example.weathernow.utilities;

import com.example.weathernow.daily_data.DailyWeatherEntry;
import com.example.weathernow.daily_data.WeatherD;
import com.example.weathernow.daily_data.Temperature;
import com.example.weathernow.hourly_data.Main;
import com.example.weathernow.hourly_data.HourlyWeatherData;
import com.example.weathernow.hourly_data.WeatherH;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JsonUtils {

    public static List<HourlyWeatherData> parseHourlyWeatherData(String jsonResponse) throws JSONException {
        List<HourlyWeatherData> hourlyWeatherDataList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonResponse);

        JSONArray weatherList = jsonObject.getJSONArray("list");
        int start = isBetween() ? 5 : 4;
        int end = start + 24;

        for (int i = start; i < end; i++) {
            JSONObject entry = weatherList.getJSONObject(i);
            HourlyWeatherData hourlyWeatherDataEntry = new HourlyWeatherData();

            hourlyWeatherDataEntry.setDt(entry.getString("dt_txt"));

            Main main = new Main();
            main.setTemp(entry.getJSONObject("main").getDouble("temp"));
            main.setTemp_min(entry.getJSONObject("main").getDouble("temp_min"));
            main.setTemp_max(entry.getJSONObject("main").getDouble("temp_max"));
            hourlyWeatherDataEntry.setMain(main);

            JSONArray weatherArray = entry.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weatherObject = weatherArray.getJSONObject(0); // Get the first item
                WeatherH hourlyWeatherInfo = new WeatherH();
                hourlyWeatherInfo.setId(weatherObject.getInt("id"));
                hourlyWeatherInfo.setDescription(weatherObject.getString("description"));
                hourlyWeatherDataEntry.setWeather(hourlyWeatherInfo);
            }
            hourlyWeatherDataList.add(hourlyWeatherDataEntry);
        }
        return hourlyWeatherDataList;
    }

    public static List<DailyWeatherEntry> parseDailyWeatherData(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        List<DailyWeatherEntry> dailyWeatherDataList = new ArrayList<>();
        // Parse list array
        JSONArray listArray = jsonObject.getJSONArray("list");

        for (int i = 1; i < listArray.length(); i++) {
            JSONObject itemObject = listArray.getJSONObject(i);

            DailyWeatherEntry dailyWeatherEntry = new DailyWeatherEntry();

            dailyWeatherEntry.setDt(itemObject.getLong("dt"));

            // Parse Temperature object
            JSONObject tempObject = itemObject.getJSONObject("temp");

            Temperature temperature = new Temperature();
            temperature.setMin(tempObject.getDouble("min"));
            temperature.setMax(tempObject.getDouble("max"));
            dailyWeatherEntry.setTemperature(temperature);


            // Parse Weather object
            JSONArray weatherArray = itemObject.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                WeatherD weather = new WeatherD();
                weather.setId(weatherObject.getInt("id"));
                weather.setDescription(weatherObject.getString("description"));
                dailyWeatherEntry.setWeather(weather);
            }
            dailyWeatherDataList.add(dailyWeatherEntry);
        }
        return dailyWeatherDataList;
    }

    public static boolean isBetween() {
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE); // Get the current minute

        // Check if the current time's minute component is between 0 and 30 for every hour
        return minute < 30;
    }

}
