package com.velvetPearl.lottery.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.viewModels.LotteryNumberListViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

/**
 * Created by Stensig on 19-Oct-16.
 */

public class TicketEdit extends Fragment implements Observer, View.OnClickListener {

    private static final String LOG_TAG = "TicketEditFragment";

    // UI elements
    private EditText ownerInput = null;
    private TextView priceLabel = null;
    private ListView numberList = null;
    private Button saveBtn = null;
    private ImageButton newLotteryNumberBtn = null;

    private String ticketId = null;
    private Ticket model;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ticket_edit, container, false);

        ApplicationDomain.getInstance().addObserver(this);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args != null) {
                ticketId = args.getString("ticketId");
                model = ApplicationDomain.getInstance().getActiveLottery().getTickets().get(ticketId);
            } else {
                model = new Ticket();
                model.setLotteryId(ApplicationDomain.getInstance().getActiveLottery().getId());
            }
        }

        initUi(root);
        return root;
    }

    @Override
    public void onDestroyView() {
        ApplicationDomain.getInstance().deleteObserver(this);
        model.getUnsavedLotteryNumbers().clear();   // Don't let unsaved lottery numbers carry over
        super.onDestroyView();
    }

    private void initUi(View fragmentView) {
        ownerInput = (EditText) fragmentView.findViewById(R.id.ticket_edit_owner_input);
        priceLabel = (TextView) fragmentView.findViewById(R.id.ticket_edit_price_var);
        numberList = (ListView) fragmentView.findViewById(R.id.ticket_edit_numbers_list);
        saveBtn = (Button) fragmentView.findViewById(R.id.ticket_edit_save);
        saveBtn.setOnClickListener(this);
        newLotteryNumberBtn = (ImageButton) fragmentView.findViewById(R.id.ticket_edit_new_number);
        newLotteryNumberBtn.setOnClickListener(this);

        updateUi();
    }

    private void updateUi() {
        ownerInput.setText(model.getOwner());

        double totalPrice = 0.0;
        TreeMap<Object, LotteryNumber> lotteryNumbers = model.getLotteryNumbers();
        for (Object key : lotteryNumbers.keySet()) {
            totalPrice += lotteryNumbers.get(key).getPrice();
        }
        for (LotteryNumber unsavedNumber : model.getUnsavedLotteryNumbers()) {
            totalPrice += unsavedNumber.getPrice();
        }
        priceLabel.setText(String.format("%.2f credits", totalPrice));

        // Set list of lottery numbers
        final ArrayList<LotteryNumberListViewModel> viewModels = convertToViewModels(model.getLotteryNumbers(), model.getUnsavedLotteryNumbers());
        if (viewModels.size() > 0) {
            numberList.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, viewModels));
            numberList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
                    menu.setHeaderTitle(String.format("%d - %s", viewModels.get(info.position).getLotteryNumber(), getString(R.string.lottery_number)));
                    // Delete option
                    menu.add(Menu.NONE, 0, 0, R.string.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getContext());
                            dlgBuilder
                                    .setTitle(R.string.attention)
                                    .setMessage(R.string.delete_lottery_number_confirm)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            LotteryNumberListViewModel viewModel = viewModels.get(info.position);
                                            if (viewModel.getId() == null) {
                                                LinkedList<LotteryNumber> unsavedNumbers = model.getUnsavedLotteryNumbers();
                                                for (int i = 0; i < unsavedNumbers.size(); ++i) {
                                                    if (unsavedNumbers.get(i).getLotteryNumber() == viewModel.getLotteryNumber()) {
                                                        unsavedNumbers.remove(i);
                                                        ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
                                                    }
                                                }
                                            } else {
                                                ApplicationDomain.getInstance().lotteryNumberRepository.deleteLotteryNumber(viewModel.getEntityModel());
                                            }
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
        } else {
            ArrayList<String> fillerList = new ArrayList<>();
            fillerList.add("");
            numberList.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, fillerList){
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);
                    TextView title = (TextView) itemView.findViewById(android.R.id.text1);
                    title.setText(R.string.none_found);
                    return itemView;
                }
            });
        }
    }

    private ArrayList<LotteryNumberListViewModel> convertToViewModels(TreeMap<Object, LotteryNumber> lotteryNumbers, LinkedList<LotteryNumber> unsavedNumbers) {
        ArrayList<LotteryNumberListViewModel> viewModels = new ArrayList<>();
        for (Object key : lotteryNumbers.keySet()) {
            viewModels.add(new LotteryNumberListViewModel(lotteryNumbers.get(key)));
        }

        for (LotteryNumber unsavedNumber : unsavedNumbers) {
            viewModels.add(new LotteryNumberListViewModel(unsavedNumber));
        }

        return viewModels;
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.TICKET_LIST_UPDATE || arg == DataAccessEvent.LOTTERY_NUMBER_UPDATE) {
                updateUi();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {
            // Don't create ticket without an owner
            String ownerName = ownerInput.getText().toString();
            if (ownerName == null ||  ownerName.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.save_failed)
                        .setMessage(R.string.ticket_edit_owner_required);
                        //.setPositiveButton(R.string.ok, );
                builder.create().show();
                return;
            }

            model.setOwner(ownerName);
            model = ApplicationDomain.getInstance().ticketRepository.saveTicket(model);

            // Go back
            getFragmentManager().popBackStack();

        } else if (v == newLotteryNumberBtn) {
            // Open dialog to enter new lottery number
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            Bundle args = new Bundle();
            args.putString("ticketId", (String) model.getId());
            DialogFragment newLotteryNumDlg = new NewLotteryNumDlg();
            newLotteryNumDlg.setArguments(args);
            newLotteryNumDlg.show(ft, "dialog");
        }
    }
}