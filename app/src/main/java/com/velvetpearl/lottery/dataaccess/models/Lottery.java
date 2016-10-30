package com.velvetPearl.lottery.dataAccess.models;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Lottery {
    private Object id;
    private long created;   /* In Unix time. */
    private double pricePerLotteryNum;
    private int lotteryNumLowerBound;
    private int lotteryNumUpperBound;

    private TreeMap<Object, Prize> prizes;
    //private List<Ticket> tickets;
    private TreeMap<Object, Ticket> tickets;
    //private ArrayList<LotteryNumber> lotteryNumbers;

    public Lottery() {
        tickets = new TreeMap<>();
        prizes = new TreeMap<>();
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public double getPricePerLotteryNum() {
        return pricePerLotteryNum;
    }

    public void setPricePerLotteryNum(double pricePerLotteryNum) {
        this.pricePerLotteryNum = pricePerLotteryNum;
    }

    public int getLotteryNumLowerBound() {
        return lotteryNumLowerBound;
    }

    public void setLotteryNumLowerBound(int lotteryNumLowerBound) {
        if (lotteryNumLowerBound < 1)
            throw new InvalidParameterException("Lower number boundary must be ");
        this.lotteryNumLowerBound = lotteryNumLowerBound;
    }

    public int getLotteryNumUpperBound() {
        return lotteryNumUpperBound;
    }

    public void setLotteryNumUpperBound(int lotteryNumUpperBound) {
        this.lotteryNumUpperBound = lotteryNumUpperBound;
    }

    public TreeMap<Object, Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(TreeMap<Object, Prize> prizes) {
        this.prizes = prizes;
    }

    public void addPrize(Prize prize) {
        if (prize == null) {
            return;
        }

        if (prize.getId() == null) {
            throw new IllegalArgumentException("Prize ID is null.");
        }

        prizes.put(prize.getId(), prize);
    }

    public TreeMap<Object, Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(TreeMap<Object, Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        if (ticket == null) {
            return;
        }

        if (ticket.getId() == null) {
            throw new IllegalArgumentException("Ticket ID is null.");
        }

        tickets.put(ticket.getId(), ticket);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Lottery ID %s", (String)id));
        sb.append(String.format(", created %d", created));
        sb.append(String.format(", price pr. num %.2f", pricePerLotteryNum));
        sb.append(String.format(", num bounds {low, high} {%d, %d}", lotteryNumLowerBound, lotteryNumUpperBound));
        if (tickets != null) {
            sb.append(String.format(", #tickets %d", tickets.size()));
            int lotteryNumberCount = 0;
            for (Object key : tickets.keySet()) {
                Ticket ticket = tickets.get(key);
                lotteryNumberCount += ticket.getLotteryNumbers().size();
            }
            sb.append(String.format(", #lotteryNumbers %d" , lotteryNumberCount));
        } else {
            sb.append(", #tickets 0, #lotteryNumbers 0");
        }
        sb.append(String.format(", #prizes %d", prizes != null ? prizes.size() : 0));

        return sb.toString();
    }
}
