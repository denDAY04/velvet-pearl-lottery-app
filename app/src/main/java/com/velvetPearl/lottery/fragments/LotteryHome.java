package com.velvetPearl.lottery.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.ApplicationDomain;
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
    private TextView lotteryNameLab = null;
    private TextView timestampLab = null;
    private TextView lotteryNumRangeLab = null;
    private TextView pricePerNumLab = null;
    private TextView lotteryNumSoldCount = null;
    private Button ticketsBtn = null;
    private Button winnersBtn = null;
    private Button prizesBtn = null;
    private CheckBox allowMultiWinOnTicket = null;
    private MenuItem deleteMenu = null;

    private ProgressDialog loadingDialog = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lottery_home, container, false);
        setHasOptionsMenu(true);

        ApplicationDomain.getInstance().addObserver(this);

        if (savedInstanceState == null && ApplicationDomain.getInstance().getActiveLottery() == null) {
            Bundle args = getArguments();
            String lotteryId = args.getString("lotteryId");
            Log.d(LOG_TAG, "loading ID " + lotteryId);

            initLoadingDialog();
            initUi(root);
            loadingDialog.show();

            ApplicationDomain.getInstance().lotteryNumberRepository.clearState();
            ApplicationDomain.getInstance().lotteryRepository.loadLottery(lotteryId);

        } else {
            initLoadingDialog();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.lottery_home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Initialize the UI references and set basic handlers.
     * @param view The view on which to collect the UI references.
     */
    private void initUi(View view) {
        getActivity().setTitle(R.string.lottery);
        ((MainActivity) getActivity()).enableActiveLotteryMenuItems();

        lotteryNameLab = (TextView) view.findViewById(R.id.lotteryhome_name);
        timestampLab = (TextView) view.findViewById(R.id.lotteryhome_timestamp);
        lotteryNumRangeLab = (TextView) view.findViewById(R.id.lotteryhome_num_range_var);
        pricePerNumLab = (TextView) view.findViewById(R.id.lotteryhome_price_per_number_var);
        lotteryNumSoldCount = (TextView) view.findViewById(R.id.lotteryhome_numbers_sold_var);
        ticketsBtn = (Button) view.findViewById(R.id.lotteryhome_tickets_btn);
        winnersBtn = (Button) view.findViewById(R.id.lotteryhome_winners_btn);
        prizesBtn = (Button) view.findViewById(R.id.lotteryhome_prizes_btn);
        allowMultiWinOnTicket = (CheckBox) view.findViewById(R.id.multi_winner_checkbox);

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
                        Log.d(LOG_TAG, "canceling lottery fetch");
                        getFragmentManager().popBackStack();
                        loadingDialog.dismiss();
                    }
                });
    }

    public void updateUi() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();

        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        lotteryNameLab.setText(lottery.getName());
        Locale locale = Locale.getDefault();
        String timestamp = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(lottery.getCreated()));
        timestampLab.setText(String.format(locale, "%s %s", getString(R.string.created), timestamp));
        lotteryNumRangeLab.setText(String.format(locale, "%d - %d", lottery.getLotteryNumLowerBound(), lottery.getLotteryNumUpperBound()));
        pricePerNumLab.setText(String.format(locale, "%.2f", lottery.getPricePerLotteryNum()));
        allowMultiWinOnTicket.setChecked(lottery.isTicketMultiWinEnabled());

        int count = 0;
        if (lottery.getTickets() != null) {
            for (Object key : lottery.getTickets().keySet()) {
                Ticket ticket = lottery.getTickets().get(key);
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
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Winners()).addToBackStack(null).commit();
        } else if (v == prizesBtn) {
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Prizes()).addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_lottery_home_delete) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
            confirmDialog
                    .setTitle(R.string.attention)
                    .setMessage(R.string.delete_lottery_confirm)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ApplicationDomain.getInstance().lotteryRepository.deleteLottery(ApplicationDomain.getInstance().getActiveLottery());
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            confirmDialog.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.getClass() == DataAccessEvent.class) {
            if (arg == DataAccessEvent.LOTTERY_REMOVED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
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
            } else {
                updateUi();
            }
        }
    }
}
