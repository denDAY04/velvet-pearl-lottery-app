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
    public void getTicket(Object id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void getTicketsForLottery(Object lotteryId) {
        if (lotteryId == null || lotteryId.getClass() != String.class) {
            return ;
        }

        query = dbContext.getReference(TicketsScheme.LABEL).orderByChild(TicketsScheme.Children.LOTTERY_ID).equalTo((String)lotteryId);
        entityListener = attachEntityListener(query, null);
    }

    @Override
    public Ticket saveTicket(Ticket ticket) throws TimeoutException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected ValueEventListener attachEntityListener(Query query, String entityId) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.TICKET_LIST_UPDATED);
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
