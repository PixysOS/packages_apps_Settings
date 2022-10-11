/*
 * Copyright (C) 2022 The PixelDust Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.pixys.settings.theme;

import android.content.Context;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class ThemeSettingsPreferenceController extends BasePreferenceController {

    private static final String PREF_KEY_THEMEMENU = "theme_customization_summary";

    public ThemeSettingsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getText(R.string.theme_customization_summary);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY_THEMEMENU;
    }
}
