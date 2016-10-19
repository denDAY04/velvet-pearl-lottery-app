package com.velvetPearl.lottery.dataAccess;

import com.google.firebase.database.FirebaseDatabase;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.LotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.LotteryRepository;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.PrizeRepository;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.TicketRepository;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryRepository;
import com.velvetPearl.lottery.dataAccess.repositories.IPrizeRepository;
import com.velvetPearl.lottery.dataAccess.repositories.ITicketRepository;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Singleton class for application-wide access to the data entity manager objects.
 */
public class ApplicationDomain extends Observable {

    private static ApplicationDomain instance = null;

    public final ILotteryRepository lotteryRepository;
    public final ITicketRepository ticketRepository;
    public final ILotteryNumberRepository lotteryNumberRepository;
    public final IPrizeRepository prizeRepository;
    private Lottery activeLottery = null;
    private ArrayList<Lottery> allLotteries = null;


    /**
     * Get reference to the singleton object instance.
     * @return reference to the singleton instance.
     */
    public static ApplicationDomain getInstance() {
        if (instance == null) {
            instance = new ApplicationDomain();
        }
        return instance;
    }

    private ApplicationDomain() {
        FirebaseDatabase dbContext = FirebaseDatabase.getInstance();

        lotteryRepository = new LotteryRepository(dbContext);
        ticketRepository = new TicketRepository(dbContext);
        lotteryNumberRepository = new LotteryNumberRepository(dbContext);
        prizeRepository = new PrizeRepository(dbContext);
    }

    /**
     * Get the active lottery.
     * @return the active lottery, or null if setActiveLottery has not yet been called.
     */
    public Lottery getActiveLottery() {
        return activeLottery;
    }

    /**
     * Store the active lottery to be processed in the singleton.
     * @param activeLottery the lottery to be processed.
     */
    public void setActiveLottery(Lottery activeLottery) {
        this.activeLottery = activeLottery;
    }

    public ArrayList<Lottery> getAllLotteries() {
        return allLotteries;
    }

    public void setAllLotteries(ArrayList<Lottery> lotteries) {
        allLotteries = lotteries;
    }

    /**
     * Set the observable state of the model to changed and notify all observers with the argument.
     * @param arg The argument that will be passed to all observers.
     */
    public void broadcastChange(Object arg) {
        setChanged();
        notifyObservers(arg);
    }

}
