package com.velvetPearl.lottery.dataAccess.firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.INumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.NumbersScheme;
import com.velvetPearl.lottery.dataAccess.models.Number;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class NumberRepository extends FirebaseRepository implements INumberRepository {

    private static final String LOG_TAG = "TicketRepository";


    @Override
    public ArrayList<Number> getNumbersForTicket(Object ticketId) throws TimeoutException {
        if (ticketId == null || ticketId.getClass() != String.class) {
            return null;
        }

        final ArrayList<Number> result = new ArrayList<>();
        authenticate();
        dbContext.getReference(NumbersScheme.LABEL)
                .equalTo((String) ticketId, NumbersScheme.Children.TICKET_ID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        synchronized (lock) {
                            for (DataSnapshot entity : dataSnapshot.getChildren()) {
                                Number number = entity.getValue(Number.class);
                                number.setId(entity.getKey());
                                result.add(number);
                            }
                            unlockedByNotify = true;
                            lock.notify();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(LOG_TAG, "getNumbersForTicket:onCancelled canceled data fetch", databaseError.toException());
                    }
                });

        synchronized (lock) {
            try {
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "getNumbersForTicket wait on data fetch interrupted", e);
            }
        }
        verifyAsyncTask();

        return result;
    }
}
