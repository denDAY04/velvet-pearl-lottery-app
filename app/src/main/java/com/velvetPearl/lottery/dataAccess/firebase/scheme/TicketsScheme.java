package com.velvetPearl.lottery.dataAccess.firebase.scheme;

/**
 * Created by Stensig on 30-Sep-16.
 */
public abstract class TicketsScheme {
    public static final String LABEL = "tickets";

    public static class Children {
        public static final String OWNER = "owner";
        public static final String LOTTERY_ID = "lotteryId";
    }
}
