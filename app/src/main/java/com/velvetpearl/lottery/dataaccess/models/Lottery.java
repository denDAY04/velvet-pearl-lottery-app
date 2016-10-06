package com.velvetPearl.lottery.dataAccess.models;

import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Lottery {
    private Object id;
    private long created;   /* In Unix time. */
    private double pricePerLotteryNum;
    private int lotteryNumLowerBound;
    private int lotteryNumUpperBound;

    private ArrayList<Prize> prizes;
    private ArrayList<Ticket> tickets;
    private ArrayList<LotteryNumber> lotteryNumbers;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
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
        if (lotteryNumLowerBound < 1)
            throw new InvalidParameterException("Lower number boundary must be ");
        this.lotteryNumLowerBound = lotteryNumLowerBound;
    }

    public int getLotteryNumUpperBound() {
        return lotteryNumUpperBound;
    }

    public void setLotteryNumUpperBound(int lotteryNumUpperBound) {
        this.lotteryNumUpperBound = lotteryNumUpperBound;
    }

    public ArrayList<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(ArrayList<Prize> prizes) {
        this.prizes = prizes;
    }

    public ArrayList<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public ArrayList<LotteryNumber> getLotteryNumbers() {
        return lotteryNumbers;
    }

    public void setLotteryNumbers(ArrayList<LotteryNumber> lotteryNumbers) {
        this.lotteryNumbers = lotteryNumbers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Lottery ID %s", (String)id));
        sb.append(String.format(", created %d", created));
        sb.append(String.format(", price pr. num %.2f", pricePerLotteryNum));
        sb.append(String.format(", num bounds {low, high} {%d, %d}", lotteryNumLowerBound, lotteryNumUpperBound));
        sb.append(String.format(", #prizes %d", prizes != null ? prizes.size() : 0));
        sb.append(String.format(", #tickets %d", tickets != null ? tickets.size() : 0));
        sb.append(String.format(", #lotteryNumbers %d" , lotteryNumbers != null ? lotteryNumbers.size() : 0));
        return sb.toString();
    }
}
