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
import com.velvetPearl.lottery.dataAccess.repositories.ITicketRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.TicketsScheme;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.TreeMap;
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
    public void loadTicket(Object id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void loadTicketsForLottery(Object lotteryId) {
        if (lotteryId == null || lotteryId.getClass() != String.class) {
            return ;
        }

        FirebaseQueryObject qObj = new FirebaseQueryObject();
        qObj.query = dbContext.getReference(TicketsScheme.LABEL).orderByChild(TicketsScheme.Children.LOTTERY_ID).equalTo((String)lotteryId);
        qObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Ticket ticket = dataSnapshot.getValue(Ticket.class);
                ticket.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Log.d(LOG_TAG, String.format("Ticket (ID %s) added.", ticket.getId()));
                    lottery.getTickets().put(ticket.getId(), ticket);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.TICKET_LIST_UPDATE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Ticket ticket = dataSnapshot.getValue(Ticket.class);
                ticket.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Log.d(LOG_TAG, String.format("Ticket (ID %s) changed.", ticket.getId()));
                    lottery.getTickets().put(ticket.getId(), ticket);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.TICKET_LIST_UPDATE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Ticket ticket = dataSnapshot.getValue(Ticket.class);
                ticket.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Log.d(LOG_TAG, String.format("Ticket (ID %s) removed.", ticket.getId()));
                    lottery.getTickets().remove(ticket.getId());
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.TICKET_LIST_UPDATE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return; // ignore
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "loadTicketsForLottery cancelled", databaseError.toException());
            }
        };
        attachAndStoreQueryObject((String)lotteryId, qObj);
    }

    @Override
    public Ticket saveTicket(Ticket ticket) throws TimeoutException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
