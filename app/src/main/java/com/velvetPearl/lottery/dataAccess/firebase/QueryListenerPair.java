package com.velvetPearl.lottery.dataAccess.firebase;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Stensig on 15-Oct-16.
 */

public class QueryListenerPair {

    private final ValueEventListener listener;
    private final Query query;

    public QueryListenerPair(Query query, ValueEventListener listener) {
        this.query = query;
        this.listener = listener;
    }

    public Query getQuery() {
        return query;
    }

    public ValueEventListener getListener() {
        return listener;
    }
}
