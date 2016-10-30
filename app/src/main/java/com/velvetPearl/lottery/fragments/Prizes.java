package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.velvetPearl.lottery.viewModels.PrizeListViewModel;
import com.velvetPearl.lottery.viewModels.TicketListViewModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

/**
 * Created by Stensig on 29-Oct-16.
 */

public class Prizes extends Fragment implements Observer, View.OnClickListener {

    private static final String LOG_TAG = "PrizesFragment";

    // UI fields
    private TextView title = null;
    private ListView listView = null;
    private ImageButton newButton = null;

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
        title = null;
        listView = null;
        newButton = null;
        ApplicationDomain.getInstance().deleteObserver(this);
        super.onDestroyView();
    }

    private void initUi(View root) {
        title = (TextView) root.findViewById(R.id.list_title);
        title.setText(R.string.prizes);

        listView = (ListView) root.findViewById(R.id.list_container);

        newButton = (ImageButton) root.findViewById(R.id.list_new_button);
        newButton.setOnClickListener(this);
    }

    private void updateUi() {
        TreeMap<Object, Prize> activePrizes = ApplicationDomain.getInstance().getActiveLottery().getPrizes();
        final ArrayList<Prize> prizes = new ArrayList<>(activePrizes.size());
        for (Object prizeId : activePrizes.keySet()) {
            prizes.add(activePrizes.get(prizeId));
        }

        if (prizes.size() > 0) {
            listView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_prize, R.id.list_item_prize_name, prizes) {
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);
                    TextView prizeName = (TextView) itemView.findViewById(R.id.list_item_prize_name);

                    prizeName.setText(prizes.get(position).getName());

                    return itemView;
                }

            });
        } else {
            String[] tempList = new String[] {getString(R.string.none_found)};
            listView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_prize, R.id.list_item_prize_name, tempList));
        }
    }

    private ArrayList<PrizeListViewModel> parseToViewModels(TreeMap<Object, Prize> prizes) {
        ArrayList<PrizeListViewModel> viewModels = new ArrayList<>();

        if (prizes != null) {
            for (Object prizeId : prizes.keySet()) {
                viewModels.add(new PrizeListViewModel(prizes.get(prizeId)));
            }
        }

        return viewModels;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.PRIZE_LIST_UPDATE) {
                updateUi();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == newButton) {
            // TODO switch view to new prize
        }
    }
}
