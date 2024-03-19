package com.example.weathernow.daily_data;

public class DailyWeatherEntry {
    private long dt;
    private Temperature temperature;
    private WeatherD weather;

    public void setDt(long dt) {
        this.dt = dt;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public void setWeather(WeatherD weather) {
        this.weather = weather;
    }

    public long getDt() {
        return dt;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public WeatherD getWeather() {
        return weather;
    }
}
