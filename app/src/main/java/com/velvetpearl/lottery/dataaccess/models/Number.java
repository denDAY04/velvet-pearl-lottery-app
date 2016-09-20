package com.velvetpearl.lottery.dataaccess.models;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Number extends Entity {
    private int lotteryNumber;

    public int getLotteryNumber() {
        return lotteryNumber;
    }

    public void setLotteryNumber(int lotteryNumber) {
        this.lotteryNumber = lotteryNumber;
    }
}
