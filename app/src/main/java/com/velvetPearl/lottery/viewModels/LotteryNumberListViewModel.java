package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;

/**
 * Created by Stensig on 20-Oct-16.
 */

public class LotteryNumberListViewModel {
    private LotteryNumber entityModel;


    public LotteryNumberListViewModel(LotteryNumber lotteryNumber) {
        this.entityModel = lotteryNumber;
    }

    @Override
    public String toString() {
        return Integer.toString(entityModel.getLotteryNumber());
    }

    public int getLotteryNumber() {return entityModel.getLotteryNumber();}

    public LotteryNumber getEntityModel() { return entityModel; }

    public Object getId() {
        return entityModel.getId();
    }
}
