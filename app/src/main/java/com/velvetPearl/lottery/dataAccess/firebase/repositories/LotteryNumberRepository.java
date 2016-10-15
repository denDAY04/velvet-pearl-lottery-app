package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteryNumbersScheme;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class LotteryNumberRepository extends FirebaseRepository implements ILotteryNumberRepository {

    private static final String LOG_TAG = "TicketRepository";

    public LotteryNumberRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }


    @Override
    public void getLotteryNumbersForTicket(final Object ticketId) {
        if (ticketId == null || ticketId.getClass() != String.class) {
            return;
        }

        Query query = dbContext.getReference(LotteryNumbersScheme.LABEL).orderByChild(LotteryNumbersScheme.Children.TICKET_ID).equalTo((String)ticketId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null || lottery.getTickets() == null || lottery.getTickets().isEmpty()) {
                    Log.e(LOG_TAG, String.format("getLotteryNumbersForTicket data sync error: [lottery == null]->%b [tickets == null]->%b [tickets isEmpty]->&b", lottery == null, lottery.getTickets() == null, lottery.getTickets() == null ? true : lottery.getTickets().isEmpty()));
                    return;
                }

                ArrayList<Ticket> tickets = lottery.getTickets();
                for (DataSnapshot entity : dataSnapshot.getChildren()) {
                    LotteryNumber number = entity.getValue(LotteryNumber.class);
                    number.setId(entity.getKey());

                    for (int i = 0; i < tickets.size(); ++i) {
                        if (tickets.get(i).getId().equals(ticketId)) {
                            Ticket ticket = tickets.get(i);
                            int existingIndex = ticket.getLotteryNumbers().indexOf(number);
                            if (existingIndex != -1) {
                                Log.d(LOG_TAG, "updating existing lottery number ID " + number.getId());
                                ticket.getLotteryNumbers().set(existingIndex, number);
                            } else {
                                Log.d(LOG_TAG, "adding new lottery number ID " + number.getId());
                                ticket.getLotteryNumbers().add(number);
                            }
                            break;
                        }
                    }
                }
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.TICKET_LIST_UPDATED);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "getLotteryNumbersForTicket data sync canceled");
            }
        };

        attachAndStoreEntityListener((String)ticketId, query, listener);
    }

    @Override
    public LotteryNumber saveLotteryNumber(LotteryNumber lotteryNumber) {
//        if (lotteryNumber == null) {
//            return null;
//        }
//        //authenticate();
//
//        DatabaseReference dbObjRef = null;
//        if (lotteryNumber.getId() != null && !((String)lotteryNumber.getId()).isEmpty()) {
//            Log.d(LOG_TAG, "saveLotteryNumber: updating existing lottery number with ID " + lotteryNumber.getId());
//            dbObjRef = dbContext.getReference(LotteryNumbersScheme.LABEL).child((String)lotteryNumber.getId());
//        } else {
//            Log.d(LOG_TAG, "saveLotteryNumber: saving new lottery number");
//            dbObjRef = dbContext.getReference(LotteryNumbersScheme.LABEL).push();
//            // Set the ID that was automatically assigned so that the returned model obj can reference
//            // future db entity.
//            lotteryNumber.setId(dbObjRef.getKey());
//        }
//
//        HashMap<String, Object> objMap = new HashMap<>();
//        objMap.put(LotteryNumbersScheme.Children.LOTTERY_NUMBER,lotteryNumber.getLotteryNumber());
//        objMap.put(LotteryNumbersScheme.Children.TICKET_ID,lotteryNumber.getTicketId());
//        dbObjRef.setValue(objMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                synchronized (lock) {
//                    unlockedByNotify = true;
//                    lock.notify();
//                }
//            }
//        });
//
//        synchronized (lock) {
//            try {
//                unlockedByNotify = false;
//                lock.wait(LOCK_TIMEOUT_MS);
//            } catch (InterruptedException e) {
//                Log.w(LOG_TAG, "saveLotteryNumber: waiting on save action interrupted", e);
//            }
//        }
//        verifyAsyncTask();
//
//        return lotteryNumber;
        return null;
    }

    @Override
    protected ValueEventListener attachEntityListener(Query query, String entityId) {

        return null;
    }

    @Override
    public void clearState() {
        removeAllEntityListeners();
    }
}
