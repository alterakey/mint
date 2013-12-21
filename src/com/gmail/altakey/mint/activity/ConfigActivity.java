/**
 * Copyright (C) 2011-2012 Takahiro Yoshimura
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gmail.altakey.mint.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference;

import com.gmail.altakey.mint.R;
import com.gmail.altakey.mint.konst.ConfigKey;
import com.gmail.altakey.mint.util.Authenticator;
import com.gmail.altakey.mint.util.Notifier;

public class ConfigActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private EditTextPreference mUserId;

    private EditTextPreference mUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config);
        mUserId = (EditTextPreference)getPreferenceScreen().findPreference(ConfigKey.USER_ID);
        mUserPassword = (EditTextPreference)getPreferenceScreen().findPreference(ConfigKey.USER_PASSWORD);
        getPreferenceScreen().findPreference(ConfigKey.RESET_NOTIFICATIONS).setOnPreferenceClickListener(new ResetNotificationAction());
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        updateSummary(sharedPreferences, ConfigKey.USER_ID);
        updateSummary(sharedPreferences, ConfigKey.USER_PASSWORD);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(sharedPreferences, key);

        if (ConfigKey.USER_ID.equals(key)
            || ConfigKey.USER_PASSWORD.equals(key)) {
            Authenticator.purge(this);
        }
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        if (key.equals(ConfigKey.USER_ID)) {
            mUserId.setSummary(mUserId.getText());
        }
    }

    private class ResetNotificationAction implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference pref) {
            Notifier notifier = new Notifier(ConfigActivity.this);
            notifier.clear();
            notifier.info("Notifications are reset.");
            return true;
        }
    }

}
