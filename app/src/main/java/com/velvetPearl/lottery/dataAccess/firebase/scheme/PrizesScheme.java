package com.velvetPearl.lottery.dataAccess.firebase.scheme;

/**
 * Created by Stensig on 30-Sep-16.
 */
public abstract class PrizesScheme {
    public static final String LABEL = "prizes";

    public static class Children {
        public static final String NAME = "name";
        public static final String NUMBER_ID = "numberId";
        public static final String LOTTERY_ID = "lotteryId";
    }
}
