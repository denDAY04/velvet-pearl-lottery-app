package com.velvetPearl.lottery.dataAccess.firebase.scheme;

/**
 * Created by Stensig on 30-Sep-16.
 */
public abstract class LotteryNumbersScheme {
    public static final String LABEL = "numbers";

    public static class Children {
        public static final String LOTTERY_NUMBER = "lotteryNumber";
        public static final String TICKET_ID = "ticketId";
        public static final String PRICE = "price";
    }
}
