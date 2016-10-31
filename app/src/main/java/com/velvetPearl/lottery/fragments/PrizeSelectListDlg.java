package com.velvetPearl.lottery.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.AppLaunchChecker;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

/**
 * Created by Stensig on 31-Oct-16.
 */

public class PrizeSelectListDlg extends DialogFragment implements Observer {

    private ListView prizeList;
    private TextView title;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_prize_select_list_dlg, container, false);
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
        prizeList = (ListView) root.findViewById(R.id.list_container);
        title = (TextView) root.findViewById(R.id.list_title);
        title.setText(R.string.select_prize_to_win);
    }

    private void updateUi() {
        final ArrayList<Prize> prizesEligibleForWin = getAvailablePrizes();

        prizeList.setAdapter(new ArrayAdapter(getContext(), R.layout.listitem_prize, R.id.list_item_prize_name, prizesEligibleForWin){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView prizeName = (TextView) view.findViewById(R.id.list_item_prize_name);
                prizeName.setText(prizesEligibleForWin.get(position).getName());
                return view;
            }
        });
        prizeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Show confirmation dialog
                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getContext());
                dlgBuilder.setMessage(String.format(getString(R.string.prize_selection_confirmation), prizesEligibleForWin.get(position).getName()))
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ApplicationDomain.getInstance().setPrizeToBeWon(prizesEligibleForWin.get(position));
                                drawWinner();
                                getFragmentManager().popBackStack();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                dlgBuilder.create().show();
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.PRIZE_UPDATE) {
                updateUi();
            }
        }
    }

    private ArrayList<Prize> getAvailablePrizes() {
        ArrayList<Prize> eligiblePrizes = new ArrayList<>();

        TreeMap<Object, Prize> allPrizes = ApplicationDomain.getInstance().getActiveLottery().getPrizes();
        for (Object prizeId : allPrizes.keySet()) {
            Prize prize = allPrizes.get(prizeId);
            if (prize.getNumberId() == null) {
                eligiblePrizes.add(prize);
            }
        }

        return eligiblePrizes;
    }

    private void drawWinner() {
        // Get all lottery numbers without a prize
        ArrayList<LotteryNumber> numbersEligibleForWin = new ArrayList<>();
        TreeMap<Object, Ticket> tickets = ApplicationDomain.getInstance().getActiveLottery().getTickets();
        for (Object ticketId : tickets.keySet()) {
            Ticket ticket = tickets.get(ticketId);
            for (LotteryNumber number : ticket.getLotteryNumbers()) {
                if (number.getWinningPrize() == null) {
                    numbersEligibleForWin.add(number);
                }
            }
        }

        // Check for all numbers having won
        if (numbersEligibleForWin.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_winless_numbers, Toast.LENGTH_LONG).show();
            return;
        }

        // Draw random number and save the ID in the prize
        int numberSelector = (int) (Math.random() * numbersEligibleForWin.size());
        Prize prizeToBeWon = ApplicationDomain.getInstance().getPrizeToBeWon();
        prizeToBeWon.setNumberId(numbersEligibleForWin.get(numberSelector).getId());

        ApplicationDomain.getInstance().prizeRepository.savePrize(prizeToBeWon);
        ApplicationDomain.getInstance().setPrizeToBeWon(null);
    }
}
