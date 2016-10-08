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
import com.velvetPearl.lottery.IEntityUiUpdater;
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

    private Query activeQuery = null;
    private ValueEventListener entityListener = null;

    public LotteryRepository() {
        super();
    }


    @Override
    public Lottery getLottery(Object id, IEntityUiUpdater uiCallback) throws TimeoutException {
        if (id == null || id.getClass() != String.class) {
            return null;
        }
        String entityId = (String) id;
        authenticate();
        resetState();

        Log.d(LOG_TAG, "getLottery querying for lottery ID " + entityId);
        activeQuery = dbContext.getReference(LotteriesScheme.LABEL).orderByKey().equalTo(entityId);
        entityListener = attachEntityListener(activeQuery, entityId, uiCallback);

        synchronized (lock) {
            try {
                unlockedByNotify = false;
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "getLottery data fetch sleep interrupted", e);
            }
        }
        verifyAsyncTask();

        return LotterySingleton.getActiveLottery();
    }

    @Override
    public Lottery getLottery(Object id) throws TimeoutException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Reset the state for the active lottery and its query.
     * <p>
     * This leaves the LotterySingleton's active lottery reference null and decouples any Firebase
     * listeners on the previous query.
     */
    private void resetState() {
        // Decouple the listener for the last query (if any) so that it doesn't keep updating
        // on that previous data.
        detachEntityListener(activeQuery, entityListener);

        // Clear active lottery such that a bad search leaves a null ref for the program to test on.
        LotterySingleton.setActiveLottery(null);
    }

    /**
     * Attach a Firebase {@Link ValueEventListener} to the {@Link Query} object and sync any reads
     * to LotterySingleton's active lottery reference.
     * @param query The query on which to attach the listener.
     * @param entityId The ID (Firebase key value) of the entity that will be synced.
     * @param uiUpdater The callback object for updating the UI whenever the query data is synced.
     * @return The listener that has been attached.
     */
    private ValueEventListener attachEntityListener(Query query, final String entityId, final IEntityUiUpdater uiUpdater) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                synchronized (lock) {
                    DataSnapshot data = dataSnapshot.child(entityId);
                    if (data.getChildrenCount() != 0) {     // The query-result for ID is not empty
                        Log.d(LOG_TAG, "reading lottery entity for ID " + data.getKey());
                        Lottery result = data.getValue(Lottery.class);
                        result.setId(data.getKey());
                        LotterySingleton.setActiveLottery(result);
                        uiUpdater.updateUi();
                    }

                    unlockedByNotify = true;
                    lock.notifyAll();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "reading lottery entity error", databaseError.toException());
            }
        };

        query.addValueEventListener(listener);
        return listener;
    }

    /**
     * Remove the {@Link ValueEventListener} from the {@Link Query} object.
     * @param query The query from which to remove the listener.
     * @param listener The listener that should be removed.
     */
    private void detachEntityListener(Query query, ValueEventListener listener) {
        if (query != null && listener != null) {
            query.removeEventListener(entityListener);
        }
    }

    @Override
    public ArrayList<Lottery> getAllLotteries() throws TimeoutException {
        authenticate();
        final ArrayList<Lottery> lotteries = new ArrayList<>();
        dbContext.getReference(LotteriesScheme.LABEL).addListenerForSingleValueEvent(new ValueEventListener() {
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
