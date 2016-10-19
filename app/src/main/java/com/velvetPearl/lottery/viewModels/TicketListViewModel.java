package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;


/**
 * Created by Stensig on 13-Oct-16.
 */

public class TicketListViewModel {
    private Object id;
    private String owner;
    private ArrayList<Integer> lotteryNumbers;

    public TicketListViewModel(Ticket entityModel) {
        id = entityModel.getId();
        owner = entityModel.getOwner();
        lotteryNumbers = new ArrayList<>();

        for (Object key : entityModel.getLotteryNumbers().keySet()) {
            LotteryNumber number = entityModel.getLotteryNumbers().get(key);
            lotteryNumbers.add(number.getLotteryNumber());
        }
    }

    public Object getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public ArrayList<Integer> getLotteryNumbers() {
        return lotteryNumbers;
    }

    @Override
    public String toString() {
        return owner;
    }
}
