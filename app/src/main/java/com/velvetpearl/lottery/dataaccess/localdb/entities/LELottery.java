package com.velvetpearl.lottery.dataaccess.localdb.entities;

import com.velvetpearl.lottery.dataaccess.models.Number;
import com.velvetpearl.lottery.dataaccess.models.Prize;
import com.velvetpearl.lottery.dataaccess.models.Ticket;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * LocalDb Entity Lottery.
 * Created by Andreas "denDAY" Stensig on 22-09-2016.
 */
public class LELottery extends RealmObject {
    @PrimaryKey
    private long id;
    @Required
    private Date created;
    private double pricePerLotteryNum;
    private int lotteryNumLowerBound;
    private int lotteryNumUpperBound;

    private RealmList<Prize> prizes;
    private RealmList<Ticket> tickets;
    private RealmList<Number> numbers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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
}

