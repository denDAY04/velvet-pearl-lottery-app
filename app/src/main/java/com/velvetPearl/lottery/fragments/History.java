package com.velvetPearl.lottery.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.LotterySingleton;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.viewModels.LotteryListViewModel;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Fragment containing a list view of the lottery history.
 */
public class History extends Fragment {

    private static final String LOG_TAG = "HistoryFragment";
    private ProgressDialog loadingDlg;
    private ListView historyListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_history, container, false);

        historyListView = (ListView) fragView.findViewById(R.id.history_lottery_listview);

        initLoadingDialog();
        loadingDlg.show();
        loadHistoryAsync();

        return fragView;
    }

    /**
     * Initialize the progress dialog for when loading the history data.
     * This method does not show the dialog.
     */
    public void initLoadingDialog() {
        loadingDlg = ProgressDialog.show(getContext(), null, getString(R.string.history_loading_history), true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Switch back to home on cancel.
                Log.d(LOG_TAG, "progressDialog:onCancelListener canceling history data load");
                getFragmentManager().beginTransaction().replace(R.id.main_fragment, new Welcome()).commit();
            }
        });
    }

    /**
     * Load the history data asynchronously and display it.
     * This method dismisses the progress dialog when the loading has completed or failed. Upon
     * failure it displays an alert enabling the user to either retry the loading or return to
     * the welcome screen. Upon success the history is loaded into a list view.
     */
    private void loadHistoryAsync() {
        new AsyncTask() {
            @Override
            public void onPostExecute(final Object result) {
                if (result == null) {

                    // Error occurred, so show dialog with option to retry or go back to home.
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getContext());
                    dlgBuilder
                            .setTitle(getString(R.string.error))
                            .setMessage(getString(R.string.history_error_loading_history))
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    loadingDlg.show();
                                    loadHistoryAsync();
                                }
                            })
                            .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Back to welcome - clearing the stack at the same time
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
                                    fragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    fragmentManager.beginTransaction().replace(R.id.main_fragment, new Welcome()).commit();
                                }
                            });
                    AlertDialog dlg = dlgBuilder.create();
                    loadingDlg.dismiss();
                    dlg.show();

                } else {

                    ArrayList<Lottery> lotteries = (ArrayList<Lottery>) result;
                    if (lotteries.size() > 0) {

                        ArrayList<LotteryListViewModel> viewModels = new ArrayList<>();
                        for (Lottery entity : lotteries) {
                            viewModels.add(new LotteryListViewModel(entity));
                        }

                        historyListView.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, viewModels));

                        // Add click listener for navigating to Lottery Home fragment on item click
                        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                LotteryListViewModel item = (LotteryListViewModel) historyListView.getItemAtPosition(i);
                                Bundle args = new Bundle();
                                args.putString("lotteryId", (String) item.getId());
                                Fragment destination = new LotteryHome();
                                destination.setArguments(args);
                                getFragmentManager().beginTransaction().replace(R.id.main_fragment,destination).addToBackStack(null).commit();
                            }
                        });

                    } else {
                        ArrayList<String> tempList = new ArrayList<>();
                        tempList.add(getString(R.string.history_no_lotteries));
                        historyListView.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, tempList));
                    }

                    loadingDlg.dismiss();
                }
            }


            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    return LotterySingleton.getInstance().getAllLotteries();
                } catch (TimeoutException e) {
                    Log.w(LOG_TAG, "loadHistoryAsync:doInBackground data load failed", e);
                }
                return null;
            }
        }.execute();
    }
}
