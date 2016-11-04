package com.velvetPearl.lottery.dataAccess.repositories;

import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public interface ILotteryNumberRepository extends IFirebaseRepository {

    void startLotteryNumbersSyncForTicket(Object ticketId);

    void stopLotteryNumbersSyncForTicket(Object ticketId);

    void deleteLotteryNumber(LotteryNumber lotteryNumber);

    LotteryNumber saveLotteryNumber(LotteryNumber lotteryNumber);
}
