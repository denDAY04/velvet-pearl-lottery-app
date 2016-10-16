package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteriesScheme;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.security.DomainCombiner;
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

    private ChildEventListener childEventListener;

    public LotteryRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }

    @Override
    public Lottery getLottery(Object id) {
        if (id == null || id.getClass() != String.class) {
            return null;
        }
        String entityId = (String) id;
        resetState();

        Log.d(LOG_TAG, "getLottery querying for lottery ID " + entityId);


        query = dbContext.getReference(LotteriesScheme.LABEL).orderByKey().equalTo(entityId);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "getLottery onChildAdded ID " + dataSnapshot.getKey());
                Lottery entity = dataSnapshot.getValue(Lottery.class);
                entity.setId(dataSnapshot.getKey());
                ApplicationDomain.getInstance().setActiveLottery(entity);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_LOADED);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "getLottery onChildChanged ID " + dataSnapshot.getKey());
                Lottery currentLottery = ApplicationDomain.getInstance().getActiveLottery();
                Lottery entity = dataSnapshot.getValue(Lottery.class);
                entity.setId(dataSnapshot.getKey());
                entity.setPrizes(currentLottery.getPrizes());
                entity.setTickets(currentLottery.getTickets());
                ApplicationDomain.getInstance().setActiveLottery(entity);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_UPDATED);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "getLottery onChildRemoved ID " + dataSnapshot.getKey());
                ApplicationDomain.getInstance().setActiveLottery(null);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_REMOVED);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                return;     // Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getLottery onCancelled", databaseError.toException());
            }
        };

        query.addChildEventListener(childEventListener);

//        query = dbContext.getReference(LotteriesScheme.LABEL).orderByKey().equalTo(entityId);
//        entityListener = attachEntityListener(query, entityId);

        return ApplicationDomain.getInstance().getActiveLottery();
    }

    /**
     * Reset the state for the active lottery and its query.
     * <p>
     * This leaves the ApplicationDomain's active lottery reference null and decouples any Firebase
     * listeners on the previous query.
     */
    private void resetState() {
        // Decouple the listener for the last query (if any) so that it doesn't keep updating
        // on that previous data.
        //detachEntityListener(query, entityListener);
        if (query != null)
            query.removeEventListener(childEventListener);

        // Clear active lottery such that a bad search leaves a null ref for the program to test on.
        ApplicationDomain.getInstance().setActiveLottery(null);

    }

    @Override
    protected ValueEventListener attachEntityListener(Query query, final String entityId) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot data = dataSnapshot.child(entityId);
                if (data.getChildrenCount() != 0) {     // The query-result for ID is not empty
                    Log.d(LOG_TAG, "reading lottery entity for ID " + data.getKey());
                    Lottery result = data.getValue(Lottery.class);
                    result.setId(data.getKey());
                    ApplicationDomain.getInstance().setActiveLottery(result);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_UPDATED);
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

    @Override
    public void getAllLotteries() {
        dbContext.getReference(LotteriesScheme.LABEL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Lottery> lotteries = new ArrayList<>();
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

                ApplicationDomain.getInstance().setAllLotteries(lotteries);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_LIST_UPDATED);
                Log.d(LOG_TAG, "getAllLotteries data fetch finished; observes notified.");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getAllLotteries: data read canceled", databaseError.toException());
            }
        });
    }

    @Override
    public Lottery saveLottery(Lottery lottery) throws TimeoutException {
        if (lottery == null)
            return null;

        //authenticate();
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
