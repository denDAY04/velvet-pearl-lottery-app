package com.velvetPearl.lottery.dataAccess;

import com.velvetPearl.lottery.dataAccess.models.Prize;

import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public interface IPrizeRepository {

    Prize getPrizeForNumber(Object numberId) throws TimeoutException;

    Prize savePrize(Prize prize) throws TimeoutException;

}
