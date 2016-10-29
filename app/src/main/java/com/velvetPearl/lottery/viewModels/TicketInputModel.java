package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.LinkedList;

/**
 * Created by Stensig on 28-Oct-16.
 */

public class TicketInputModel {

    private Ticket ticket;
    private LinkedList<LotteryNumber> unsavedNumbers;

    public TicketInputModel() {
        ticket = new Ticket();
        ticket.setLotteryId(ApplicationDomain.getInstance().getActiveLottery().getId());
        unsavedNumbers = new LinkedList<>();
    }

    public TicketInputModel(Ticket ticket) {
        this();
        if (ticket != null) {
            this.ticket = ticket;
        }
    }

    public LinkedList<LotteryNumber> getUnsavedNumbers() {
        return unsavedNumbers;
    }

    public Ticket getTicket() {
        return ticket;
    }

}
