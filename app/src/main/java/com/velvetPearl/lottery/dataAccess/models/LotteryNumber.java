package com.velvetPearl.lottery.dataAccess.models;

import io.realm.RealmObject;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class LotteryNumber {
    // Entity member fields
    private Object id;
    private int lotteryNumber;
    private Prize winningPrize;

    // Navigational member fields
    private Object ticketId;

    public int getLotteryNumber() {
        return lotteryNumber;
    }

    public void setLotteryNumber(int lotteryNumber) {
        this.lotteryNumber = lotteryNumber;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Prize getWinningPrize() {
        return winningPrize;
    }

    public void setWinningPrize(Prize winningPrize) {
        this.winningPrize = winningPrize;
    }

    public Object getTicketId() {
        return ticketId;
    }

    public void setTicketId(Object ticketId) {
        this.ticketId = ticketId;
    }
}
