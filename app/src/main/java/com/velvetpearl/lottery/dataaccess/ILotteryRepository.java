package com.velvetpearl.lottery.dataaccess;

import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.util.ArrayList;

/**
 * Created by Andras "denDAY" Stensig on 20-Sep-16.
 */
public interface ILotteryRepository {
    Lottery getLottery(long id);
    ArrayList<Lottery> getAllLotteries();
    Lottery saveLottery(Lottery lottery);
}
