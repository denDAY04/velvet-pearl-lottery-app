package com.velvetpearl.lottery.dataaccess.models;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Lottery extends RealmObject {
    @PrimaryKey
    private int lotteryId;
    @Required
    private Date created;
    @Required
    private double pricePerLotteryNum;
    @Required
    private int lotteryNumLowerBound;
    @Required
    private int lotteryNumUpperBound;

    private RealmList<Prize> prizes;
    private RealmList<Ticket> tickets;
    private RealmList<Number> numbers;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created == null ? new Date() : created;
    }

    public double getPricePerLotteryNum() {
        return pricePerLotteryNum;
    }

    public void setPricePerLotteryNum(double pricePerLotteryNum) {
        this.pricePerLotteryNum = pricePerLotteryNum;
    }

    public int getLotteryNumLowerBound() {
        return lotteryNumLowerBound;
    }

    public void setLotteryNumLowerBound(int lotteryNumLowerBound) {
        this.lotteryNumLowerBound = lotteryNumLowerBound;
    }

    public int getLotteryNumUpperBound() {
        return lotteryNumUpperBound;
    }

    public void setLotteryNumUpperBound(int lotteryNumUpperBound) {
        this.lotteryNumUpperBound = lotteryNumUpperBound;
    }

    public RealmList<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(RealmList<Prize> prizes) {
        this.prizes = prizes;
    }

    public RealmList<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(RealmList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public RealmList<Number> getNumbers() {
        return numbers;
    }

    public void setNumbers(RealmList<Number> numbers) {
        this.numbers = numbers;
    }

    public int getLotteryId() {
        return lotteryId;
    }
}
