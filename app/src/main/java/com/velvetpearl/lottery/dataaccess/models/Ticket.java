package com.velvetpearl.lottery.dataaccess.models;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Ticket extends RealmObject {
    @PrimaryKey
    private int ticketId;
    @Required
    private String owner;
    @Required
    private RealmList<Number> numbers;


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public RealmList<Number> getNumbers() {
        return numbers;
    }

    public void setNumbers(RealmList<Number> numbers) {
        this.numbers = numbers;
    }

    public int getTicketId() {
        return ticketId;
    }
}
