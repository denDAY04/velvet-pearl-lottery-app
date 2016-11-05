package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.firebase.FirebaseQueryObject;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.LotteryNumbersScheme;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class LotteryNumberRepository extends FirebaseRepository implements ILotteryNumberRepository {

    private static final String LOG_TAG = "TicketRepository";

    public LotteryNumberRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }


    @Override
    public void startLotteryNumbersSyncForTicket(final Object ticketId) {
        if (ticketId == null || ticketId.getClass() != String.class) {
            return;
        }

        FirebaseQueryObject qObj = new FirebaseQueryObject();
        qObj.query = dbContext.getReference(LotteryNumbersScheme.LABEL).orderByChild(LotteryNumbersScheme.Children.TICKET_ID).equalTo((String)ticketId);
        qObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null) {
                    return;
                }

                Map<Object, Ticket> tickets = lottery.getTickets();
                if (tickets == null) {
                    return;
                }

                LotteryNumber lotteryNum = dataSnapshot.getValue(LotteryNumber.class);
                lotteryNum.setId(dataSnapshot.getKey());
                Log.d(LOG_TAG, String.format("LotteryNumber (ID %s) added.", lotteryNum.getId()));


                Ticket ticket = tickets.get(lotteryNum.getTicketId());
                ticket.addLotteryNumber(lotteryNum);
                ApplicationDomain.getInstance().prizeRepository.startPrizeSyncForLotteryNumber(lotteryNum.getId());
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null) {
                    return;
                }

                Map<Object, Ticket> tickets = lottery.getTickets();
                if (tickets == null) {
                    return;
                }

                LotteryNumber lotteryNum = dataSnapshot.getValue(LotteryNumber.class);
                lotteryNum.setId(dataSnapshot.getKey());
                Log.d(LOG_TAG, String.format("LotteryNumber (ID %s) changed.", lotteryNum.getId()));

                Ticket ticket = tickets.get(lotteryNum.getTicketId());
                ticket.addLotteryNumber(lotteryNum);
                ApplicationDomain.getInstance().prizeRepository.startPrizeSyncForLotteryNumber(lotteryNum.getId());
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null) {
                    return;
                }

                Map<Object, Ticket> tickets = lottery.getTickets();
                if (tickets == null) {
                    return;
                }

                LotteryNumber lotteryNum = dataSnapshot.getValue(LotteryNumber.class);
                lotteryNum.setId(dataSnapshot.getKey());
                Log.d(LOG_TAG, String.format("LotteryNumber (ID %s) removed.", lotteryNum.getId()));

               tickets.get(lotteryNum.getTicketId()).removeLotteryNumber(lotteryNum);
                ApplicationDomain.getInstance().prizeRepository.stopPrizeSyncForLotteryNumber(lotteryNum.getId());
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return; // ignore
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "startLotteryNumbersSyncForTicket cancelled", databaseError.toException());
            }
        };
        attachAndStoreQueryObject((String)ticketId, qObj);
    }

    @Override
    public void deleteLotteryNumber(LotteryNumber entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        // Remove the prize associated with the lottery number
        if (entity.getWinningPrize() != null) {
            Prize prize = entity.getWinningPrize();
            prize.setNumberId( null);
            ApplicationDomain.getInstance().prizeRepository.savePrize(prize);
        }

        dbContext.getReference(LotteryNumbersScheme.LABEL).child((String) entity.getId()).removeValue();
    }

    @Override
    public void stopLotteryNumbersSyncForTicket(Object ticketId) {
        if (ticketId != null) {
            Log.d(LOG_TAG, String.format("Detaching lottery number listener for ticket ID %s.",(String) ticketId));
            detatchEntityListener((String) ticketId);
        }
    }

    @Override
    public LotteryNumber saveLotteryNumber(LotteryNumber lotteryNumber) {
        if (lotteryNumber == null) {
            return null;
        }

        // Get reference to existing db entity or push a new one, saving the ID
        DatabaseReference dbLotteryNumRef;
        if (lotteryNumber.getId() != null && !((String) lotteryNumber.getId()).isEmpty()) {
            Log.d(LOG_TAG, String.format("Saving existing lottery number (ID %s)", lotteryNumber.getId()));
            dbLotteryNumRef = dbContext.getReference(LotteryNumbersScheme.LABEL).child((String) lotteryNumber.getId());
        } else {
            Log.d(LOG_TAG, "Saving new lottery number");
            dbLotteryNumRef = dbContext.getReference(LotteryNumbersScheme.LABEL).push();
            lotteryNumber.setId(dbLotteryNumRef.getKey());
        }

        // Save the number's prize
        Prize prize = lotteryNumber.getWinningPrize();
        if (prize != null) {
            prize.setNumberId(lotteryNumber.getId());
            ApplicationDomain.getInstance().prizeRepository.savePrize(prize);
        }

        // Save direct fields
        HashMap<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(LotteryNumbersScheme.Children.LOTTERY_NUMBER, lotteryNumber.getLotteryNumber());
        fieldValues.put(LotteryNumbersScheme.Children.PRICE, lotteryNumber.getPrice());
        fieldValues.put(LotteryNumbersScheme.Children.TICKET_ID, lotteryNumber.getTicketId());
        dbLotteryNumRef.setValue(fieldValues);

        return lotteryNumber;
    }


    @Override
    public void clearState() {
        detachAndRemoveAllQueryObjects();
    }
}
