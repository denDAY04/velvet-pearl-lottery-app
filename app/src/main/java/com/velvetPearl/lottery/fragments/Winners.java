package com.velvetPearl.lottery.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
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

            winnersListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    menu.setHeaderTitle(R.string.winner);
                    // Delete option
                    menu.add(Menu.NONE, 0, 0, R.string.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getContext());
                            dlgBuilder
                                    .setTitle(R.string.attention)
                                    .setMessage(R.string.delete_winner_confirm)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            WinnerListViewModel model = viewModels.get(info.position);
                                            model.getPrize().setNumberId(null);
                                            ApplicationDomain.getInstance().prizeRepository.savePrize(model.getPrize());
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

            winnersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ApplicationDomain.getInstance().setEditingTicketFromLottery((String) viewModels.get(position).getTicketId());
                    getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new TicketEdit()).addToBackStack(null).commit();
                }
            });
        } else {
            String[] tempList = new String[] {getString(R.string.none_found)};
            winnersListView.setAdapter(new ArrayAdapter(getActivity(), R.layout.listitem_prize, R.id.list_item_prize_name, tempList));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == drawWinnerBtn) {

            // Check that there are prizes yet to be won
            boolean eligiblePrizes = false;
            TreeMap<Object, Prize> prizes = ApplicationDomain.getInstance().getActiveLottery().getPrizes();
            for (Object prizeId : prizes.keySet()) {
                Prize prize = prizes.get(prizeId);
                if (prize.getNumberId() == null) {
                    eligiblePrizes = true;
                    break;
                }
            }

            // Check for all prizes having been assigned
            if (!eligiblePrizes) {
                Toast.makeText(getContext(), R.string.no_prizes_left, Toast.LENGTH_LONG).show();
                return;
            }

            // Open dialog to enter new lottery number
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment prizeSelectDlg = new PrizeSelectListDlg();
            prizeSelectDlg.show(ft, "dialog");
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.PRIZE_UPDATE || arg == DataAccessEvent.TICKET_LIST_UPDATE) {
                updateUi();
            }
        }
    }
}
