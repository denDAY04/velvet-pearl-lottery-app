package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;

/**
 * Created by Stensig on 20-Oct-16.
 */

public class LotteryNumberListViewModel {
    private int lotteryNumber;
    private Object id;

    public LotteryNumberListViewModel(LotteryNumber lotteryNumber) {
        this.lotteryNumber = lotteryNumber.getLotteryNumber();
        this.id = lotteryNumber.getId();
    }

    @Override
    public String toString() {
        return Integer.toString(lotteryNumber);
    }

    public int getLotteryNumber() {return lotteryNumber;}

    public Object getId() {
        return id;
    }
}
