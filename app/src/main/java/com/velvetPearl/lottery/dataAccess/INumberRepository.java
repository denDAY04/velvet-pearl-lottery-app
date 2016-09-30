package com.velvetPearl.lottery.dataAccess;

import com.velvetPearl.lottery.dataAccess.models.Number;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by Stensig on 30-Sep-16.
 */
public interface INumberRepository {

    ArrayList<Number> getNumbersForTicket(Object ticketId) throws TimeoutException;
}
