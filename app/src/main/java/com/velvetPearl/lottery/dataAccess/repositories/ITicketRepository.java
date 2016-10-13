package com.velvetPearl.lottery.dataAccess.repositories;

import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public interface ITicketRepository {

    /**
     *
     * @param id
     * @return
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    Ticket getTicket(Object id);

    /**
     *
     * @param lotteryId
     * @return
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    ArrayList<Ticket> getTicketsForLottery(Object lotteryId);

    /**
     *
     * @param ticket
     * @return
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    Ticket saveTicket(Ticket ticket) throws TimeoutException;
}
