package com.velvetPearl.lottery.fragments;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.TreeMap;

public class Tickets extends Fragment implements Observer, View.OnClickListener {

    private static final String LOG_TAG = "TicketsFragment";

    private ListView ticketsListView = null;
    private ImageButton newTicketBtn = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tickets, container, false);

        ApplicationDomain.getInstance().addObserver(this);
        ticketsListView = (ListView) root.findViewById(R.id.tickets_list_container);
        newTicketBtn = (ImageButton) root.findViewById(R.id.tickets_new_button);
        newTicketBtn.setOnClickListener(this);

        updateUi();

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

        if (viewModels.size() > 0) {
            ticketsListView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_ticket, R.id.list_item_ticket_owner, viewModels) {
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);
                    TextView ownerLabel = (TextView) itemView.findViewById(R.id.list_item_ticket_owner);
                    TextView numberCntLabel = (TextView) itemView.findViewById(R.id.list_item_ticket_numbers);
                    TextView priceLabel = (TextView) itemView.findViewById(R.id.list_item_ticket_price);

                    // Name of the ticket owner
                    TicketListViewModel viewModel = viewModels.get(position);
                    ownerLabel.setText(viewModel.getOwner());

                    // Number of lottery numbers
                    int numberCount = viewModel.getLotteryNumbers().size();
                    numberCntLabel.setText(String.format(getString(R.string.list_item_ticket_number_count), numberCount));

                    // Price of the ticket
                    priceLabel.setText(String.format(getString(R.string.list_item_ticket_credits), viewModel.getTotalTicketPrice()));

                    return itemView;
                }

            });

            // Add context menu to the tickets
            ticketsListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                // By inspiration of https://www.mikeplate.com/2010/01/21/show-a-context-menu-for-long-clicks-in-an-android-listview/

                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
                    menu.setHeaderTitle(String.format("%s - %s", getString(R.string.ticket), viewModels.get(info.position).getOwner()));

                    // Delete option
                    menu.add(Menu.NONE, 0, 0, R.string.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getContext());
                            dlgBuilder
                                    .setTitle(R.string.attention)
                                    .setMessage(R.string.delete_ticket_confirm)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ApplicationDomain.getInstance().ticketRepository.deleteTicket(viewModels.get(info.position).getEntityModel());
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            return; // don't do anything
                                        }
                                    });
                            dlgBuilder.create().show();
                            return true;
                        }
                    });
                }
            });
            ticketsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bundle args = new Bundle();
                    args.putString("ticketId", (String) viewModels.get(position).getId());
                    Fragment ticketEditFrag = new TicketEdit();
                    ticketEditFrag.setArguments(args);
                    getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, ticketEditFrag).addToBackStack(null).commit();
                }
            });
        } else {
            ArrayList<String> fillerList = new ArrayList<>();
            fillerList.add("");
            ticketsListView.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, fillerList) {
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);
                    TextView title = (TextView) itemView.findViewById(android.R.id.text1);
                    title.setText(R.string.none_found);
                    return itemView;
                };
            });
        }
    }

    private ArrayList<TicketListViewModel> convertToViewModels(TreeMap<Object, Ticket> tickets) {
        ArrayList<TicketListViewModel> viewModels = new ArrayList<>(tickets.size());
        for(Object key : tickets.keySet()) {
            viewModels.add(new TicketListViewModel(tickets.get(key)));
        }
        return viewModels;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.getClass() == DataAccessEvent.class
                && (arg == DataAccessEvent.TICKET_LIST_UPDATE || arg == DataAccessEvent.LOTTERY_NUMBER_UPDATE)) {
            updateUi();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == newTicketBtn) {

            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new TicketEdit()).addToBackStack(null).commit();
        }
    }
}