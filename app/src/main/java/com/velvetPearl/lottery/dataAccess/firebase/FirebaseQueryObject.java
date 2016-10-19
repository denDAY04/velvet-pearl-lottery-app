package com.velvetPearl.lottery.dataAccess.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Stensig on 15-Oct-16.
 */

public class FirebaseQueryObject {

    public ChildEventListener listener;
    public Query query;

    public FirebaseQueryObject() {
        this.query = null;
        this.listener = null;
    }

    public FirebaseQueryObject(Query query, ChildEventListener listener) {
        this.query = query;
        this.listener = listener;
    }

}
