package com.velvetPearl.lottery.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.models.Prize;

/**
 * Created by Stensig on 30-Oct-16.
 */

public class PrizeInputDlg extends DialogFragment implements View.OnClickListener {

    private static final String LOG_TAG = "PrizeInputDlg";

    private EditText prizeNameInput;
    private TextView dialogTitle;
    private Button saveBtn;
    private Button cancelBtn;
    private TextView errorLabel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_prize_input_dlg, container);

        initUi(root);
        updateUi();

        return root;
    }

    private void updateUi() {
        prizeNameInput.setText(ApplicationDomain.getInstance().getEditingPrize().getName());
        errorLabel.setText(null);
    }

    private void initUi(View root) {
        prizeNameInput = (EditText) root.findViewById(R.id.prize_input_dlg_name);
        dialogTitle = (TextView) root.findViewById(R.id.prize_input_dlg_title);
        saveBtn = (Button) root.findViewById(R.id.prize_input_dlg_save_btn);
        cancelBtn = (Button) root.findViewById(R.id.prize_input_dlg_cancel_btn);
        errorLabel = (TextView) root.findViewById(R.id.prize_input_dlg_error_lab);

        if (ApplicationDomain.getInstance().getEditingPrize().getId() != null) {
            dialogTitle.setText(R.string.edit_ptize);
            getDialog().setTitle(R.string.edit_ptize);
        } else {
            dialogTitle.setText(R.string.new_prize);
            getDialog().setTitle(R.string.new_prize);
        }

        saveBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {
            String name = prizeNameInput.getText().toString();
            if (name == null || name.isEmpty()) {
                errorLabel.setText(R.string.prize_error_name_required);
                return;
            }
            Prize prize = ApplicationDomain.getInstance().getEditingPrize();
            prize.setName(name);
            ApplicationDomain.getInstance().prizeRepository.savePrize(prize);

            getFragmentManager().popBackStack();
        } else if (v == cancelBtn) {
            ApplicationDomain.getInstance().resetEditingPrize();
            getFragmentManager().popBackStack();
        }
    }
}
