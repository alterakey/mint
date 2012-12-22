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

package com.gmail.altakey.mint;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

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

        if (ConfigKey.USER_ID.equals(key)) {
            Authenticator.purge(this);
        }
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        if (key.equals(ConfigKey.USER_ID)) {
            mUserId.setSummary(mUserId.getText());
        }
    }

}
