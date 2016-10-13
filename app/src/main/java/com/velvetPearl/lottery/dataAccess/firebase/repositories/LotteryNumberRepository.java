package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.IEntityUiUpdater;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteryNumbersScheme;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class LotteryNumberRepository extends FirebaseRepository implements ILotteryNumberRepository {

    private static final String LOG_TAG = "TicketRepository";

    public LotteryNumberRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }


    @Override
    public ArrayList<LotteryNumber> getLotteryNumbersForTicket(Object ticketId) throws TimeoutException {
        if (ticketId == null || ticketId.getClass() != String.class) {
            return null;
        }

        final ArrayList<LotteryNumber> result = new ArrayList<>();
        //authenticate();
        dbContext.getReference(LotteryNumbersScheme.LABEL)
                .equalTo((String) ticketId, LotteryNumbersScheme.Children.TICKET_ID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        synchronized (lock) {
                            for (DataSnapshot entity : dataSnapshot.getChildren()) {
                                LotteryNumber lotteryNumber = entity.getValue(LotteryNumber.class);
                                lotteryNumber.setId(entity.getKey());
                                result.add(lotteryNumber);
                            }
                            unlockedByNotify = true;
                            lock.notify();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(LOG_TAG, "getLotteryNumbersForTicket:onCancelled canceled data fetch", databaseError.toException());
                    }
                });

        synchronized (lock) {
            try {
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "getLotteryNumbersForTicket wait on data fetch interrupted", e);
            }
        }
        verifyAsyncTask();

        return result;
    }

    @Override
    public LotteryNumber saveLotteryNumber(LotteryNumber lotteryNumber) throws TimeoutException {
        if (lotteryNumber == null) {
            return null;
        }
        //authenticate();

        DatabaseReference dbObjRef = null;
        if (lotteryNumber.getId() != null && !((String)lotteryNumber.getId()).isEmpty()) {
            Log.d(LOG_TAG, "saveLotteryNumber: updating existing lottery number with ID " + lotteryNumber.getId());
            dbObjRef = dbContext.getReference(LotteryNumbersScheme.LABEL).child((String)lotteryNumber.getId());
        } else {
            Log.d(LOG_TAG, "saveLotteryNumber: saving new lottery number");
            dbObjRef = dbContext.getReference(LotteryNumbersScheme.LABEL).push();
            // Set the ID that was automatically assigned so that the returned model obj can reference
            // future db entity.
            lotteryNumber.setId(dbObjRef.getKey());
        }

        HashMap<String, Object> objMap = new HashMap<>();
        objMap.put(LotteryNumbersScheme.Children.LOTTERY_NUMBER,lotteryNumber.getLotteryNumber());
        objMap.put(LotteryNumbersScheme.Children.TICKET_ID,lotteryNumber.getTicketId());
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

        return lotteryNumber;
    }

    @Override
    protected ValueEventListener attachEntityListener(Query query, String entityId) {
        return null;
    }
}
