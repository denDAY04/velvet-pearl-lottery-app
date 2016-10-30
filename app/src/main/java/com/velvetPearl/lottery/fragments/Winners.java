package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.velvetPearl.lottery.R;

/**
 * Created by Stensig on 30-Oct-16.
 */

public class Winners extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = "WinnersFragment";

    private ImageButton drawWinnerBtn;
    private TextView titleLabel;
    private ListView winnersListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        initUi(root);

        return root;
    }

    private void initUi(View root) {
        drawWinnerBtn = (ImageButton) root.findViewById(R.id.list_new_button);
        titleLabel = (TextView) root.findViewById(R.id.list_title);
        winnersListView = (ListView) root.findViewById(R.id.list_container);

        drawWinnerBtn.setOnClickListener(this);
        titleLabel.setText(R.string.winners);
    }

    @Override
    public void onClick(View v) {
        if (v == drawWinnerBtn) {
            Log.d(LOG_TAG, "drawing winner");
        }
    }
}
