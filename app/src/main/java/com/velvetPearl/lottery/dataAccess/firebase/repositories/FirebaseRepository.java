package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.firebase.FirebaseQueryObject;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Abstract base class for Firebase repository classes.
 */
public abstract class FirebaseRepository {

    protected static final String LOG_TAG = "FirebaseRepository";

    protected FirebaseDatabase dbContext = null;
    protected HashMap<String, FirebaseQueryObject> dataAccessorsCollection;


    protected FirebaseRepository(FirebaseDatabase dbContext) {
        this.dbContext = dbContext;
        dataAccessorsCollection = new HashMap<>();
    }

    /**
     * Stop syncing Firebase changes denoted by the key.
     * @param key Unique key denoting the pair to be removed.
     */
    protected void detatchEntityListener(String key) {
        FirebaseQueryObject dataAccessPair = dataAccessorsCollection.get(key);
        if (dataAccessPair != null) {
            dataAccessPair.query.removeEventListener(dataAccessPair.listener);
        }
    }

    /**
     * Attach the child event listener to the query stored in the query object and store it in
     * a the class' private container.
     * @param key Unique identifier that will denote the pair.
     * @param queryObject Object encapsulating the Firebase query and child event listener.
     */
    public void attachAndStoreQueryObject(String key, FirebaseQueryObject queryObject) {
        if (key != null && queryObject.query != null && queryObject.listener != null && !dataAccessorsCollection.containsKey(key)) {
            queryObject.query.addChildEventListener(queryObject.listener);
            dataAccessorsCollection.put(key, queryObject);
        }
    }

    /**
     * Detach all stored query objects, stopping the Firebase data sync, and remove the objects.
     */
    public void detachAndRemoveAllQueryObjects() {
        for ( String key : dataAccessorsCollection.keySet()) {
            detatchEntityListener(key);
        }
        dataAccessorsCollection.clear();
    }
}
