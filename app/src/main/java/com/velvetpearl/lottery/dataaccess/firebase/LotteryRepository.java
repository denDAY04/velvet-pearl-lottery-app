package com.velvetpearl.lottery.dataaccess.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.velvetpearl.lottery.R;
import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.firebase.scheme.LotteriesScheme;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 22-Sep-16.
 */
public class LotteryRepository implements ILotteryRepository {

    private static final String LOG_TAG = "LotteryRepository";
    private static final long LOCK_TIMEOUT_MS = 10000;

    private final FirebaseAuth dbAuth;
    private FirebaseDatabase dbContext = null;
    private ArrayList<Lottery> lotteries = null;
    private Object lock = new Object();
    private boolean unlockedByNotify = false;


    public LotteryRepository() {
        dbAuth = FirebaseAuth.getInstance();
    }

    @Override
    public Lottery getLottery(Object id) throws TimeoutException {
        authenticate();
        // TODO

        verifyAsyncTask();

        return null;
    }

    @Override
    public ArrayList<Lottery> getAllLotteries() throws TimeoutException {
        authenticate();

        dbContext.getReference(LotteriesScheme.LABEL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                synchronized (lock) {
                    lotteries = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Lottery entry = snapshot.getValue(Lottery.class);
                        entry.setId(snapshot.getKey());
                        lotteries.add(entry);
                    }
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

    /**
     * Authenticate access to the Firebase database.
     * NOTE that this call locks the active thread until the authorization either succeeds or fails.
     * @throws TimeoutException if the authentication task didn't complete in time.
     */
    private void authenticate() throws TimeoutException {
        if (dbContext != null)
            return;

        dbAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                synchronized (lock) {
                    if (!task.isSuccessful()) {
                        Log.w(LOG_TAG, "authenticate", task.getException());
                        Log.d(LOG_TAG, "authenticate:signInAnonymously: Firebase authentication failed");
                    } else {
                        Log.d(LOG_TAG, "authenticate:signInAnonymously: Firebase authentication succeeded");
                        dbContext = FirebaseDatabase.getInstance();
                    }
                    unlockedByNotify = true;
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            while (dbContext == null) {
                try {
                    unlockedByNotify = false;
                    lock.wait(LOCK_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "authenticate: waiting on authentication interrupted");
                }
            }
        }
        verifyAsyncTask();
    }

    /**
     * Verify whether the last async task was completed successfully, as determined by an internal
     * flag. If not, a timeout exception is thrown to indicate the event.
     * @throws TimeoutException if the flag was not set.
     */
    private void verifyAsyncTask() throws TimeoutException {
        if (!unlockedByNotify)
            throw new TimeoutException();
    }

}
