package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.viewModels.WinnerListViewModel;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

/**
 * Created by Stensig on 30-Oct-16.
 */

public class Winners extends Fragment implements View.OnClickListener, Observer {

    private static final String LOG_TAG = "WinnersFragment";

    private ImageButton drawWinnerBtn;
    private TextView titleLabel;
    private ListView winnersListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        ApplicationDomain.getInstance().addObserver(this);

        initUi(root);
        updateUi();

        return root;
    }

    @Override
    public void onDestroyView() {
        ApplicationDomain.getInstance().deleteObserver(this);
        super.onDestroyView();
    }

    private void initUi(View root) {
        drawWinnerBtn = (ImageButton) root.findViewById(R.id.list_new_button);
        titleLabel = (TextView) root.findViewById(R.id.list_title);
        winnersListView = (ListView) root.findViewById(R.id.list_container);

        drawWinnerBtn.setOnClickListener(this);
        titleLabel.setText(R.string.winners);
    }

    public void updateUi() {
        final ArrayList<WinnerListViewModel> viewModels = new ArrayList<>();
        TreeMap<Object, Prize> prizes = ApplicationDomain.getInstance().getActiveLottery().getPrizes();
        for (Object prizeId : prizes.keySet()) {
            Prize prize = prizes.get(prizeId);
            if (prize.getNumberId() != null) {
                viewModels.add(new WinnerListViewModel(prize));
            }
        }

        if (viewModels.size() > 0) {
            winnersListView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_winner, R.id.list_item_ticket_owner, viewModels){
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);

                    TextView lotteryNumber = (TextView) itemView.findViewById(R.id.list_item_lottery_num);
                    TextView ticketOwner = (TextView) itemView.findViewById(R.id.list_item_ticket_owner);
                    TextView prizeName = (TextView) itemView.findViewById(R.id.list_item_prize_name);
                    WinnerListViewModel model = viewModels.get(position);

                    lotteryNumber.setText(Integer.toString(model.getLotteryNumber()));
                    ticketOwner.setText(model.getTicketOwner());
                    prizeName.setText(model.getPrize().getName());

                    return itemView;
                }
            });

            // TODO Add context menu for delete + click listener for navigating to ticket
        } else {
            String[] tempList = new String[] {getString(R.string.none_found)};
            winnersListView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_prize, R.id.list_item_prize_name, tempList));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == drawWinnerBtn) {
            Log.d(LOG_TAG, "drawing winner");
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.WINNER_UPDATE) {
                updateUi();
            }
        }
    }
}
