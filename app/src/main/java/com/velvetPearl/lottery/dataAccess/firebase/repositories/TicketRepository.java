package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.repositories.ITicketRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.TicketsScheme;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class TicketRepository extends FirebaseRepository implements ITicketRepository {

    private static final String LOG_TAG = "TicketRepository";

    public TicketRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }


    @Override
    public Ticket getTicket(Object id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ArrayList<Ticket> getTicketsForLottery(Object lotteryId)  {
        //authenticate();

        if (lotteryId == null || lotteryId.getClass() != String.class) {
            return null;
        }


        //authenticate();
        query = dbContext.getReference(TicketsScheme.LABEL).orderByChild(TicketsScheme.Children.LOTTERY_ID).equalTo((String)lotteryId);
        entityListener = attachEntityListener(query, null);

//        synchronized (lock) {
//            Log.d(LOG_TAG, "locking ticket repo");
//
//            try {
//                unlockedByNotify = false;
//                lock.wait(LOCK_TIMEOUT_MS);
//            } catch (InterruptedException e) {
//                Log.w(LOG_TAG, "getLottery data fetch sleep interrupted", e);
//            }
//        }
//        Log.d(LOG_TAG, "unlocked ticket repo");
//        verifyAsyncTask();

//        return ApplicationDomain.getInstance().getActiveLottery().getTickets();


//        final ArrayList<Ticket> result = new ArrayList<>();
//        authenticate();
//        dbContext.getReference(TicketsScheme.LABEL)
//                .equalTo((String) lotteryId, TicketsScheme.Children.LOTTERY_ID)
//                .addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                synchronized (lock) {
//                    for (DataSnapshot entity : dataSnapshot.getChildren()) {
//                        Ticket ticket = entity.getValue(Ticket.class);
//                        ticket.setId(entity.getKey());
//                        result.add(ticket);
//                    }
//                    unlockedByNotify = true;
//                    lock.notify();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(LOG_TAG, "getTicketsForLottery:onCancelled canceled data fetch", databaseError.toException());
//            }
//        });
//
//        synchronized (lock) {
//            try {
//                unlockedByNotify = false;
//                lock.wait(LOCK_TIMEOUT_MS);
//            } catch (InterruptedException e) {
//                Log.w(LOG_TAG, "getTicketsForLottery wait on data fetch interrupted", e);
//            }
//        }
//        verifyAsyncTask();
//
//        return result;
    }

    @Override
    public Ticket saveTicket(Ticket ticket) throws TimeoutException {
        throw new UnsupportedOperationException("Not implemented");

//        if (ticket == null) {
//            return null;
//        }
//        authenticate();
//
//        DatabaseReference dbObjRef = null;
//        if (ticket.getId() != null && !((String)ticket.getId()).isEmpty()) {
//            Log.d(LOG_TAG, "saveTicket: updating existing ticket with ID " + ticket.getId());
//            dbObjRef = dbContext.getReference(TicketsScheme.LABEL).child((String)ticket.getId());
//        } else {
//            Log.d(LOG_TAG, "saveTicket: saving new ticket");
//            dbObjRef = dbContext.getReference(TicketsScheme.LABEL).push();
//            // Set the ID that was automatically assigned so that the returned model obj can reference
//            // future db entity.
//            ticket.setId(dbObjRef.getKey());
//        }
//
//        HashMap<String, Object> objMap = new HashMap<>();
//        objMap.put(TicketsScheme.Children.OWNER,ticket.getOwner());
//        objMap.put(TicketsScheme.Children.LOTTERY_ID,ticket.getLotteryId());
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
//                Log.w(LOG_TAG, "saveTicket: waiting on save action interrupted", e);
//            }
//        }
//        verifyAsyncTask();
//
//        return ticket;
    }

    @Override
    protected ValueEventListener attachEntityListener(Query query, String entityId) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                synchronized (lock) {
                    ArrayList<Ticket> tickets = ApplicationDomain.getInstance().getActiveLottery().getTickets();
                    for (DataSnapshot entry : dataSnapshot.getChildren()) {
                        Ticket ticket = entry.getValue(Ticket.class);
                        ticket.setId(entry.getKey());

                        int existingIndex = tickets.indexOf(ticket);
                        if (existingIndex > -1) {
                            Log.d(LOG_TAG, "updating ticket with ID " + ticket.getId());
                            tickets.set(existingIndex, ticket);
                        } else {
                            Log.d(LOG_TAG, "adding ticket with ID " + ticket.getId());
                            tickets.add(ticket);
                        }
                    }
                    ApplicationDomain.getInstance().setModelChanged();
                    ApplicationDomain.getInstance().notifyObservers(DataAccessEvent.TICKET_LIST_UPDATED);
//                    unlockedByNotify = true;
//                    lock.notify();
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "Cancelled data sync", databaseError.toException());
            }
        };

        query.addValueEventListener(listener);
        return listener;
    }
}
