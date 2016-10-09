package com.velvetPearl.lottery.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velvetPearl.lottery.IEntityUiUpdater;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.LotterySingleton;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeoutException;


public class LotteryHome extends Fragment implements IEntityUiUpdater {

    private static final String LOG_TAG = "LotteryHome";

    // UI refs
    private TextView timestampLab = null;
    private TextView lotteryNumRangeLab = null;
    private TextView pricePerNumLab = null;
    private TextView lotteryNumSoldCount = null;

    private ProgressDialog loadingDialog = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_lottery_home, container, false);

        Bundle args = getArguments();
        String lotteryId = args.getString("lotteryId");
        Log.d(LOG_TAG, "loading ID " + lotteryId);

        initLoadingDialog();
        initUi(fragView);
        loadingDialog.show();
        loadLotteryAsync(lotteryId);

        return fragView;
    }

    /**
     * Initialize the UI references.
     * @param view The view on which to collect the UI references.
     */
    private void initUi(View view) {
        timestampLab = (TextView) view.findViewById(R.id.lotteryhome_timestamp);
        lotteryNumRangeLab = (TextView) view.findViewById(R.id.lotteryhome_num_range_var);
        pricePerNumLab = (TextView) view.findViewById(R.id.lotteryhome_price_per_number_var);
        lotteryNumSoldCount = (TextView) view.findViewById(R.id.lotteryhome_numbers_sold_var);
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

    @Override
    public void updateUi() {
        Log.d(LOG_TAG, "update ui!");

        // TODO populate UI with
        Lottery lottery = LotterySingleton.getActiveLottery();
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


    /**
     * Load the full lottery for the specific lottery ID and update the UI when the data has been
     * loaded.
     * @param lotteryId ID of the lottery to load.
     */
    private void loadLotteryAsync(final String lotteryId) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    // TODO: load all children data of the lottery
                    return LotterySingleton.getInstance().getLottery(lotteryId, LotteryHome.this);
                } catch (TimeoutException e) {
                    Log.w(LOG_TAG, "fetching lottery data failed", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                loadingDialog.dismiss();
                if (result == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder
                            .setTitle(R.string.error)
                            .setMessage(R.string.lotteryhome_error_timeout)
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    loadingDialog.show();
                                    loadLotteryAsync(lotteryId);
                                }
                            })
                            .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Back to welcome - clearing the stack at the same time
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
                                    fragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    fragmentManager.beginTransaction().replace(R.id.main_fragment, new Welcome()).commit();
                                }
                            });
                    loadingDialog.dismiss();
                    builder.create().show();
                }
            }
        }.execute();
    }
}
