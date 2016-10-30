package com.velvetPearl.lottery.dataAccess;

/**
 * Created by Stensig on 13-Oct-16.
 */

public enum DataAccessEvent {
    LOTTERY_LIST_UPDATED,       /** A lottery was updated in the list of all lotteries or the list was read. */
    LOTTERY_UPDATED,            /** The lottery entity in the {@Link ApplicationDomain} class was updated. */
    TICKET_LIST_UPDATE, LOTTERY_LOADED, LOTTERY_REMOVED, LOTTERY_NUMBER_UPDATE, PRIZE_LIST_UPDATE;        /** An ticket for the current lottery was updated or the list was read. */
}
