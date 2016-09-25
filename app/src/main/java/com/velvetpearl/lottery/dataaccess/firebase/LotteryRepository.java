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
import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.firebase.scheme.LotteriesScheme;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Stensig on 22-Sep-16.
 */
public class LotteryRepository implements ILotteryRepository {

    private static final String LOG_TAG = "LotteryRepository";

    private final FirebaseAuth dbAuth;
    private FirebaseDatabase dbContext = null;
    private ArrayList<Lottery> lotteries = null;
    private Object lock = new Object();

    public LotteryRepository() {
        dbAuth = FirebaseAuth.getInstance();

        /*dbAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(LOG_TAG, "signInAnonymously", task.getException());
                    Log.d(LOG_TAG, "firebase authentication failed");
                } else {
                    dbContext = FirebaseDatabase.getInstance();
                    //loadLotteryData();
                }
            }
        });*/
        final Task<AuthResult> authenticationTask = dbAuth.signInAnonymously();
        synchronized (authenticationTask) {
            authenticationTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    authenticationTask.notify();
                }
            });

            try {
                authenticationTask.wait();
            } catch (InterruptedException e) {
            }
        }
        dbContext = FirebaseDatabase.getInstance();
        Log.d(LOG_TAG, "done");
    }

    @Override
    public Lottery getLottery(long id) {
        return null;
    }

    @Override
    public ArrayList<Lottery> getAllLotteries() {
        while (dbContext == null) {
            try {
                lock.wait(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        dbContext.getReference(LotteriesScheme.LABEL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                synchronized (lock) {
                    lotteries = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d(LOG_TAG, "getAllLotteries: child read with id " + snapshot.getKey());
                        Lottery entry = snapshot.getValue(Lottery.class);
                        entry.setId(snapshot.getKey());
                        lotteries.add(entry);
                    }
                    lock.notify();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getAllLotteries: data read canceled", databaseError.toException());
            }
        });
        return lotteries;

        /*
        synchronized (lock) {
            loadLotteryData();
            while (lotteries == null) {
                try {
                    lock.wait(10);
                } catch (InterruptedException e) {
                }
            }
        }
        return lotteries;
        */
    }

    @Override
    public Lottery saveLottery(Lottery lottery) {
        DatabaseReference dbObjRef = null;
        if (lottery == null)
            return null;
        else if (lottery.getId() != null && !lottery.getId().isEmpty()) {
            Log.d(LOG_TAG, "saveLottery: updating existing lottery with ID " + lottery.getId());
            dbObjRef = dbContext.getReference(LotteriesScheme.LABEL).child(lottery.getId());
        } else {
            Log.d(LOG_TAG, "saveLottery: saving new lottery");
            dbObjRef = dbContext.getReference(LotteriesScheme.LABEL).push();
            // Set the ID that was automatically assigned so that the returned model obj can reference
            // future db entity.
            lottery.setId(dbObjRef.getKey());
        }

        HashMap<String, Object> objMap = new HashMap<>();
        objMap.put(LotteriesScheme.Children.CREATED,lottery.getCreated());
        objMap.put(LotteriesScheme.Children.PRICE_PER_LOTTERY_NUM, lottery.getPricePerLotteryNum());
        objMap.put(LotteriesScheme.Children.LOTTERY_NUM_LOWER_BOUND, lottery.getLotteryNumLowerBound());
        objMap.put(LotteriesScheme.Children.LOTTERY_NUM_UPPER_BOUND, lottery.getLotteryNumUpperBound());
        dbObjRef.setValue(objMap);

        return lottery;
    }

    private void loadLotteryData() {
        dbContext.getReference(LotteriesScheme.LABEL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                synchronized (lock) {
                    lotteries = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d(LOG_TAG, "getAllLotteries: child read with id " + snapshot.getKey());
                        Lottery entry = snapshot.getValue(Lottery.class);
                        entry.setId(snapshot.getKey());
                        lotteries.add(entry);
                    }
                    lock.notify();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getAllLotteries: data read canceled", databaseError.toException());
            }
        });
    }
}
