package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.TreeMap;

/**
 * Created by Stensig on 30-Oct-16.
 */

public class WinnerListViewModel {
    private Prize prize;
    private String ticketOwner;
    private int lotteryNumber;

    public WinnerListViewModel(Prize prize) {
        if (prize != null && prize.getNumberId() != null) {
            this.prize = prize;

            TreeMap<Object, Ticket> tickets = ApplicationDomain.getInstance().getActiveLottery().getTickets();
            for (Object ticketId : tickets.keySet()) {
                Ticket ticket = tickets.get(ticketId);
                for (LotteryNumber number : ticket.getLotteryNumbers()) {
                    if (number.getId().equals(prize.getNumberId())) {
                        ticketOwner = ticket.getOwner();
                        lotteryNumber = number.getLotteryNumber();
                        return;
                    }
                }
            }
        }
    }

    public Prize getPrize() {
        return prize;
    }

    public String getTicketOwner() {
        return ticketOwner;
    }

    public int getLotteryNumber() {
        return lotteryNumber;
    }
}
