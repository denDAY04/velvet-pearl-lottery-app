package com.velvetPearl.lottery.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.LotterySingleton;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.viewModels.LotteryListViewModel;
import com.velvetPearl.lottery.viewModels.TicketListViewModel;

import java.util.ArrayList;

public class Tickets extends Fragment {

    private ListView ticketsListView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tickets, container, false);

        ticketsListView = (ListView) root.findViewById(R.id.tickets_list_container);

        if (savedInstanceState == null) {
            initUi();
        }

        return root;
    }

    private void initUi() {
        final ArrayList<TicketListViewModel> viewModels = convertToViewModels(LotterySingleton.getActiveLottery().getTickets());

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
                    sb.append(String.format("%d "));
                }
                subTitle.setText(sb.toString());

                return itemView;
            }
        });


//        ticketsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TicketListViewModel item = (TicketListViewModel) ticketsListView.getItemAtPosition(i);
//                Bundle args = new Bundle();
//                args.putString("ticketId", (String) item.getId());
//                android.support.v4.app.Fragment destination = new LotteryHome();
//                destination.setArguments(args);
//                getFragmentManager().beginTransaction().replace(R.id.main_fragment,destination).addToBackStack(null).commit();
//            }
//        });
    }

    private ArrayList<TicketListViewModel> convertToViewModels(ArrayList<Ticket> tickets) {
        ArrayList<TicketListViewModel> viewModels = new ArrayList<>(tickets.size());
        for(Ticket ticket : tickets) {
            viewModels.add(new TicketListViewModel(ticket));
        }
        return viewModels;
    }
}