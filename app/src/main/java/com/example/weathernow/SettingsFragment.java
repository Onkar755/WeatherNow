package com.example.weathernow;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.example.weathernow.utilities.NetworkUtils;
import com.example.weathernow.utilities.WeatherUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    String mWeatherJsonHourly;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    boolean isValid;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.settings_preference);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = prefScreen.getPreference(i);
            if (!(preference instanceof CheckBoxPreference)) {
                assert sharedPreferences != null;
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
                if (preference.getKey().equals("location")) {
                    EditTextPreference locationPreference = (EditTextPreference) preference;
                    locationPreference.setOnPreferenceChangeListener((preference1, newValue) -> {
                        String newLocation = newValue.toString().toLowerCase();
                        checkLocationValidity(newLocation);
                        // Validate location format
                        if (isValid) {
                            // Location format is valid, update preference summary and save preference
                            setPreferenceSummary(preference1, newLocation);
                            return true;
                        } else {
                            // Location format is invalid, show toast and do not save preference
                            Toast.makeText(getContext(), "Invalid location. Please enter a valid location name.", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    });
                }
            }
        }
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        Objects.requireNonNull(Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()))
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        assert key != null;
        Preference preference = findPreference(key);
        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            editTextPreference.setSummary(WeatherUtils.toTitleCase(editTextPreference.getText()));
        } else if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }
    }


    private void checkLocationValidity(String location) {
        executorService.execute(() -> {
            try {
                URL githubUrl = NetworkUtils.buildTestPreferenceLocation(location);
                if (githubUrl != null) {
                    mWeatherJsonHourly = NetworkUtils.getResponseFromHttpUrl(githubUrl);
                }
                JSONObject jsonObject = new JSONObject(mWeatherJsonHourly);
                if (jsonObject.has("cod")) {
                    int cod = jsonObject.getInt("cod");
                    // Check if cod is equal to 200 (or any other valid value)
                    isValid = cod == 200;
                }
            } catch (IOException e) {
                Log.e(TAG, "An error occurred", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
