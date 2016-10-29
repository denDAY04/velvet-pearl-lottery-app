package com.velvetPearl.lottery.dataAccess.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;


/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Ticket {
    // Entity member fields
    private Object id;
    private String owner;
    private TreeMap<Object, LotteryNumber> lotteryNumbers;
    //private LinkedList<LotteryNumber> lotteryNumbers;

    // Navigational member fields
    private Object lotteryId;

    // List for new lottery numbers not yet saved to the database.
    //private LinkedList<LotteryNumber> unsavedLotteryNumbers;

    public Ticket() {
        //lotteryNumbers = new LinkedList<>();
        lotteryNumbers = new TreeMap<>();
        //unsavedLotteryNumbers = new LinkedList<>();
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public final ArrayList<LotteryNumber> getLotteryNumbers() {
        ArrayList<LotteryNumber> list = new ArrayList<>(lotteryNumbers.size());
        for (Object key : lotteryNumbers.keySet()) {
            list.add(lotteryNumbers.get(key));
        }
        return list;
    }

    public void removeLotteryNumber(LotteryNumber number){
        if (number == null) {
            return;
        }

        if (number.getId() == null) {
            throw new IllegalArgumentException("Lottery number does not have an ID.");
        }

        lotteryNumbers.remove(number.getId());
    }

    public void addLotteryNumber(LotteryNumber number) {
        if (number == null) {
            return;
        }

        if (number.getId() == null) {
            throw new IllegalArgumentException("Lottery number does not have an ID.");
        }

        lotteryNumbers.put(number.getId(), number);
    }


//    public void setLotteryNumbers(TreeMap<Object, LotteryNumber> lotteryNumbers) {
//        this.lotteryNumbers = lotteryNumbers;
//    }

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

//    public LinkedList<LotteryNumber> getUnsavedLotteryNumbers() {
//        return unsavedLotteryNumbers;
//    }
}
