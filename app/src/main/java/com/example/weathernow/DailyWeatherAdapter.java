package com.example.weathernow;

import static com.example.weathernow.utilities.WeatherUtils.formatTemperature;
import static com.example.weathernow.utilities.WeatherUtils.toTitleCase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathernow.daily_data.DailyWeatherEntry;
import com.example.weathernow.utilities.WeatherUtils;

import java.util.List;

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder> {

    List<DailyWeatherEntry> dailyWeatherDataList;
    final Context context;

    public void setWeatherDataList(List<DailyWeatherEntry> dailyWeatherDataList) {
        this.dailyWeatherDataList = dailyWeatherDataList;
        notifyDataSetChanged();
    }

    public DailyWeatherAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public DailyWeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.daily_forecast_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyWeatherAdapter.ViewHolder holder, int position) {
        DailyWeatherEntry dailyWeatherData = dailyWeatherDataList.get(position);

        holder.description.setText(toTitleCase(dailyWeatherData.getWeather().getDescription()));

        holder.date.setText(WeatherUtils.convertEpochToDate(dailyWeatherData.getDt()));

        String minTemp = formatTemperature(context, dailyWeatherData.getTemperature().getMin());
        holder.min.setText(minTemp);
        String maxTemp = formatTemperature(context, dailyWeatherData.getTemperature().getMax());
        holder.max.setText(maxTemp);

        int weatherId = dailyWeatherData.getWeather().getId();
        int weatherIconId = WeatherUtils.getResourceIdForWeatherCondition(weatherId,false);
        holder.icon.setImageResource(weatherIconId);
    }

    @Override
    public int getItemCount() {
        return dailyWeatherDataList == null ? 0 : dailyWeatherDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView date;
        final TextView description;
        final TextView min;
        final TextView max;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.weather_icon);
            date = itemView.findViewById(R.id.date);
            description = itemView.findViewById(R.id.weather_description);
            min = itemView.findViewById(R.id.low_temperature);
            max = itemView.findViewById(R.id.high_temperature);
        }
    }
}
