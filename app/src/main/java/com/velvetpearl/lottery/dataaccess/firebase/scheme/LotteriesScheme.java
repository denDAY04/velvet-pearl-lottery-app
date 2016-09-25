package com.velvetpearl.lottery.dataaccess.firebase.scheme;

/**
 * Created by Stensig on 25-Sep-16.
 */
public abstract class LotteriesScheme {
    public static final String LABEL = "lotteries";

    public static class Children {
        public static final String CREATED = "created";
        public static final String PRICE_PER_LOTTERY_NUM = "pricePerLotteryNum";
        public static final String LOTTERY_NUM_LOWER_BOUND = "lotteryNumLowerBound";
        public static final String LOTTERY_NUM_UPPER_BOUND = "lotteryNumUpperBound";
    }
}
