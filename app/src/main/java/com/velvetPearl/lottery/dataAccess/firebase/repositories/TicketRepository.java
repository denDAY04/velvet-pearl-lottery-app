package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.firebase.FirebaseQueryObject;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.repositories.ITicketRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.TicketsScheme;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
                    lottery.addTicket(ticket);
                    ApplicationDomain.getInstance().lotteryNumberRepository.fetchLotteryNumbersForTicket(ticket.getId());
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
                    ApplicationDomain.getInstance().lotteryNumberRepository.fetchLotteryNumbersForTicket(ticket.getId());
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
    public Ticket saveTicket(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        DatabaseReference dbTicketRef;
        if (ticket.getId() != null && !((String) ticket.getId()).isEmpty()) {
            Log.d(LOG_TAG, String.format("Saving existing ticket (ID %s)", (String) ticket.getId()));
            dbTicketRef = dbContext.getReference(TicketsScheme.LABEL).child((String) ticket.getId());
        } else {
            Log.d(LOG_TAG, "Saving new ticket.");
            dbTicketRef = dbContext.getReference(TicketsScheme.LABEL).push();
            ticket.setId(dbTicketRef.getKey());
        }

        for (LotteryNumber number : ticket.getLotteryNumbers()) {
            number.setTicketId(ticket.getId());
            ApplicationDomain.getInstance().lotteryNumberRepository.saveLotteryNumber(number);
        }

        HashMap<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TicketsScheme.Children.OWNER, ticket.getOwner());
        fieldValues.put(TicketsScheme.Children.LOTTERY_ID, ticket.getLotteryId());

        dbTicketRef.setValue(fieldValues);

        return ticket;
    }

    @Override
    public void deleteTicket(Ticket entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        // Remove all of the lottery numbers associated with the ticket
        for (LotteryNumber number : entity.getLotteryNumbers()) {
            ApplicationDomain.getInstance().lotteryNumberRepository.deleteLotteryNumber(number);
        }

        dbContext.getReference(TicketsScheme.LABEL).child((String) entity.getId()).removeValue();
    }

}
