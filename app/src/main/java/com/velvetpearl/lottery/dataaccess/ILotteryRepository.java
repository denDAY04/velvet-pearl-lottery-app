package com.velvetpearl.lottery.dataaccess;

import com.velvetpearl.lottery.dataaccess.models.Lottery;

/**
 * Created by Andras "denDAY" Stensig on 20-Sep-16.
 */
public interface ILotteryRepository {
    Lottery GetLottery(int id);
    Lottery NewLottery(Lottery lottery);
}
