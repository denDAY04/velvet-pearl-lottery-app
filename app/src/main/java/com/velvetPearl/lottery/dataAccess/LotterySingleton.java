package com.velvetPearl.lottery.dataAccess;

import android.app.Application;

import com.velvetPearl.lottery.dataAccess.firebase.LotteryRepository;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

/**
 * Singleton class for application-wide access to the data entity manager objects.
 */
public class LotterySingleton extends Application {

    private static ILotteryRepository lotteryRepository = null;
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
    }
}
