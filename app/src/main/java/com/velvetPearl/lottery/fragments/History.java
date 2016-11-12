package com.velvetPearl.lottery.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.viewModels.LotteryListViewModel;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Fragment containing a list view of the lottery history.
 */
public class History extends Fragment implements Observer {

    private static final String LOG_TAG = "HistoryFragment";
    private ProgressDialog loadingDlg;
    private ListView historyListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_history, container, false);
        ApplicationDomain.getInstance().addObserver(this);

        historyListView = (ListView) fragView.findViewById(R.id.history_lottery_listview);
        getActivity().setTitle(R.string.lottery_history);

        initLoadingDialog();
        loadingDlg.show();
        loadHistory();

        ((MainActivity) getActivity()).disableActiveLotteryMenuItems();

        return fragView;
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "unsubscribing from model updates");
        ApplicationDomain.getInstance().deleteObserver(this);
        super.onDestroyView();
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
                getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Welcome()).commit();
            }
        });

    }

    /**
     *
     */
    private void loadHistory() {
        ApplicationDomain.getInstance().lotteryRepository.loadAllLotteries();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.getClass() != DataAccessEvent.class || arg != DataAccessEvent.LOTTERY_LIST_UPDATED) {
            return;
        }

        ArrayList<Lottery> lotteries = ApplicationDomain.getInstance().getAllLotteries();
        if (lotteries.size() > 0) {

            final ArrayList<LotteryListViewModel> viewModels = new ArrayList<>();
            for (Lottery entity : lotteries) {
                viewModels.add(new LotteryListViewModel(entity));
            }

            historyListView.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_2, android.R.id.text1, viewModels){
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View root = super.getView(position, convertView, parent);

                    TextView heading = (TextView) root.findViewById(android.R.id.text1);
                    TextView subheading = (TextView) root.findViewById(android.R.id.text2);

                    LotteryListViewModel model = viewModels.get(position);
                    heading.setText(model.getName());
                    subheading.setText(model.getCreatedFormated());

                    return root;
                }
            });

            // Add click listener for navigating to Lottery Home fragment on item click
            historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    LotteryListViewModel item = (LotteryListViewModel) historyListView.getItemAtPosition(i);
                    Bundle args = new Bundle();
                    args.putString("lotteryId", (String) item.getId());
                    Fragment destination = new LotteryHome();
                    destination.setArguments(args);
                    ApplicationDomain.getInstance().clearActiveLottery();
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getFragmentManager().beginTransaction().replace(R.id.main_fragment_container,destination).addToBackStack(null).commit();
                }
            });

        } else {
            ArrayList<String> tempList = new ArrayList<>();
            tempList.add(getString(R.string.history_no_lotteries));
            historyListView.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, tempList));
        }

        if (loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
    }
}
