package com.velvetPearl.lottery.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.AppLaunchChecker;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
//import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Stensig on 02-Nov-16.
 */

public class NewLottery extends Fragment implements View.OnClickListener, View.OnKeyListener, Observer {

    private static final String LOG_TAG = "NewLotteryFragment";

    private EditText numRangeMin;
    private EditText numRangeMax;
    private EditText numPrice;
    private Button cancelBtn;
    private Button createBtn;
    private EditText nameInput;
    private TextView errorLabel;
    private CheckBox allowMultiWinOnTicket;

    private AlertDialog savingDlg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_lottery, container, false);
        ApplicationDomain.getInstance().addObserver(this);
        initUi(root);

        return root;
    }

    @Override
    public void onDestroyView() {
        ApplicationDomain.getInstance().deleteObserver(this);
        super.onDestroyView();
    }

    private void initUi(View root) {
        MainActivity activity = (MainActivity) getActivity();
        activity.disableActiveLotteryMenuItems();
        activity.setTitle(R.string.new_lottery);

        numRangeMin = (EditText) root.findViewById(R.id.new_lottery_min_num);
        numRangeMax = (EditText) root.findViewById(R.id.new_lottery_max_num);
        numPrice = (EditText) root.findViewById(R.id.new_lottery_price_per_num);
        cancelBtn = (Button) root.findViewById(R.id.new_lottery_cancel);
        createBtn = (Button) root.findViewById(R.id.new_lottery_create);
        nameInput = (EditText) root.findViewById(R.id.new_lottery_name);
        allowMultiWinOnTicket = (CheckBox) root.findViewById(R.id.multi_winner_checkbox);
        errorLabel = (TextView) root.findViewById(R.id.new_lottery_error_lab);
        errorLabel.setText(null);

        cancelBtn.setOnClickListener(this);
        createBtn.setOnClickListener(this);

        numRangeMin.setOnKeyListener(this);
        numRangeMax.setOnKeyListener(this);
        nameInput.setOnKeyListener(this);
        numPrice.setOnKeyListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == cancelBtn) {
            FragmentManager fragMan = getFragmentManager();
            if (fragMan.getBackStackEntryCount() > 0) {
                fragMan.popBackStack();
            } else {
                fragMan.beginTransaction().replace(R.id.main_fragment_container, new Welcome()).commit();
            }
        } else if (view == createBtn) {
            parseInputDataToNewLottery();
        }
    }

    private void parseInputDataToNewLottery() {
        String name = nameInput.getText().toString();
        if (name.isEmpty()) {
            errorLabel.setText(R.string.new_lottery_error_missing_name);
            return;
        }

        int numMin;
        try {
            numMin = Integer.parseInt(numRangeMin.getText().toString());
        } catch (Exception ex) {
            errorLabel.setText(R.string.new_lottery_error_min_not_int);
            return;
        }
        if (numMin < 1) {
            errorLabel.setText(R.string.new_lottery_error_min_negative);
            return;
        }

        int numMax;
        try {
            numMax = Integer.parseInt(numRangeMax.getText().toString());
        } catch (Exception ex) {
            errorLabel.setText(R.string.new_lottery_error_max_not_int);
            return;
        }
        if (numMax <= numMin) {
            errorLabel.setText(R.string.new_lottery_error_max_small);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(numPrice.getText().toString());
        } catch (Exception ex) {
            errorLabel.setText(R.string.new_lottery_error_price_not_double);
            return;
        }
        if (price < 0.0) {
            errorLabel.setText(R.string.new_lottery_error_price_not_double);
            return;
        }

        Log.d(LOG_TAG, "creating new lottery");
        Lottery lottery = new Lottery();
        lottery.setCreated(System.currentTimeMillis());
        lottery.setName(name);
        lottery.setLotteryNumLowerBound(numMin);
        lottery.setLotteryNumUpperBound(numMax);
        lottery.setPricePerLotteryNum(price);
        lottery.setTicketMultiWinEnabled(allowMultiWinOnTicket.isChecked());
        lottery = ApplicationDomain.getInstance().lotteryRepository.saveLottery(lottery);
        ApplicationDomain.getInstance().lotteryRepository.loadLottery(lottery.getId());

        final Lottery finalLottery = lottery;
        savingDlg = ProgressDialog.show(getContext(), getString(R.string.please_wait), getString(R.string.create_lottery), true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d(LOG_TAG, String.format("Cancelling lottery creation. Calling delete on ID %s.", (String) finalLottery.getId()));
                ApplicationDomain.getInstance().lotteryRepository.deleteLottery(finalLottery);
            }
        });

    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    parseInputDataToNewLottery();
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public void update(Observable observable, Object args) {
        if (args != null && args.getClass() == DataAccessEvent.class) {
            if (args == DataAccessEvent.LOTTERY_LOADED){
                if (savingDlg.isShowing()) {
                    savingDlg.dismiss();
                }
                getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LotteryHome()).addToBackStack(null).commit();
            }
        }
    }
}
