package com.velvetpearl.lottery.dataaccess.models;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Prize extends Entity {
    private String name;
    private Number winningNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Number getWinningNumber() {
        return winningNumber;
    }

    public void setWinningNumber(Number winningNumber) {
        this.winningNumber = winningNumber;
    }
}
