package com.velvetPearl.lottery.dataAccess.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.velvetPearl.lottery.IEntityUiUpdater;

import java.util.concurrent.TimeoutException;

/**
 * Abstract base class for Firebase repository classes.
 */
public abstract class FirebaseRepository {

    protected static final String LOG_TAG = "FirebaseRepository";
    protected static final long LOCK_TIMEOUT_MS = 10000;

    protected final FirebaseAuth dbAuth;
    protected FirebaseDatabase dbContext = null;
    protected Object lock = new Object();
    protected boolean unlockedByNotify = false;

    protected Query query = null;
    protected ValueEventListener entityListener = null;

    protected FirebaseRepository() {
        dbAuth = FirebaseAuth.getInstance();
    }

    /**
     * Authenticate access to the Firebase database.
     * NOTE that this call locks the active thread until the authorization either succeeds or fails.
     * @throws TimeoutException if the authentication task didn't complete in time.
     */
    protected void authenticate() throws TimeoutException {
        if (dbContext != null)
            return;

        dbAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                synchronized (lock) {
                    if (!task.isSuccessful()) {
                        Log.w(LOG_TAG, "authenticate", task.getException());
                        Log.d(LOG_TAG, "authenticate:signInAnonymously Firebase authentication failed");
                    } else {
                        Log.d(LOG_TAG, "authenticate:signInAnonymously Firebase authentication succeeded");
                        dbContext = FirebaseDatabase.getInstance();
                    }
                    unlockedByNotify = true;
                    lock.notify();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                synchronized (lock) {
                    Log.w(LOG_TAG, "authenticate:signInAnonymously action failed", e);
                    unlockedByNotify = true;
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            try {
                unlockedByNotify = false;
                lock.wait(LOCK_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "authenticate: waiting on authentication interrupted");
            }
        }
        verifyAsyncTask();
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
     * to LotterySingleton's active lottery reference.
     * <p>
     * This method is abstract and requires the concrete repositories to implement their own proper
     * version of the entity listener so as to process the data correctly.
     * @param query The query on which to attach the listener.
     * @param entityId The ID (Firebase key value) of the entity that will be synced. If the query
     *                 if related to more than one entity this parameter should be ignored in the
     *                 concrete implementation.
     * @param uiUpdater The callback object for updating the UI whenever the query data is synced.
     * @return The listener that has been attached.
     */
    protected abstract ValueEventListener attachEntityListener(Query query, final String entityId, final IEntityUiUpdater uiUpdater);

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
}
