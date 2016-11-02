package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;


import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;

/**
 * Created by Stensig on 02-Nov-16.
 *
 * Preference theme fix using https://github.com/Gericop/Android-Support-Preference-V7-Fix
 */

public class Preferences extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        MainActivity activity = (MainActivity) getActivity();
        activity.disableActiveLotteryMenuItems();
        activity.setTitle(R.string.settings);

        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
