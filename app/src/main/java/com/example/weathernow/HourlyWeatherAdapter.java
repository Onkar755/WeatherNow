package com.example.weathernow;

import static com.example.weathernow.utilities.WeatherUtils.formatTemperature;
import static com.example.weathernow.utilities.WeatherUtils.isNight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathernow.hourly_data.HourlyWeatherData;
import com.example.weathernow.utilities.WeatherUtils;

import java.util.List;

public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.ViewHolder> {
    final Context context;

    public HourlyWeatherAdapter(Context context) {
        this.context = context;
    }

    private List<HourlyWeatherData> hourlyWeatherDataList;

    public void setWeatherDataList(List<HourlyWeatherData> hourlyWeatherDataList) {
        this.hourlyWeatherDataList = hourlyWeatherDataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HourlyWeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.hourly_forecast_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyWeatherAdapter.ViewHolder holder, int position) {
        HourlyWeatherData hourlyWeatherData = hourlyWeatherDataList.get(position);
        String time = WeatherUtils.convertEpochToTime(hourlyWeatherData.getDt());
        if (position == 0) {
            holder.timeTextview.setText(R.string.txt_now);
        } else {
            holder.timeTextview.setText(time);
        }

        String temperature = formatTemperature(context, hourlyWeatherData.getMain().getTemp());
        holder.minTempTextview.setText(temperature);

        int weatherId = hourlyWeatherData.getWeather().getId();
        int weatherIconId = WeatherUtils.getResourceIdForWeatherCondition(weatherId, isNight(time));
        holder.icon.setImageResource(weatherIconId);
    }

    @Override
    public int getItemCount() {
        return hourlyWeatherDataList != null ? hourlyWeatherDataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView timeTextview;
        final TextView minTempTextview;
        final ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextview = itemView.findViewById(R.id.hourly_weather_time);
            minTempTextview = itemView.findViewById(R.id.hourly_weather_temp_min);
            icon = itemView.findViewById(R.id.hourly_weather_icon);
        }
    }
}
