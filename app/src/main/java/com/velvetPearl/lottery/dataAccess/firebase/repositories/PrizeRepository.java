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
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.IPrizeRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.PrizesScheme;
import com.velvetPearl.lottery.dataAccess.models.Prize;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class PrizeRepository extends FirebaseRepository implements IPrizeRepository {

    private static final String LOG_TAG = "PrizeRepository";

    public PrizeRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }

    @Override
    public void loadPrizeForNumber(final Object numberId) {
        if (numberId == null) {
            return;
        }

        FirebaseQueryObject qObj = new FirebaseQueryObject();
        qObj.query = dbContext.getReference(PrizesScheme.LABEL).orderByChild(PrizesScheme.Children.NUMBER_ID).equalTo((String) numberId);
        qObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Log.d(LOG_TAG, String.format("Prize (ID %s) added for number (ID %s).", prize.getId(), numberId));
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                Map<Object, Ticket> tickets = lottery.getTickets();
                boolean done = false;
                for (Object ticketId : tickets.keySet()) {
                    Ticket ticket = tickets.get(ticketId);
                    for (LotteryNumber number : ticket.getLotteryNumbers()) {
                        if (number.getId().equals(prize.getNumberId())) {
                            number.setWinningPrize(prize);
                            done = true;
                            break;
                        }
                    }
                    if (done) break;
                }
                lottery.addPrize(prize);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Log.d(LOG_TAG, String.format("Prize (ID %s) changed for number (ID %s).", prize.getId(), numberId));
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                Map<Object, Ticket> tickets = lottery.getTickets();
                boolean done = false;
                for (Object ticketId : tickets.keySet()) {
                    Ticket ticket = tickets.get(ticketId);
                    for (LotteryNumber number : ticket.getLotteryNumbers()) {
                        // Check whether the change was the prize being moved to another number,
                        // and if so, update the old number's prize reference.
                        if (number.getId().equals(numberId) && !numberId.equals(prize.getNumberId())) {
                            number.setWinningPrize(null);
                        } else if (number.getId() == prize.getNumberId()) {
                            number.setWinningPrize(prize);
                            done = true;
                            break;
                        }
                    }
                    if (done) break;
                }
                lottery.addPrize(prize);
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Log.d(LOG_TAG, String.format("Prize (ID %s) removed for number (ID %s).", prize.getId(), numberId));
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                Map<Object, Ticket> tickets = lottery.getTickets();
                boolean done = false;
                for (Object ticketId : tickets.keySet()) {
                    Ticket ticket = tickets.get(ticketId);
                    for (LotteryNumber number : ticket.getLotteryNumbers()) {
                        if (number.getId().equals(prize.getNumberId())) {
                            number.setWinningPrize(null);
                            done = true;
                            break;
                        }
                    }
                    if (done) break;
                }
                lottery.removePrize(prize.getId());
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return; //Ignore
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "loadPrizeForNumber cancelled.", databaseError.toException());
            }
        };

        attachAndStoreQueryObject((String) numberId, qObj);
    }

    @Override
    public void loadAvailablePrizesForLottery(Object lotteryId) {
        if (lotteryId == null) {
            return;
        }

        FirebaseQueryObject qObj = new FirebaseQueryObject();
        qObj.query = dbContext.getReference(PrizesScheme.LABEL).orderByChild(PrizesScheme.Children.LOTTERY_ID).equalTo((String) lotteryId);
        qObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                // Ignore prizes that have been won and thus have a lottery number reference
                if (prize.getNumberId() == null) {
                    Log.d(LOG_TAG, String.format("Prize (ID %s) added.", prize.getId()));
                    ApplicationDomain.getInstance().getActiveLottery().addPrize(prize);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Log.d(LOG_TAG, String.format("Prize (ID %s) changed.", prize.getId()));
                ApplicationDomain.getInstance().getActiveLottery().addPrize(prize);

                // If change was a number winning the prize, set the prize reference in the number
                if (prize.getNumberId() != null) {
                    Map<Object, Ticket> tickets = ApplicationDomain.getInstance().getActiveLottery().getTickets();
                    boolean done = false;
                    for (Object ticketId : tickets.keySet()) {
                        Ticket ticket = tickets.get(ticketId);
                        for (LotteryNumber number : ticket.getLotteryNumbers()) {
                            if (number.getId() == prize.getNumberId()) {
                                number.setWinningPrize(prize);
                                done = true;
                                break;
                            }
                        }
                        if (done) break;
                    }
                }
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Log.d(LOG_TAG, String.format("Prize (ID %s) removed.", prize.getId()));
                    lottery.removePrize(prize.getId());
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return; // Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "loadAvailablePrizesForLottery cancelled.", databaseError.toException());
            }
        };

        attachAndStoreQueryObject((String) lotteryId, qObj);
    }

    @Override
    public Prize savePrize(Prize prize) {
        if (prize == null) {
            return null;
        }

        DatabaseReference dbPrize;
        if (prize.getId() != null && !((String) prize.getId()).isEmpty()) {
            Log.d(LOG_TAG, String.format("Saving existing prize (ID %s).", prize.getId()));
            dbPrize = dbContext.getReference(PrizesScheme.LABEL).child((String) prize.getId());
        } else {
            Log.d(LOG_TAG, "Saving new prize.");
            dbPrize = dbContext.getReference(PrizesScheme.LABEL).push();
            prize.setId(dbPrize.getKey());
        }

        HashMap<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(PrizesScheme.Children.NAME, prize.getName());
        fieldValues.put(PrizesScheme.Children.NUMBER_ID, prize.getNumberId());
        fieldValues.put(PrizesScheme.Children.LOTTERY_ID, prize.getLotteryId());
        dbPrize.setValue(fieldValues);

        return prize;
    }

    @Override
    public void deletePrize(Prize entity) {
        if (entity == null ||  entity.getId() == null) {
            return;
        }

        Log.d(LOG_TAG, String.format("Removing prize (ID %s).", entity.getId()));
        dbContext.getReference(PrizesScheme.LABEL).child((String) entity.getId()).removeValue();
    }

}
