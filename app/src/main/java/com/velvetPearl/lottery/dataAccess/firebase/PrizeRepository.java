package com.velvetPearl.lottery.dataAccess.firebase;

import com.velvetPearl.lottery.dataAccess.IPrizeRepository;
import com.velvetPearl.lottery.dataAccess.models.Prize;

import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class PrizeRepository extends FirebaseRepository implements IPrizeRepository {
    @Override
    public Prize getPrizeForNumber(Object numberId) throws TimeoutException {
        return null;
    }
}
