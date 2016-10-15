package com.velvetPearl.lottery.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.viewModels.TicketListViewModel;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Tickets extends Fragment implements Observer {

    private static final String LOG_TAG = "TicketsFragment";
    private ListView ticketsListView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tickets, container, false);
        ApplicationDomain.getInstance().addObserver(this);
        ticketsListView = (ListView) root.findViewById(R.id.tickets_list_container);

        if (savedInstanceState == null) {
            updateUi();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "unsubscribing from model updates");
        ApplicationDomain.getInstance().deleteObserver(this);
        super.onDestroyView();
    }

    private void updateUi() {
        final ArrayList<TicketListViewModel> viewModels = convertToViewModels(ApplicationDomain.getInstance().getActiveLottery().getTickets());

        ticketsListView.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, viewModels){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                TextView title = (TextView) itemView.findViewById(android.R.id.text1);
                TextView subTitle = (TextView) itemView.findViewById(android.R.id.text2);

                // Name of the ticket owner
                TicketListViewModel viewModel = viewModels.get(position);
                title.setText(viewModel.toString());

                // List of the numbers on the ticket
                StringBuilder sb = new StringBuilder("");
                for (Integer number : viewModel.getLotteryNumbers()) {
                    sb.append(String.format("%d ", number));
                }
                subTitle.setText(sb.toString());

                return itemView;
            }
        });
    }

    private ArrayList<TicketListViewModel> convertToViewModels(ArrayList<Ticket> tickets) {
        ArrayList<TicketListViewModel> viewModels = new ArrayList<>(tickets.size());
        for(Ticket ticket : tickets) {
            viewModels.add(new TicketListViewModel(ticket));
        }
        return viewModels;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.getClass() == DataAccessEvent.class && arg == DataAccessEvent.TICKET_LIST_UPDATED) {
            updateUi();
        }
    }
}