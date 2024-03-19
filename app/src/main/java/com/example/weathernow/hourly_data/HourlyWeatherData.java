package com.example.weathernow.hourly_data;

public class HourlyWeatherData {
    private Main main;
    private WeatherH weather;
    private String dt_txt;

    public Main getMain() {
        return main;
    }

    public WeatherH getWeather() {
        return weather;
    }

    public String getDt() {
        return dt_txt;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setWeather(WeatherH weather) {
        this.weather = weather;
    }


    public void setDt(String dt_txt) {
        this.dt_txt = dt_txt;
    }
}
