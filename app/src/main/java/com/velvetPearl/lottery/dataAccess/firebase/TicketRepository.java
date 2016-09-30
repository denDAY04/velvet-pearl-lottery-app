package com.velvetPearl.lottery.dataAccess.firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ITicketRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.TicketsScheme;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class TicketRepository extends FirebaseRepository implements ITicketRepository {

    private static final String LOG_TAG = "TicketRepository";

    @Override
    public Ticket getTicket(Object id) throws TimeoutException {
        return null;
    }

    @Override
    public ArrayList<Ticket> getTicketsForLottery(Object lotteryId) throws TimeoutException {
        if (lotteryId == null || lotteryId.getClass() != String.class) {
            return null;
        }
        final ArrayList<Ticket> result = new ArrayList<>();
        authenticate();
        dbContext.getReference(TicketsScheme.LABEL)
                .equalTo((String) lotteryId, TicketsScheme.Children.LOTTERY_ID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                synchronized (lock) {
                    for (DataSnapshot entity : dataSnapshot.getChildren()) {
                        Ticket ticket = entity.getValue(Ticket.class);
                        ticket.setId(entity.getKey());
                        result.add(ticket);
                    }
                    unlockedByNotify = true;
                    lock.notify();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getTicketsForLottery:onCancelled canceled data fetch", databaseError.toException());
            }
        });

        synchronized (lock) {
            try {
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "getTicketsForLottery wait on data fetch interrupted", e);
            }
        }
        verifyAsyncTask();

        return result;
    }

    @Override
    public Ticket saveTicket(Ticket ticket) throws TimeoutException {
        return null;
    }
}
