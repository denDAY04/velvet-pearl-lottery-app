package com.velvetPearl.lottery.dataAccess.models;

import java.util.ArrayList;


/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Ticket {
    // Entity member fields
    private Object id;
    private String owner;
    private ArrayList<LotteryNumber> lotteryNumbers;

    // Navigational member fields
    private Object lotteryId;

    public Ticket() {
        lotteryNumbers = new ArrayList<>();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<LotteryNumber> getLotteryNumbers() {
        return lotteryNumbers;
    }

    public void setLotteryNumbers(ArrayList<LotteryNumber> lotteryNumbers) {
        this.lotteryNumbers = lotteryNumbers;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(Object lotteryId) {
        this.lotteryId = lotteryId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Ticket other = (Ticket) obj;
        if (other.id == null) {
            return false;
        }

        return id.equals(other.id);
    }
}
