package com.velvetPearl.lottery.dataAccess.models;


/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Prize {
    // Entity member fields
    private Object id;
    private String name;

    // Navigational member fields
    private Object numberId;
    private Object lotteryId;

    public void copy(Prize other) {
        id = other.id;
        name = other.name;
        numberId = other.numberId;
        lotteryId = other.lotteryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getNumberId() {
        return numberId;
    }

    public void setNumberId(Object numberId) {
        this.numberId = numberId;
    }

    public Object getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(Object lotteryId) {
        this.lotteryId = lotteryId;
    }
}
