package com.velvetPearl.lottery.dataAccess.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ILotteryRepository;
import com.velvetPearl.lottery.dataAccess.LotterySingleton;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteriesScheme;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 22-Sep-16.
 */
public class LotteryRepository extends FirebaseRepository implements ILotteryRepository {

    private static final String LOG_TAG = "LotteryRepository";


    public LotteryRepository() {
        super();
    }


    @Override
    public Lottery getLottery(Object id) throws TimeoutException {
        if (id == null || id.getClass() != String.class) {
            return null;
        }

        authenticate();
        // TODO

        LotterySingleton.setActiveLottery(new Lottery());

        Query query = dbContext.getReference(LotteriesScheme.LABEL).orderByKey().equalTo((String)id);
        attachLotteryBaseListener(query);

        verifyAsyncTask();

        return LotterySingleton.getActiveLottery();
    }

    private void attachLotteryBaseListener(Query query) {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "updating fetched base lottery data for lottery ID " + dataSnapshot.getKey());
                LotterySingleton.setActiveLottery((Lottery) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public ArrayList<Lottery> getAllLotteries() throws TimeoutException {
        authenticate();
        final ArrayList<Lottery> lotteries = new ArrayList<>();
        dbContext.getReference(LotteriesScheme.LABEL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                synchronized (lock) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Lottery entry = snapshot.getValue(Lottery.class);
                        entry.setId(snapshot.getKey());
                        lotteries.add(entry);
                    }
                    Collections.sort(lotteries, new Comparator<Lottery>() {
                        @Override
                        public int compare(Lottery o1, Lottery o2) {
                            // Arguments swapped to force descending order
                            return Long.compare(o2.getCreated(), o1.getCreated());
                        }
                    });

                    unlockedByNotify = true;
                    lock.notify();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getAllLotteries: data read canceled", databaseError.toException());
            }
        });

        synchronized (lock) {
            try {
                unlockedByNotify = false;
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "getAllLotteries: wait on data-fetch interrupted", e);
            }
        }
        verifyAsyncTask();

        return lotteries;
    }

    @Override
    public Lottery saveLottery(Lottery lottery) throws TimeoutException {
        if (lottery == null)
            return null;

        authenticate();
        DatabaseReference dbObjRef = null;
        if (lottery.getId() != null && !((String)lottery.getId()).isEmpty()) {
            Log.d(LOG_TAG, "saveLottery: updating existing lottery with ID " + lottery.getId());
            dbObjRef = dbContext.getReference(LotteriesScheme.LABEL).child((String)lottery.getId());
        } else {
            Log.d(LOG_TAG, "saveLottery: saving new lottery");
            dbObjRef = dbContext.getReference(LotteriesScheme.LABEL).push();
            // Set the ID that was automatically assigned so that the returned model obj can reference
            // future db entity.
            lottery.setId(dbObjRef.getKey());
        }

        // TODO: save other entities from lottery

        HashMap<String, Object> objMap = new HashMap<>();
        objMap.put(LotteriesScheme.Children.CREATED,lottery.getCreated());
        objMap.put(LotteriesScheme.Children.PRICE_PER_LOTTERY_NUM, lottery.getPricePerLotteryNum());
        objMap.put(LotteriesScheme.Children.LOTTERY_NUM_LOWER_BOUND, lottery.getLotteryNumLowerBound());
        objMap.put(LotteriesScheme.Children.LOTTERY_NUM_UPPER_BOUND, lottery.getLotteryNumUpperBound());
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
                Log.d(LOG_TAG, "saveLottery: waiting on save action interrupted");
            }
        }
        verifyAsyncTask();

        return lottery;
    }

}
