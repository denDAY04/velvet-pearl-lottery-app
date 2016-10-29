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
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.firebase.FirebaseQueryObject;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteriesScheme;
import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 22-Sep-16.
 */
public class LotteryRepository extends FirebaseRepository implements ILotteryRepository {

    private static final String LOG_TAG = "LotteryRepository";

    public LotteryRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }

    @Override
    public void loadLottery(Object id) {
        if (id == null || id.getClass() != String.class) {
            return;
        }

        String entityId = (String) id;
        // Don't reload if the lottery is already stored.
        Lottery currentLottery = ApplicationDomain.getInstance().getActiveLottery();
        if (currentLottery != null && currentLottery.getId().equals(entityId)) {
            return;
        }

        resetState();

        Log.d(LOG_TAG, "loadLottery querying for lottery ID " + entityId);

        FirebaseQueryObject queryObject = new FirebaseQueryObject();
        queryObject.query = dbContext.getReference(LotteriesScheme.LABEL).orderByKey().equalTo(entityId);
        queryObject.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "loadLottery onChildAdded ID " + dataSnapshot.getKey());
                Lottery entity = dataSnapshot.getValue(Lottery.class);
                entity.setId(dataSnapshot.getKey());
                ApplicationDomain.getInstance().setActiveLottery(entity);
                ApplicationDomain.getInstance().ticketRepository.loadTicketsForLottery(entity.getId());
                ApplicationDomain.getInstance().prizeRepository.loadPrizesForLottery(entity.getId());
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_LOADED);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "loadLottery onChildChanged ID " + dataSnapshot.getKey());
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
                Log.d(LOG_TAG, "loadLottery onChildRemoved ID " + dataSnapshot.getKey());
                ApplicationDomain.getInstance().setActiveLottery(null);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_REMOVED);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return;     // Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "loadLottery onCancelled", databaseError.toException());
            }
        };
        attachAndStoreQueryObject(entityId, queryObject);
    }

    /**
     * Reset the state for the active lottery and its query.
     * <p>
     * This leaves the ApplicationDomain's active lottery reference null and decouples any Firebase
     * listeners on the previous query.
     */
    private void resetState() {
        detachAndRemoveAllQueryObjects();

        // Clear active lottery such that a bad search leaves a null ref for the program to test on.
        ApplicationDomain.getInstance().setActiveLottery(null);
    }

    @Override
    public void loadAllLotteries() {
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
                Log.d(LOG_TAG, "loadAllLotteries data fetch finished; observes notified.");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "loadAllLotteries: data read canceled", databaseError.toException());
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

    @Override
    public void deleteLottery(Lottery entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        TreeMap<Object, Ticket> tickets =  entity.getTickets();
        for (Object key : tickets.keySet()) {
            ApplicationDomain.getInstance().ticketRepository.deleteTicket(tickets.get(key));
        }

        TreeMap<Object, Prize> prize = entity.getPrizes();
        for (Object key : prize.keySet()) {
            ApplicationDomain.getInstance().prizeRepository.deletePrize(prize.get(key));
        }

        dbContext.getReference(LotteriesScheme.LABEL).child((String) entity.getId()).removeValue();
    }

}
