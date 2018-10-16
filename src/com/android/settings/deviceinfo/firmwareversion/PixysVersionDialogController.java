/*
 * Copyright (C) 2018 AospExtended ROM Project
 * Copyright (C) 2018 PixysOS
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.os.SELinux;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.content.res.Resources;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import android.os.SystemProperties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PixysVersionDialogController  implements View.OnClickListener {

    private static final String TAG = "ExtfirmwareDialogCtrl";
    private static final int DELAY_TIMER_MILLIS = 500;
    private static final int ACTIVITY_TRIGGER_COUNT = 3;

    @VisibleForTesting
    static final int PIXYS_VERSION_VALUE_ID = R.id.pixys_version_value;
    @VisibleForTesting
    static final int PIXYS_VERSION_LABEL_ID = R.id.pixys_version_label;

    private final FirmwareVersionDialogFragment mDialog;
    private final Context mContext;
    private final UserManager mUserManager;
    private final long[] mHits = new long[ACTIVITY_TRIGGER_COUNT];

    private RestrictedLockUtils.EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;

    public PixysVersionDialogController(FirmwareVersionDialogFragment dialog) {
        mDialog = dialog;
        mContext = dialog.getContext();
        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
    }

    @Override
    public void onClick(View v) {
        arrayCopy();
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DELAY_TIMER_MILLIS)) {
            if (mUserManager.hasUserRestriction(UserManager.DISALLOW_FUN)) {
                if (mFunDisallowedAdmin != null && !mFunDisallowedBySystem) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(mContext,
                            mFunDisallowedAdmin);
                }
                Log.d(TAG, "Sorry, no fun for you!");
                return;
            }

            final Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(
                            "android", com.android.internal.app.PlatLogoActivity.class.getName());
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Unable to start activity " + intent.toString());
            }
        }
    }

    /**
     * Updates the pixys version to the dialog.
     */
    public void initialize() {
        initializeAdminPermissions();
        registerClickListeners();

        initializeSelinuxProperties();

        mDialog.setText(PIXYS_VERSION_VALUE_ID,
                BidiFormatter.getInstance().unicodeWrap(Build.PIXYS_DISPLAY_VERSION));
    }

    private void registerClickListeners() {
        mDialog.registerClickListener(PIXYS_VERSION_LABEL_ID, this /* listener */);
        mDialog.registerClickListener(PIXYS_VERSION_VALUE_ID, this /* listener */);
    }

    /**
     * Fetch the selinux status and export it to selinuxStatus.
     */
    static final String PROPERTY_SELINUX_STATUS = "ro.boot.selinux";
    private String selinuxStatus;
    private View mRootView;
    private Context context;
    private void initializeSelinuxProperties() {

	String selinuxStatusProp =  SystemProperties.get(PROPERTY_SELINUX_STATUS);
	//SELinux status check, taken from SELinuxPreferenceController.java
        if (selinuxStatusProp != null || SELinux.isSELinuxEnabled()) {
            if (SELinux.isSELinuxEnforced()) {
                selinuxStatus = context.getResources().getString(R.string.selinux_status_enforcing);
	    } else if (!SELinux.isSELinuxEnforced()) {
                selinuxStatus = context.getResources().getString(R.string.selinux_status_permissive);
       	    } else {
                selinuxStatus = context.getResources().getString(R.string.selinux_status_disabled);
	    }
        }
        // export string to the id
        TextView textview = mRootView.findViewById(R.id.selinux_status_value);
        textview.setText(selinuxStatus);
    }

    /**
     * Copies the array onto itself to remove the oldest hit.
     */
    @VisibleForTesting
    void arrayCopy() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
    }

    @VisibleForTesting
    void initializeAdminPermissions() {
        mFunDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(
                mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
        mFunDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(
                mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
    }
}
