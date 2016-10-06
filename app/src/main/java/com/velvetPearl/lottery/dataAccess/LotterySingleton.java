package com.velvetPearl.lottery.dataAccess;

import android.app.Application;

import com.velvetPearl.lottery.dataAccess.firebase.LotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.LotteryRepository;
import com.velvetPearl.lottery.dataAccess.firebase.PrizeRepository;
import com.velvetPearl.lottery.dataAccess.firebase.TicketRepository;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

/**
 * Singleton class for application-wide access to the data entity manager objects.
 */
public class LotterySingleton extends Application {

    private static ILotteryRepository lotteryRepository = null;
    private static ITicketRepository ticketRepository = null;
    private static ILotteryNumberRepository lotteryNumberRepository = null;
    private static IPrizeRepository prizeRepository = null;

    private static Lottery activeLottery = null;

    /**
     * Get a reference to the Lottery repository object.
     * @return
     */
    public static ILotteryRepository getInstance() {
        if (lotteryRepository == null) {
            lotteryRepository = new LotteryRepository();
        }
        return lotteryRepository;
    }

    public static ITicketRepository getTicketInstance() {
        if (ticketRepository == null) {
            ticketRepository = new TicketRepository();
        }
        return ticketRepository;
    }

    public static ILotteryNumberRepository getLotteryNumberInstance() {
        if (lotteryNumberRepository == null) {
            lotteryNumberRepository = new LotteryNumberRepository();
        }
        return lotteryNumberRepository;
    }

    public static IPrizeRepository getPrizeRepository() {
        if (prizeRepository == null) {
            prizeRepository = new PrizeRepository();
        }
        return prizeRepository;
    }

    /**
     * Get the active lottery.
     * @return the active lottery, or null if setActiveLottery has not yet been called.
     */
    public static Lottery getActiveLottery() {
        return activeLottery;
    }

    /**
     * Store the active lottery to be processed in the singleton.
     * @param activeLottery the lottery to be processed.
     */
    public static void setActiveLottery(Lottery activeLottery) {
        LotterySingleton.activeLottery = activeLottery;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        lotteryRepository = new LotteryRepository();
        ticketRepository = new TicketRepository();
        lotteryNumberRepository = new LotteryNumberRepository();
        prizeRepository = new PrizeRepository();
    }
}
