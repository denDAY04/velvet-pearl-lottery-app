package com.velvetPearl.lottery.viewModels;

import com.velvetPearl.lottery.dataAccess.models.Lottery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Stensig on 08-10-2016.
 */

public class LotteryListViewModel {

    private final Object id;
    private final long created;

    public LotteryListViewModel(Lottery entityModel) {
        created = entityModel.getCreated();
        id = entityModel.getId();
    }

    public Object getId() {
        return id;
    }

    /**
     * Print the object by its created-time in format \"MMM DD YYYY hh:mm\".
     * @return the generated string.
     */
    @Override
    public String toString() {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(created));
    }
}
