package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.firebase.FirebaseQueryObject;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteryNumbersScheme;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;

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
    public void fetchLotteryNumbersForTicket(final Object ticketId) {
        if (ticketId == null || ticketId.getClass() != String.class) {
            return;
        }

        FirebaseQueryObject qObj = new FirebaseQueryObject();
        qObj.query = dbContext.getReference(LotteryNumbersScheme.LABEL).orderByChild(LotteryNumbersScheme.Children.TICKET_ID).equalTo((String)ticketId);
        qObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LotteryNumber lotteryNum = dataSnapshot.getValue(LotteryNumber.class);
                lotteryNum.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null && lottery.getTickets() != null) {
                    Log.d(LOG_TAG, String.format("LotteryNumber (ID %s) added.", lotteryNum.getId()));
                    Ticket ticket = lottery.getTickets().get(lotteryNum.getTicketId());
                    ticket.getLotteryNumbers().put(lotteryNum.getId(), lotteryNum);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                LotteryNumber lotteryNum = dataSnapshot.getValue(LotteryNumber.class);
                lotteryNum.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null && lottery.getTickets() != null) {
                    Log.d(LOG_TAG, String.format("LotteryNumber (ID %s) changed.", lotteryNum.getId()));
                    Ticket ticket = lottery.getTickets().get(lotteryNum.getTicketId());
                    ticket.getLotteryNumbers().put(lotteryNum.getId(), lotteryNum);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                LotteryNumber lotteryNum = dataSnapshot.getValue(LotteryNumber.class);
                lotteryNum.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null && lottery.getTickets() != null) {
                    Log.d(LOG_TAG, String.format("LotteryNumber (ID %s) removed.", lotteryNum.getId()));
                    Ticket ticket = lottery.getTickets().get(lotteryNum.getTicketId());
                    ticket.getLotteryNumbers().remove(lotteryNum.getId());
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return; // ignore
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "fetchLotteryNumbersForTicket cancelled", databaseError.toException());
            }
        };
        attachAndStoreQueryObject((String)ticketId, qObj);
    }

    @Override
    public void deleteLotteryNumber(Object lotteryNumberId) {
        // TODO Delete in firebase and fire event
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
    public void clearState() {
        detachAndRemoveAllQueryObjects();
    }
}
