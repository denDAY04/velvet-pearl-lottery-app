package com.velvetpearl.lottery.dataaccess.models;

import java.util.ArrayList;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Ticket extends Entity {
    private String owner;
    private ArrayList<Number> numbers;


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<Number> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<Number> numbers) {
        this.numbers = numbers;
    }
}
