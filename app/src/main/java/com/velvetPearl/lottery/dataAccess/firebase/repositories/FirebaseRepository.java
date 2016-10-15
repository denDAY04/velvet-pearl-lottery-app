package com.velvetPearl.lottery.dataAccess.firebase.repositories;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.dataAccess.firebase.QueryListenerPair;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Abstract base class for Firebase repository classes.
 */
public abstract class FirebaseRepository {

    protected static final String LOG_TAG = "FirebaseRepository";
    protected static final long LOCK_TIMEOUT_MS = 10000;

    protected FirebaseDatabase dbContext = null;
    protected Object lock = new Object();
    protected boolean unlockedByNotify = false;

    protected Query query = null;
    protected ValueEventListener entityListener = null;

    protected HashMap<String, QueryListenerPair> dataAccessorsCollection;

    protected FirebaseRepository(FirebaseDatabase dbContext) {
        this.dbContext = dbContext;
        dataAccessorsCollection = new HashMap<>();
    }


    /**
     * Verify whether the last async task was completed successfully, as determined by an internal
     * flag. If not, a timeout exception is thrown to indicate the event.
     * @throws TimeoutException if the flag was not set.
     */
    protected void verifyAsyncTask() throws TimeoutException {
        if (!unlockedByNotify)
            throw new TimeoutException();
    }

    /**
     * Attach a Firebase {@Link ValueEventListener} to the {@Link Query} object and sync any reads
     * to ApplicationDomain's active lottery reference.
     * <p>
     * This method is abstract and requires the concrete repositories to implement their own proper
     * version of the entity listener so as to process the data correctly.
     * @param query The query on which to attach the listener.
     * @param entityId The ID (Firebase key value) of the entity that will be synced. If the query
     *                 if related to more than one entity this parameter should be ignored in the
     *                 concrete implementation.
     * @return The listener that has been attached.
     */
    protected abstract ValueEventListener attachEntityListener(Query query, final String entityId);

    /**
     * Remove the {@Link ValueEventListener} from the {@Link Query} object.
     * @param query The query from which to remove the listener.
     * @param listener The listener that should be removed.
     */
    protected void detachEntityListener(Query query, ValueEventListener listener) {
        if (query != null && listener != null) {
            query.removeEventListener(entityListener);
        }
    }

    /**
     * Stop syncing Firebase changes denoted by the key and remove the stored query and
     * listener pair.
     * @param key Unique key denoting the pair to be removed.
     */
    protected void removeEntityListener(String key) {
        QueryListenerPair dataAccessPair = dataAccessorsCollection.get(key);
        if (dataAccessPair != null) {
            dataAccessPair.getQuery().removeEventListener(dataAccessPair.getListener());
            dataAccessorsCollection.remove(key);
        }
    }

    /**
     * Attach a value event listener to the query and store the pair by the unique key.
     * @param key Unique identifier that will denote the pair.
     * @param query The Firebase Query object the listener will be attached to.
     * @param listener The value event listener that will be attached to the query.
     */
    public void attachAndStoreEntityListener(String key, Query query, ValueEventListener listener) {
        if (key != null && query != null && listener != null) {
            query.addValueEventListener(listener);
            QueryListenerPair dataAccessPair = new QueryListenerPair(query, listener);
            if (dataAccessorsCollection.containsKey(key)) {
                throw new IllegalArgumentException(String.format("%s is already stored in the map", key));
            } else {
                dataAccessorsCollection.put(key, dataAccessPair);
            }
        }
    }

    /**
     * Remove all stored query-listener pairs.
     */
    public void removeAllEntityListeners() {
        for ( String key : dataAccessorsCollection.keySet()) {
            removeEntityListener(key);
        }
    }
}
