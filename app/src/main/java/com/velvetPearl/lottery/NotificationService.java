package com.velvetPearl.lottery;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.firebase.FirebaseQueryObject;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteriesScheme;
import com.velvetPearl.lottery.dataAccess.models.Lottery;


/**
 * Created by Stensig on 24-Nov-16.
 */

public class NotificationService extends IntentService {

    private static final String LOG_TAG = "NotificationService";

    private FirebaseQueryObject fbQueryObj;
    private long initialLotteryCount;

    public NotificationService() {
        super("NotificationService");
        initialLotteryCount = -1;
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(LOG_TAG, "Firebase authentication succeeded.");
                } else {
                    Log.e(LOG_TAG, "Firebase authentication failed. ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (fbQueryObj != null) {
            Log.i(LOG_TAG, "Firebase query already active.");
            return;
        }

        fbQueryObj = new FirebaseQueryObject();
        fbQueryObj.query = FirebaseDatabase.getInstance().getReference(LotteriesScheme.LABEL);
        fbQueryObj.query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                initialLotteryCount = dataSnapshot.getChildrenCount();
                Log.i(LOG_TAG, String.format("%d initial lotteries at service start.", initialLotteryCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "Counting initial lotteries failed: ", databaseError.toException());
            }
        });

        // Wait until the number of initial lotteries has been determined
        while (initialLotteryCount == -1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        fbQueryObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Don't notify about additions if it is still among the initial lotteries
                if (initialLotteryCount > 0) {
                    --initialLotteryCount;
                    return;
                }

                Lottery lottery = dataSnapshot.getValue(Lottery.class);
                lottery.setId(dataSnapshot.getKey());
                Log.i(LOG_TAG, String.format("onChildAdded %s", (String)lottery.getId()));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // ignore
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // ignore
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // ignore
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "Firebase query failed. ", databaseError.toException());
            }
        };

        Log.i(LOG_TAG, "Starting firebase listener.");
        fbQueryObj.query.addChildEventListener(fbQueryObj.listener);

        // Don't let the call return or the service stops
        while (true) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (fbQueryObj != null) {
            Log.i(LOG_TAG, "Removing firebase listener.");
            fbQueryObj.query.removeEventListener(fbQueryObj.listener);
        }
        Log.i(LOG_TAG, "Destroying service.");
        super.onDestroy();
    }
}
