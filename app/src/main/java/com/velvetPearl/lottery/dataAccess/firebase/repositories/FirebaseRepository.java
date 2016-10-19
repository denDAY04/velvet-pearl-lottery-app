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
    protected static final long LOCK_TIMEOUT_MS = 10000;

    protected FirebaseDatabase dbContext = null;
    protected Object lock = new Object();
    protected boolean unlockedByNotify = false;

    protected HashMap<String, FirebaseQueryObject> dataAccessorsCollection;

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

//    /**
//     * Attach a Firebase {@Link ValueEventListener} to the {@Link Query} object and sync any reads
//     * to ApplicationDomain's active lottery reference.
//     * <p>
//     * This method is abstract and requires the concrete repositories to implement their own proper
//     * version of the entity listener so as to process the data correctly.
//     * @param query The query on which to attach the listener.
//     * @param entityId The ID (Firebase key value) of the entity that will be synced. If the query
//     *                 if related to more than one entity this parameter should be ignored in the
//     *                 concrete implementation.
//     * @return The listener that has been attached.
//     */
//    protected abstract ValueEventListener attachEntityListener(Query query, final String entityId);
//
//    /**
//     * Remove the {@Link ValueEventListener} from the {@Link Query} object.
//     * @param query The query from which to remove the listener.
//     * @param listener The listener that should be removed.
//     */
//    protected void detachEntityListener(Query query, ValueEventListener listener) {
//        if (query != null && listener != null) {
//            query.removeEventListener(entityListener);
//        }
//    }

    /**
     * Stop syncing Firebase changes denoted by the key and remove the stored query and
     * listener pair.
     * @param key Unique key denoting the pair to be removed.
     */
    protected void removeEntityListener(String key) {
        FirebaseQueryObject dataAccessPair = dataAccessorsCollection.get(key);
        if (dataAccessPair != null) {
            dataAccessPair.query.removeEventListener(dataAccessPair.listener);
            dataAccessorsCollection.remove(key);
        }
    }

    /**
     * Attach the child event listener to the query stored in the query object and store it in
     * a the class' private container.
     * @param key Unique identifier that will denote the pair.
     * @param queryObject Object encapsulating the Firebase query and child event listener.
     */
    public void attachAndStoreQueryObject(String key, FirebaseQueryObject queryObject) {
        if (key != null && queryObject.query != null && queryObject.listener != null) {
            queryObject.query.addChildEventListener(queryObject.listener);
            dataAccessorsCollection.put(key, queryObject);
        }
    }

    /**
     * Detach all stored query objects, stopping the Firebase data sync, and remove the objects.
     */
    public void detachAndRemoveAllQueryObjects() {
        for ( String key : dataAccessorsCollection.keySet()) {
            FirebaseQueryObject dataAccessPair = dataAccessorsCollection.get(key);
            dataAccessPair.query.removeEventListener(dataAccessPair.listener);
        }
        dataAccessorsCollection.clear();
    }
}
