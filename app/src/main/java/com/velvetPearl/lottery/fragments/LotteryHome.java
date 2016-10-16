package com.velvetPearl.lottery.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;


public class LotteryHome extends Fragment implements View.OnClickListener, Observer {

    private static final String LOG_TAG = "LotteryHome";

    // UI refs
    private TextView timestampLab = null;
    private TextView lotteryNumRangeLab = null;
    private TextView pricePerNumLab = null;
    private TextView lotteryNumSoldCount = null;
    private Button ticketsBtn = null;
    private Button winnersBtn = null;
    private Button prizesBtn = null;

    private ProgressDialog loadingDialog = null;
    private boolean initialLoad = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lottery_home, container, false);

        ApplicationDomain.getInstance().addObserver(this);

        if (savedInstanceState == null && ApplicationDomain.getInstance().getActiveLottery() == null) {
            Bundle args = getArguments();
            String lotteryId = args.getString("lotteryId");
            Log.d(LOG_TAG, "loading ID " + lotteryId);

            initLoadingDialog();
            initUi(root);
            loadingDialog.show();

            ApplicationDomain.getInstance().lotteryNumberRepository.clearState();
            ApplicationDomain.getInstance().lotteryRepository.getLottery(lotteryId);

        } else {
            initUi(root);
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

    /**
     * Initialize the UI references and set basic handlers.
     * @param view The view on which to collect the UI references.
     */
    private void initUi(View view) {
        timestampLab = (TextView) view.findViewById(R.id.lotteryhome_timestamp);
        lotteryNumRangeLab = (TextView) view.findViewById(R.id.lotteryhome_num_range_var);
        pricePerNumLab = (TextView) view.findViewById(R.id.lotteryhome_price_per_number_var);
        lotteryNumSoldCount = (TextView) view.findViewById(R.id.lotteryhome_numbers_sold_var);
        ticketsBtn = (Button) view.findViewById(R.id.lotteryhome_tickets_btn);
        winnersBtn = (Button) view.findViewById(R.id.lotteryhome_winners_btn);
        prizesBtn = (Button) view.findViewById(R.id.lotteryhome_prizes_btn);

        ticketsBtn.setOnClickListener(this);
        winnersBtn.setOnClickListener(this);
        prizesBtn.setOnClickListener(this);
    }

    /**
     * Initialize the loading dialog, without showing it.
     */
    private void initLoadingDialog() {
        loadingDialog = ProgressDialog.show(getContext(),
                null,       /* timestampLab */
                getString(R.string.lotteryhome_loading_lottery),
                false,      /* indeterminate */
                true,       /* cancelable */
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        loadingDialog.dismiss();
                        Log.d(LOG_TAG, "canceling lottery fetch");
                    }
                });
    }

    public void updateUi() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();
        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        Locale locale = Locale.getDefault();
        String timestamp = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(lottery.getCreated()));
        timestampLab.setText(String.format(locale, "%s", timestamp));
        lotteryNumRangeLab.setText(String.format(locale, "%d - %d", lottery.getLotteryNumLowerBound(), lottery.getLotteryNumUpperBound()));
        pricePerNumLab.setText(String.format(locale, "%.2f", lottery.getPricePerLotteryNum()));
        int count = 0;
        if (lottery.getTickets() != null) {
            for (Ticket ticket : lottery.getTickets()) {
                count += ticket.getLotteryNumbers().size();
            }
        }
        lotteryNumSoldCount.setText(Integer.toString(count));
    }


    @Override
    public void onClick(View v) {
        if (v == ticketsBtn) {
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Tickets()).addToBackStack(null).commit();
        } else if (v == winnersBtn) {
            Log.d(LOG_TAG, "NOT IMPLEMENTED");
        } else if (v == prizesBtn) {
            Log.d(LOG_TAG, "NOT IMPLEMENTED");
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.LOTTERY_LOADED) {
                ApplicationDomain.getInstance().ticketRepository.getTicketsForLottery(ApplicationDomain.getInstance().getActiveLottery().getId());
                return;
            }

            if (arg == DataAccessEvent.TICKET_LIST_UPDATED) {
                if (initialLoad) {
                    Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                    for (Ticket ticket : lottery.getTickets()) {
                        ApplicationDomain.getInstance().lotteryNumberRepository.getLotteryNumbersForTicket(ticket.getId());
                    }
                    initialLoad = false;
                }
                updateUi();
            } else if (arg == DataAccessEvent.LOTTERY_UPDATED) {
                updateUi();
            } else if (arg == DataAccessEvent.LOTTERY_REMOVED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.attention))
                        .setMessage(getString(R.string.lottery_was_deleted))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);  // Empty back stack
                                getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Welcome()).commit();
                            }
                        });
                 builder.create().show();
            }
        }
    }
}
