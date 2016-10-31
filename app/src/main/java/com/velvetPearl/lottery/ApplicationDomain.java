package com.velvetPearl.lottery;

import com.google.firebase.database.FirebaseDatabase;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.LotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.LotteryRepository;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.PrizeRepository;
import com.velvetPearl.lottery.dataAccess.firebase.repositories.TicketRepository;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryNumberRepository;
import com.velvetPearl.lottery.dataAccess.repositories.ILotteryRepository;
import com.velvetPearl.lottery.dataAccess.repositories.IPrizeRepository;
import com.velvetPearl.lottery.dataAccess.repositories.ITicketRepository;
import com.velvetPearl.lottery.viewModels.TicketInputModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.TreeMap;

/**
 * Singleton class for application-wide access to the data entity manager objects.
 */
public class ApplicationDomain extends Observable {

    private static ApplicationDomain instance = null;

    public final ILotteryRepository lotteryRepository;
    public final ITicketRepository ticketRepository;
    public final ILotteryNumberRepository lotteryNumberRepository;
    public final IPrizeRepository prizeRepository;

    private Lottery activeLottery;
    private ArrayList<Lottery> allLotteries;

    private TicketInputModel editingTicket;
    private Prize editingPrize;
    private Prize prizeToBeWon;

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

        activeLottery = null;
        allLotteries = null;

        editingTicket = null;
        editingPrize = null;
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

    public TicketInputModel getEditingTicket() {
        return editingTicket;
    }

    public void resetEditingTicket() {
        editingTicket = new TicketInputModel();
    }

    public void setEditingTicketFromLottery(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            return;
        }

        editingTicket = new TicketInputModel(getActiveLottery().getTickets().get(ticketId));
    }

    public LinkedList<Integer> getUsedLotteryNumbers() {
        LinkedList<Integer> usedNumbers = new LinkedList<>();

        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        if (lottery != null) {
            TreeMap<Object, Ticket> tickets = lottery.getTickets();
            for (Object ticketKey : tickets.keySet()) {
                Ticket ticket = tickets.get(ticketKey);
                for (LotteryNumber number : ticket.getLotteryNumbers()) {
                    usedNumbers.add(number.getLotteryNumber());
                }
            }
        }

        for (LotteryNumber number : editingTicket.getUnsavedNumbers()) {
            usedNumbers.add(number.getLotteryNumber());
        }

        return usedNumbers;
    }

    public  boolean allLotteryNumbersTaken() {
        LinkedList<Integer>  takenNumbers = ApplicationDomain.getInstance().getUsedLotteryNumbers();
        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        int rangeCount = lottery.getLotteryNumUpperBound() - lottery.getLotteryNumLowerBound() + 1;     // Both bounds inclusive

        return takenNumbers.size() >= rangeCount;
    }

    public Prize getEditingPrize() { return editingPrize; }

    public void setEditingPrize(Prize prize) { editingPrize = prize; }

    public void resetEditingPrize() {
        editingPrize = new Prize();
        editingPrize.setLotteryId(ApplicationDomain.getInstance().getActiveLottery().getId());
    }

    public Prize getPrizeToBeWon() {
        return prizeToBeWon;
    }

    public void setPrizeToBeWon(Prize prizeToBeWon) {
        this.prizeToBeWon = prizeToBeWon;
    }
}
