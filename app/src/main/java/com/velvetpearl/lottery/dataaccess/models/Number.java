package com.velvetPearl.lottery.dataAccess.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Number extends RealmObject {
    @PrimaryKey
    private int numberId;
    private int lotteryNumber;

    public int getLotteryNumber() {
        return lotteryNumber;
    }

    public void setLotteryNumber(int lotteryNumber) {
        this.lotteryNumber = lotteryNumber;
    }

    public int getNumberId() {
        return numberId;
    }
}
