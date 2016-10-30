package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.IPrizeRepository;
import com.velvetPearl.lottery.dataAccess.firebase.scheme.PrizesScheme;
import com.velvetPearl.lottery.dataAccess.models.Prize;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public class PrizeRepository extends FirebaseRepository implements IPrizeRepository {

    private static final String LOG_TAG = "PrizeRepository";

    public PrizeRepository(FirebaseDatabase dbContext) {
        super(dbContext);
    }

    @Override
    public Prize getPrizeForNumber(Object numberId) {
        return null;
    }

    @Override
    public void loadPrizesForLottery(Object lotteryId) {
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

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Log.d(LOG_TAG, String.format("Prize (ID %s) added.", prize.getId()));
                    lottery.addPrize(prize);

                    if (prize.getLotteryNumberId() != null) {
                        TreeMap<Object, Ticket> tickets = ApplicationDomain.getInstance().getActiveLottery().getTickets();
                        for (Object ticketId : tickets.keySet()) {
                            Ticket ticket = tickets.get(ticketId);
                            for (LotteryNumber number : ticket.getLotteryNumbers()) {
                                if (number.getId().equals(prize.getLotteryNumberId())) {
                                    number.setWinningPrize(prize);
                                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                                    return;
                                }
                            }
                        }
                    }

                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Prize prize = dataSnapshot.getValue(Prize.class);
                prize.setId(dataSnapshot.getKey());

                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
                if (lottery != null) {
                    Log.d(LOG_TAG, String.format("Prize (ID %s) changed.", prize.getId()));
                    lottery.addPrize(prize);
                    ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.PRIZE_UPDATE);
                }
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
                Log.w(LOG_TAG, "loadPrizesForLottery cancelled.", databaseError.toException());
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
        fieldValues.put(PrizesScheme.Children.NUMBER_ID, prize.getLotteryNumberId());
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
