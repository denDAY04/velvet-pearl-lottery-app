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
    private final String name;

    public LotteryListViewModel(Lottery entityModel) {
        created = entityModel.getCreated();
        id = entityModel.getId();
        name = entityModel.getName();
    }

    public Object getId() {
        return id;
    }


    public String getCreatedFormated() {
        return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(created));
    }

    public String getName() {
        return name;
    }
}
