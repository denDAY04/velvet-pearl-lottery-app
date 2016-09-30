package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.models.Ticket;


public class Welcome extends Fragment {
    private static final String LOG_TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_welcome, container, false);

        fragView.findViewById(R.id.home_btn_new_lottery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "onClick: new lottery");

                String lotteryId = "-KSWN3Cc8asmcV6zklc0";
                Ticket ticket = new Ticket();
                ticket.setOwner("");

            }
        });

        fragView.findViewById(R.id.home_btn_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "onClick: history");
                getFragmentManager().beginTransaction().replace(R.id.main_fragment, new History()).addToBackStack(null).commit();
            }
        });

        return fragView;
    }
}
