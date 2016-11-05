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
    public void startPrizeSyncForLotteryNumber(final Object numberId) {
        if (numberId == null) {
            return;
        }

        FirebaseQueryObject qObj = new FirebaseQueryObject();
        qObj.query = dbContext.getReference(PrizesScheme.LABEL).orderByChild(PrizesScheme.Children.NUMBER_ID).equalTo((String) numberId);
        qObj.listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // A prize with ticketId field was read, so sync by setting the prize reference of the associated lottery number and add it to the collection of prizes
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());
                Log.d(LOG_TAG, String.format("Prize (ID %s) loaded with lotteryId.", prize.getId(), numberId));

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    lottery.addPrize(prize);
                    Map<Object, Ticket> tickets = lottery.getTickets();
                    for (Object ticketId : tickets.keySet()) {
                        Ticket ticket = tickets.get(ticketId);
                        for (LotteryNumber number : ticket.getLotteryNumbers()) {
                            if (number.getId().equals(prize.getNumberId())) {
                                number.setWinningPrize(prize);
                                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // ticketId field was changed, so sync the changes by setting the prize reference to null on the past associated lottery number
                // and update the reference on the newly associated number
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());
                Log.d(LOG_TAG, String.format("lotteryId changed for prize (ID %s).", prize.getId(), numberId));

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Map<Object, Ticket> tickets = lottery.getTickets();
                    boolean removedOldReference = false;
                    boolean setNewReference = false;
                    for (Object ticketId : tickets.keySet()) {
                        Ticket ticket = tickets.get(ticketId);
                        for (LotteryNumber number : ticket.getLotteryNumbers()) {
                            if (number.getWinningPrize() != null && number.getWinningPrize().getId().equals(prize.getId())) {
                                number.setWinningPrize(null);
                                removedOldReference = true;
                            }
                            if (number.getId().equals(prize.getNumberId())) {
                                number.setWinningPrize(prize);
                                setNewReference = true;
                            }

                            if (removedOldReference && setNewReference) break;
                        }
                        if (removedOldReference && setNewReference) break;
                    }
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // ticketId field was removed, so sync the changes by setting the prize reference to null on the past associated lottery number
                Object prizeId = dataSnapshot.getKey();
                Log.d(LOG_TAG, String.format("lotteryId removed for prize (ID %s).", prizeId, numberId));

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Map<Object, Ticket> tickets = lottery.getTickets();
                    boolean done = false;
                    for (Object ticketId : tickets.keySet()) {
                        Ticket ticket = tickets.get(ticketId);
                        for (LotteryNumber number : ticket.getLotteryNumbers()) {
                            if (number.getWinningPrize() != null && number.getWinningPrize().getId().equals(prizeId)) {
                                number.setWinningPrize(null);
                                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                return; //Ignore
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "startPrizeSyncForLotteryNumber cancelled.", databaseError.toException());
            }
        };

        attachAndStoreQueryObject((String) numberId, qObj);
    }

    @Override
    public void stopPrizeSyncForLotteryNumber(Object numberId) {
        if (numberId != null) {
            Log.d(LOG_TAG, String.format("Detaching prize listener for lottery number %s.", numberId));
            detatchEntityListener((String) numberId);
        }
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
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null) {
                    return;
                }
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                // Ignore prizes that have been won and thus have a lottery number reference
                if (prize.getNumberId() == null) {
                    Log.d(LOG_TAG, String.format("Prize (ID %s) added.", prize.getId()));
                    lottery.addPrize(prize);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null) {
                    return;
                }

                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());
                Log.d(LOG_TAG, String.format("Prize (ID %s) changed.", prize.getId()));

                lottery.changePrize(prize);

                // If change was a number winning the prize, set the prize reference in the number
                if (prize.getNumberId() != null) {
                    Map<Object, Ticket> tickets = lottery.getTickets();
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
                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery == null) {
                    return;
                }

                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Log.d(LOG_TAG, String.format("Prize (ID %s) removed.", prize.getId()));
                lottery.removePrize((String) prize.getId());
                ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
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
