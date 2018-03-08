/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.SHOW_BATTERY_PERCENT;

/**
 * A controller to manage the switch for showing battery percentage in the status bar.
 */

public class BatteryPercentagePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "BatteryPercentagePref";

    private static final String KEY_BATTERY_PERCENTAGE = "battery_percentage";

    private DropDownPreference mBatteryPercentage;

    public BatteryPercentagePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BATTERY_PERCENTAGE;
    }

    @Override
    public void updateState(Preference preference) {
        final DropDownPreference mBatteryPercentage = (DropDownPreference) preference;
        if (mBatteryPercentage != null) {
            int batteryPercentageSetting = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT, 0,
                    UserHandle.USER_CURRENT);
            String percentageSetting = String.valueOf(batteryPercentageSetting);
            mBatteryPercentage.setValue(percentageSetting);
            updateBatteryPercentageSummary(mBatteryPercentage, percentageSetting);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String percentageSetting = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.SHOW_BATTERY_PERCENT,
                    Integer.parseInt(percentageSetting), UserHandle.USER_CURRENT);
            updateBatteryPercentageSummary((DropDownPreference) preference, percentageSetting);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not persist battery percentage setting", e);
        }
        return true;
    }

    private void updateBatteryPercentageSummary(Preference mBatteryPercentage, String percentageSetting) {
        if (percentageSetting != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .battery_percentage_values);
            final int summaryArrayResId = R.array.battery_percentage_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (percentageSetting.equals(values[i])) {
                    if (i < summaries.length) {
                        mBatteryPercentage.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mBatteryPercentage.setSummary("");
        Log.e(TAG, "Invalid battery percentage value: " + percentageSetting);
    }
}
