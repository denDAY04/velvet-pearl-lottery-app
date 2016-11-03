package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;


public class Welcome extends Fragment {
    private static final String LOG_TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_welcome, container, false);

        getActivity().setTitle(R.string.velvet_pearl_lottery);

        fragView.findViewById(R.id.home_btn_new_lottery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new NewLottery()).commit();
            }
        });

        fragView.findViewById(R.id.home_btn_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new History()).addToBackStack(null).commit();
            }
        });

        ((MainActivity) getActivity()).disableActiveLotteryMenuItems();

        return fragView;
    }
}
