package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.repositories.IPrizeRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.PrizesScheme;
import com.velvetPearl.lottery.dataAccess.models.Prize;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class PrizeRepository extends FirebaseRepository implements IPrizeRepository {

    private static final String LOG_TAG = "PrizeRepository";

    public PrizeRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }

    @Override
    public Prize getPrizeForNumber(Object numberId) {
        return null;
    }

    @Override
    public void getPrizesForLottery(Object lotteryId) {

    }

    @Override
    public Prize savePrize(Prize prize) {
        if (prize == null) {
            return null;
        }

        DatabaseReference dbPrize;
        if (prize.getId() != null && !((String) prize.getId()).isEmpty()) {
            Log.d(LOG_TAG, String.format("Saving existing prize (ID %s).", prize.getId()));
            dbPrize = dbContext.getReference(PrizesScheme.LABEL).child((String) prize.getId());
        } else {
            Log.d(LOG_TAG, "Saving new prize.");
            dbPrize = dbContext.getReference(PrizesScheme.LABEL).push();
            prize.setId(dbPrize.getKey());
        }

        HashMap<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(PrizesScheme.Children.NAME, prize.getName());
        fieldValues.put(PrizesScheme.Children.NUMBER_ID, prize.getLotteryNumberId());
        dbPrize.setValue(fieldValues);

        return prize;
    }

    @Override
    public void deletePrize(Prize entity) {
        if (entity == null ||  entity.getId() == null) {
            return;
        }

        dbContext.getReference(PrizesScheme.LABEL).child((String) entity.getId()).removeValue();
    }

}
