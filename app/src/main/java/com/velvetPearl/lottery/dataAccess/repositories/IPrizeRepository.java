package com.velvetPearl.lottery.dataAccess.repositories;

import com.velvetPearl.lottery.dataAccess.models.Prize;

import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public interface IPrizeRepository {

    void startPrizeSyncForLotteryNumber(Object numberId);

    void stopPrizeSyncForLotteryNumber(Object numberId);

    void loadAvailablePrizesForLottery(Object lotteryId);

    Prize savePrize(Prize prize);

    void deletePrize(Prize entity);
}
