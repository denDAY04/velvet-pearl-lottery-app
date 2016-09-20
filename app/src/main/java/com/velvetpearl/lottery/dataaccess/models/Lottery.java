package com.velvetpearl.lottery.dataaccess.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Lottery extends Entity{
    private Date created;
    private double pricePerLotteryNum;
    private int lotteryNumLowerBound;
    private int lotteryNumUpperBound;

    private ArrayList<Prize> prizes;
    private ArrayList<Ticket> tickets;
    private ArrayList<Number> numbers;

    public Date getCreated() {
        return created;
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

    public ArrayList<Number> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<Number> numbers) {
        this.numbers = numbers;
    }
}
