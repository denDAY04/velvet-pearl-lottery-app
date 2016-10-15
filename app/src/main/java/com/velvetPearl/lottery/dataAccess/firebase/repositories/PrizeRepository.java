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

    public PrizeRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }

    @Override
    public Prize getPrizeForNumber(Object numberId) throws TimeoutException {
        return null;
    }

    @Override
    public Prize savePrize(Prize prize) throws TimeoutException {
        if (prize == null) {
            return null;
        }
        //authenticate();

        DatabaseReference dbObjRef = null;
        if (prize.getId() != null && !((String)prize.getId()).isEmpty()) {
            Log.d(LOG_TAG, "savePrize: updating existing prize with ID " + prize.getId());
            dbObjRef = dbContext.getReference(PrizesScheme.LABEL).child((String)prize.getId());
        } else {
            Log.d(LOG_TAG, "savePrize: saving new prize");
            dbObjRef = dbContext.getReference(PrizesScheme.LABEL).push();
            // Set the ID that was automatically assigned so that the returned model obj can reference
            // future db entity.
            prize.setId(dbObjRef.getKey());
        }

        HashMap<String, Object> objMap = new HashMap<>();
        objMap.put(PrizesScheme.Children.NAME, prize.getName());
        objMap.put(PrizesScheme.Children.NUMBER_ID, prize.getLotteryNumberId());
        dbObjRef.setValue(objMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                synchronized (lock) {
                    unlockedByNotify = true;
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            try {
                unlockedByNotify = false;
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "saveLotteryNumber: waiting on save action interrupted", e);
            }
        }
        verifyAsyncTask();

        return prize;
    }

    @Override
    protected ValueEventListener attachEntityListener(Query query, String entityId) {
        return null;
    }
}
