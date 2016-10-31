package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.TreeMap;

/**
 * Created by Stensig on 30-Oct-16.
 */

public class PrizeListViewModel {

    private Prize prize;
    private Ticket ticket;

    public PrizeListViewModel(Prize prize) {
        this.prize = prize;
        if (prize.getNumberId() != null) {
            TreeMap<Object, Ticket> tickets = ApplicationDomain.getInstance().getActiveLottery().getTickets();
            for (Object ticketId : tickets.keySet()) {
                Ticket activeTicket = tickets.get(ticketId);
                for (LotteryNumber number : activeTicket.getLotteryNumbers()) {
                    if (number.getId().equals(prize.getNumberId())) {
                        this.ticket = activeTicket;
                    }
                }
            }
        }
    }

    public Prize getPrize() {return prize;}

    public Ticket getTicket() {return ticket;}
}
