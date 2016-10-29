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
            FirebaseQueryObject dataAccessPair = dataAccessorsCollection.get(key);
            dataAccessPair.query.removeEventListener(dataAccessPair.listener);
        }
        dataAccessorsCollection.clear();
    }
}
