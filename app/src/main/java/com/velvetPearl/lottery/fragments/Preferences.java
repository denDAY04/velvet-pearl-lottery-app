package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;


import com.velvetPearl.lottery.R;

/**
 * Created by Stensig on 02-Nov-16.
 */

public class Preferences extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
