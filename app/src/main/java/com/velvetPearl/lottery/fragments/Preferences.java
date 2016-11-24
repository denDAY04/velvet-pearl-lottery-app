package com.velvetPearl.lottery.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;


import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.NotificationService;
import com.velvetPearl.lottery.R;

/**
 * Created by Stensig on 02-Nov-16.
 *
 */

public class Preferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "PreferencesFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        MainActivity activity = (MainActivity) getActivity();
        activity.disableActiveLotteryMenuItems();
        activity.setTitle(R.string.settings);

        setPreferencesFromResource(R.xml.preferences, rootKey);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preference_lottery_notifications))) {
            if (sharedPreferences.getBoolean(key, true)){
                Log.i(LOG_TAG, "Lottery notifications enabled.");
                if (ApplicationDomain.getInstance().notificationService != null ) {
                    Log.i(LOG_TAG, "Lottery notifications already running. Stopping service.");
                    getActivity().stopService(ApplicationDomain.getInstance().notificationService);
                }
                Log.i(LOG_TAG, "Starting lottery notification service.");
                ApplicationDomain.getInstance().notificationService = new Intent(getActivity(), NotificationService.class);
                getActivity().startService(ApplicationDomain.getInstance().notificationService);
            } else {
                Log.i(LOG_TAG, "Lottery notifications disabled.");
                if (ApplicationDomain.getInstance().notificationService != null) {
                    Log.i(LOG_TAG, "Stopping lottery notification service.");
                    getActivity().stopService(ApplicationDomain.getInstance().notificationService);
                    ApplicationDomain.getInstance().notificationService = null;
                }
            }
        }
    }
}
