package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.LinkedList;

/**
 * Created by Stensig on 28-Oct-16.
 */

public class TicketInputModel {

    private LinkedList<LotteryNumber> lotteryNumbers;
    private String owner;
    private Object ticketId;


    public TicketInputModel() {
        lotteryNumbers = new LinkedList<>();
        owner = "";
        ticketId = null;
    }

    public TicketInputModel(Ticket ticket) {
        this();
        if (ticket != null) {
            owner = ticket.getOwner();
            ticketId = ticket.getId();

            for (Object key : ticket.getLotteryNumbers().keySet()) {
                lotteryNumbers.add(ticket.getLotteryNumbers().get(key));
            }
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Object getTicketId() {
        return ticketId;
    }

    public LinkedList<LotteryNumber> getLotteryNumbers() {
        return lotteryNumbers;
    }
}
