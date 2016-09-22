package com.velvetpearl.lottery.dataaccess.localdb;

import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.localdb.entities.LELottery;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import io.realm.Realm;

/**
 * Created by Andreas "denDAY" Stensig on 22-09-2016.
 */
public class LotteryRepository implements ILotteryRepository {
    @Override
    public Lottery getLottery(long id) {
        Realm dbContext = Realm.getDefaultInstance();
        LELottery dbLELottery = dbContext.where(LELottery.class).equalTo("id", id).findFirst();
        return entityToModel(dbLELottery);
    }

    @Override
    public Lottery saveLottery(Lottery lottery) {
        return null;
    }

    private static Lottery entityToModel(LELottery entity) {
        if (entity == null)
            return null;

        Lottery model = new Lottery();
        model.setId(entity.getId());
        model.setCreated(entity.getCreated());
        model.setLotteryNumLowerBound(entity.getLotteryNumLowerBound());
        model.setLotteryNumUpperBound(entity.getLotteryNumUpperBound());
        model.setPricePerLotteryNum(entity.getPricePerLotteryNum());
        // TODO: convert the relationship members

        return model;
    }
}
