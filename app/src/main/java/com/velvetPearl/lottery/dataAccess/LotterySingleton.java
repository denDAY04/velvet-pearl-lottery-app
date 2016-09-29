package com.velvetPearl.lottery.dataAccess;

import android.app.Application;

import com.velvetPearl.lottery.dataAccess.firebase.LotteryRepository;

/**
 * Created by Stensig on 29-Sep-16.
 */
public class LotterySingleton extends Application {

    private static ILotteryRepository lotteryRepository = null;


    public static ILotteryRepository getInstance() {
        if (lotteryRepository == null) {
            lotteryRepository = new LotteryRepository();
        }
        return lotteryRepository;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        lotteryRepository = new LotteryRepository();
    }
}
