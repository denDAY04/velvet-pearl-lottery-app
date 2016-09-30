package com.velvetPearl.lottery.dataAccess.models;

import io.realm.RealmObject;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Number extends RealmObject {
    private Object id;
    private int lotteryNumber;

    public int getLotteryNumber() {
        return lotteryNumber;
    }

    public void setLotteryNumber(int lotteryNumber) {
        this.lotteryNumber = lotteryNumber;
    }

    public Object getId() {
        return id;
    }
}
