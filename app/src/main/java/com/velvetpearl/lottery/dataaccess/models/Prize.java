package com.velvetPearl.lottery.dataAccess.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Prize extends RealmObject {
    @PrimaryKey
    private int prizeId;
    @Required
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

    public int getPrizeId() {
        return prizeId;
    }
}
