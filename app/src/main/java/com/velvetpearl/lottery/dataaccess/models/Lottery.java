package com.velvetPearl.lottery.dataAccess.models;

import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;

import java.security.InvalidParameterException;
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
    private String name;
    private boolean ticketMultiWinEnabled;

    private TreeMap<Object, Prize> prizes;
    private TreeMap<Object, Ticket> tickets;

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

    public void removePrize(String prizeId) {
        if (prizeId == null) {
            return;
        }

        Prize prize = prizes.get(prizeId);
        if (prize == null) {
            return;
        }

        // In case the prize has been assigned to a lottery number, find the number and remove its reference to the prize
        if (prize.getNumberId() != null) {
            for (Object ticketId : tickets.keySet()) {
                Ticket ticket = tickets.get(ticketId);
                for (LotteryNumber number : ticket.getLotteryNumbers()) {
                    if (number.getId().equals(prize.getNumberId())) {
                        number.setWinningPrize(null);
                        prizes.remove(prizeId);
                        ApplicationDomain.getInstance().broadcastChange(DataAccessEvent.LOTTERY_NUMBER_UPDATE);
                        return;
                    }
                }
            }
        } else {
            prizes.remove(prizeId);
        }
    }

    public void changePrize(Prize newPrize) {
        if (newPrize == null) {
            return;
        }

        if (newPrize.getId() == null) {
            throw new IllegalArgumentException("New-Prize ID is null.");
        }

        if (prizes.containsKey(newPrize.getId())) {
            prizes.get(newPrize.getId()).copy(newPrize);
        } else {
            prizes.put(newPrize.getId(), newPrize);
        }
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

    public void removeTicket(String prizeId) {
        if (prizeId == null) {
            return;
        }

        prizes.remove(prizeId);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTicketMultiWinEnabled() {
        return ticketMultiWinEnabled;
    }

    public void setTicketMultiWinEnabled(boolean ticketMultiWinEnabled) {
        this.ticketMultiWinEnabled = ticketMultiWinEnabled;
    }
}
