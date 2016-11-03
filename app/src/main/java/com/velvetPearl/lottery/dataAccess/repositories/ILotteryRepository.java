package com.velvetPearl.lottery.dataAccess.repositories;

import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.util.concurrent.TimeoutException;

/**
 *
 */
public interface ILotteryRepository {

    /**
     * Load the lottery with the given ID and store it in the app domain singleton.
     * @param id Entity ID for the lottery to load.
     */
    void loadLottery(Object id);

    /**
     * Get all lottery entities in the data service sorted by created date (descending).
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    void loadAllLotteries();

    /**
     * Save a lottery in the data service, whether it's a new entity altogether or a modified one.
     * @param lottery the entity model to save/update.
     * @return the lottery model that was saved, with an updated ID reference to that of the
     * database entity.
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    Lottery saveLottery(Lottery lottery);

    void deleteLottery(Lottery entity);
}
