package com.velvetPearl.lottery.dataAccess.models;


/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Prize {
    // Entity member fields
    private Object id;
    private String name;

    // Navigational member fields
    private Object lotteryNumberId;

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

    public Object getLotteryNumberId() {
        return lotteryNumberId;
    }

    public void setLotteryNumberId(Object numberId) {
        this.lotteryNumberId = numberId;
    }
}
