package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.TreeMap;


/**
 * Created by Stensig on 13-Oct-16.
 */

public class TicketListViewModel {
    private Ticket entityModel;

    public TicketListViewModel(Ticket entityModel) {
        this.entityModel = entityModel;
    }

    public Object getId() {
        return entityModel.getId();
    }

    public String getOwner() {
        return entityModel.getOwner();
    }

    public ArrayList<Integer> getLotteryNumbers() {
        ArrayList<Integer> result = new ArrayList<>();
        TreeMap<Object, LotteryNumber> lotteryNumbers = entityModel.getLotteryNumbers();
        for (Object key : lotteryNumbers.keySet()) {
            result.add(lotteryNumbers.get(key).getLotteryNumber());
        }
        return result;
    }

    public Ticket getEntityModel() { return entityModel; }

    @Override
    public String toString() {
        return entityModel.getOwner();
    }
}
