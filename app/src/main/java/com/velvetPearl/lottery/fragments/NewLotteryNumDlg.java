package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by Stensig on 27-Oct-16.
 */

public class NewLotteryNumDlg extends DialogFragment implements View.OnClickListener {

    // UI elements
    private Button randomBtn;
    private Button specificNumBtn;
    private EditText numberInput;
    private TextView errorLabel;

    private String ticketId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_lottery_num_dlg, container, false);

        initUi(root);
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            ticketId = args.getString("ticketId");
        }

        return root;
    }

    private void initUi(View rootView) {
        randomBtn = (Button) rootView.findViewById(R.id.lottery_num_random_btn);
        specificNumBtn = (Button) rootView.findViewById(R.id.lottery_num_specific_btn);
        numberInput = (EditText) rootView.findViewById(R.id.lottery_num_input);
        errorLabel = (TextView) rootView.findViewById(R.id.lottery_num_error_lab);

        randomBtn.setOnClickListener(this);
        specificNumBtn.setOnClickListener(this);

        errorLabel.setText(null);
    }

    private LinkedList<Integer> getUsedLotteryNumbers() {
        LinkedList<Integer> usedNumbers = new LinkedList<>();

        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        if (lottery != null) {
            TreeMap<Object, Ticket> tickets = lottery.getTickets();
            for (Object ticketKey : tickets.keySet()) {
                Ticket ticket = tickets.get(ticketKey);
                TreeMap<Object, LotteryNumber> numbers =  ticket.getLotteryNumbers();
                for (Object numberKey : numbers.keySet()) {
                    usedNumbers.add(numbers.get(numberKey).getLotteryNumber());
                }
            }
        }

        if (ticketId != null) {
            Ticket thisTicket = lottery.getTickets().get(ticketId);
            for (LotteryNumber unsavedNumber : thisTicket.getUnsavedLotteryNumbers()) {
                usedNumbers.add(unsavedNumber.getLotteryNumber());
            }
        }

        return usedNumbers;
    }

    @Override
    public void onClick(View v) {
        if (v == randomBtn) {

            int upperBound = ApplicationDomain.getInstance().getActiveLottery().getLotteryNumUpperBound();
            int lowerBound = ApplicationDomain.getInstance().getActiveLottery().getLotteryNumLowerBound();
            LinkedList<Integer> usedNumbers = getUsedLotteryNumbers();
            int randomNumber;

            do {
                randomNumber = (int) (Math.random() * upperBound);
                if (randomNumber < lowerBound) {
                    continue;
                }
            } while (usedNumbers.contains(randomNumber));

            storeLotteryNumber(randomNumber);
            getFragmentManager().popBackStack();
        } else if (v == specificNumBtn) {

            String input = numberInput.getText().toString();
            int userNumber;
            try {
                userNumber = Integer.parseInt(input);
            } catch (Exception e) {
                errorLabel.setText(R.string.lottery_num_error_format);
                return;
            }

            Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
            if (userNumber < lottery.getLotteryNumLowerBound() || userNumber > lottery.getLotteryNumUpperBound()) {
                errorLabel.setText(String.format(getString(R.string.lottery_num_error_range),lottery.getLotteryNumLowerBound(), lottery.getLotteryNumUpperBound()));
                return;
            }

            if (getUsedLotteryNumbers().contains(userNumber)){
                errorLabel.setText(R.string.lottery_num_error_unavailable);
                return;
            }

            storeLotteryNumber(userNumber);
            getFragmentManager().popBackStack();
        }
    }

    private void storeLotteryNumber(int number) {
        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        LotteryNumber lotteryNumber = new LotteryNumber();
        lotteryNumber.setTicketId(ticketId);
        lotteryNumber.setLotteryNumber(number);
        lotteryNumber.setPrice(lottery.getPricePerLotteryNum());

        ApplicationDomain.getInstance().getEditingTicket().getLotteryNumbers().add(lotteryNumber);
    }
}
