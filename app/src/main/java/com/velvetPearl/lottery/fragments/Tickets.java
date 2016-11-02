package com.velvetPearl.lottery.fragments;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.viewModels.TicketListViewModel;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

public class Tickets extends Fragment implements Observer, View.OnClickListener {

    private static final String LOG_TAG = "TicketsFragment";

    // UI fields
    private ListView ticketsListView = null;
    private ImageButton newTicketBtn = null;
    private TextView title = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        ApplicationDomain.getInstance().addObserver(this);
        getActivity().setTitle(R.string.tickets);
        ((MainActivity) getActivity()).enableActiveLotteryMenuItems();
        title = (TextView) root.findViewById(R.id.list_title);
        title.setText(R.string.tickets);
        ticketsListView = (ListView) root.findViewById(R.id.list_container);
        newTicketBtn = (ImageButton) root.findViewById(R.id.list_new_button);
        newTicketBtn.setOnClickListener(this);


        updateUi();

        return root;
    }

    @Override
    public void onDestroyView() {
        ticketsListView = null;
        newTicketBtn = null;
        title = null;
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

            // Open ticket for edit on click
            ticketsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bundle args = new Bundle();
                    args.putString("ticketId", (String) viewModels.get(position).getId());
                    Fragment ticketEditFrag = new TicketEdit();
                    ticketEditFrag.setArguments(args);
                    ApplicationDomain.getInstance().setEditingTicketFromLottery((String) viewModels.get(position).getId());
                    getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, ticketEditFrag).addToBackStack(null).commit();
                }
            });
        } else {
            String[] tempList = new String[] {getString(R.string.none_found)};
            ticketsListView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_prize, R.id.list_item_prize_name, tempList));
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
            ApplicationDomain.getInstance().resetEditingTicket();

            if (ApplicationDomain.getInstance().allLotteryNumbersTaken()) {
                Toast.makeText(getContext(), R.string.no_lottery_numbers_left, Toast.LENGTH_LONG).show();
                return;
            }

            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new TicketEdit()).addToBackStack(null).commit();
        }
    }
}