package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.io.File;

/**
 * Created by Stensig on 19-Nov-16.
 */

public class PrintDlg extends DialogFragment {

    private File outputFile;
    private Lottery lottery;

    // UI elements
    private Button cancelBtn;
    private Switch storageDeviceSwitch;
    private EditText saveAsInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_print_dlg, container, false);

        lottery = ApplicationDomain.getInstance().getActiveLottery();
        initUI(root);

        return root;
    }

    private void initUI(View root) {
        cancelBtn = (Button) root.findViewById(R.id.print_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        storageDeviceSwitch = (Switch) root.findViewById(R.id.print_storage_device);
        storageDeviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    storageDeviceSwitch.setText(R.string.external);
                } else {
                    storageDeviceSwitch.setText(R.string.internal);
                }
            }
        });

        saveAsInput = (EditText) root.findViewById(R.id.print_save_as);
        saveAsInput.setText(lottery.getName());
    }
}
