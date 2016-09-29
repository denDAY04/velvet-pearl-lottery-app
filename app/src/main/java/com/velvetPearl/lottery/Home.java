package com.velvetPearl.lottery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class Home extends Fragment {
    private static final String LOG_TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_home, container, false);

        fragView.findViewById(R.id.home_btn_new_lottery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "onClick: new lottery");
            }
        });

        fragView.findViewById(R.id.home_btn_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "onClick: history");
            }
        });

        return fragView;
    }
}
