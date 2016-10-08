package com.velvetPearl.lottery.dataAccess;

import com.velvetPearl.lottery.IEntityUiUpdater;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.fragments.LotteryHome;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Interface for read and write operations on the Lottery entities in the data service.
 * <p>
 * BE AWARE that all calls through this interface are expected to block the calling thread whenever
 * it accesses the data service. The UI thread should only use this interface asynchronously.
 * <p/>
 */
public interface ILotteryRepository {

    Lottery getLottery(Object id, IEntityUiUpdater uiCallback) throws TimeoutException;

    /**
     *
     * @param id
     * @return
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    Lottery getLottery(Object id) throws TimeoutException;

    /**
     * Get all lottery entities in the data service sorted by created date (descending).
     * @return A collection of all the entities.
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    ArrayList<Lottery> getAllLotteries() throws TimeoutException;

    /**
     * Save a lottery in the data service, whether it's a new entity altogether or a modified one.
     * @param lottery the entity model to save/update.
     * @return the lottery model that was saved, with an updated ID reference to that of the
     * database entity.
     * @throws TimeoutException if the action did not complete before a given timeout period.
     */
    Lottery saveLottery(Lottery lottery) throws TimeoutException;
}
