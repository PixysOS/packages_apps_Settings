/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.settings.aoscp.security;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.util.ambient.weather.WeatherClient;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.WEATHER_LOCKSCREEN_UNIT;

public class WeatherTempPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_WEATHER_TEMP = "weather_temp";

    private Context mContext;
    private ListPreference mPref;

    public WeatherTempPreferenceController(Context context) {
        super(context, KEY_WEATHER_TEMP);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return WeatherClient.isAvailable(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPref = (ListPreference) screen.findPreference(getPreferenceKey());
        if (mPref == null) return;
        int value = Settings.System.getInt(mContext.getContentResolver(), WEATHER_LOCKSCREEN_UNIT, 1);
        mPref.setValue(Integer.toString(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        Settings.System.putInt(mContext.getContentResolver(), WEATHER_LOCKSCREEN_UNIT, value);
        refreshSummary(preference);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        int value = Settings.System.getInt(mContext.getContentResolver(), WEATHER_LOCKSCREEN_UNIT, 1);
        int index = mPref.findIndexOfValue(Integer.toString(value));
        return mPref.getEntries()[index];
    }
}
