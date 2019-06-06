/*
 * Copyright (C) 2019 The Pixys Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.pixys.settings.preferences.CustomSeekBarPreference;

public class RoundedCorners extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener {

    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_STATUS_BAR_PADDING = "sysui_status_bar_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";

    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private CustomSeekBarPreference mSBPadding;
    private SwitchPreference mRoundedFwvals;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rounded_corners);

        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int cornerRadius = Settings.Secure.getIntForUser(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                (int) (res.getDimension(resourceIdRadius) / density), UserHandle.USER_CURRENT);
        mCornerRadius.setValue(cornerRadius);
        mCornerRadius.setOnPreferenceChangeListener(this);

        // Rounded Content Padding
        mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int contentPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                (int) (res.getDimension(resourceIdPadding) / density), UserHandle.USER_CURRENT);
        mContentPadding.setValue(contentPadding);
        mContentPadding.setOnPreferenceChangeListener(this);

        // Status Bar Content Padding
        mSBPadding = (CustomSeekBarPreference) findPreference(SYSUI_STATUS_BAR_PADDING);
        int resourceIdSBPadding = res.getIdentifier("com.android.systemui:dimen/status_bar_extra_padding", null,
                null);
        int sbPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
                Settings.Secure.SYSUI_STATUS_BAR_PADDING,
                (int) (res.getDimension(resourceIdSBPadding) / density), UserHandle.USER_CURRENT);
        mSBPadding.setValue(sbPadding);
        mSBPadding.setOnPreferenceChangeListener(this);

        // Rounded use Framework Values
        mRoundedFwvals = (SwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);
    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = getContext().getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int resourceIdSBPadding = res.getIdentifier("com.android.systemui:dimen/status_bar_extra_padding", null,
                null);
        mCornerRadius.setValue((int) (res.getDimension(resourceIdRadius) / density));
        mContentPadding.setValue((int) (res.getDimension(resourceIdPadding) / density));
        mSBPadding.setValue((int) (res.getDimension(resourceIdSBPadding) / density));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCornerRadius) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                    (int) newValue, UserHandle.USER_CURRENT);
        } else if (preference == mContentPadding) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                    (int) newValue, UserHandle.USER_CURRENT);
        } else if (preference == mSBPadding) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_STATUS_BAR_PADDING,
                    (int) newValue, UserHandle.USER_CURRENT);
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.PIXYS_SETTINGS;
    }
}
