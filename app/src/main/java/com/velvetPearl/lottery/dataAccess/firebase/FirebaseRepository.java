package com.velvetPearl.lottery.dataAccess.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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
                        Log.d(LOG_TAG, "authenticate:signInAnonymously: Firebase authentication failed");
                    } else {
                        Log.d(LOG_TAG, "authenticate:signInAnonymously: Firebase authentication succeeded");
                        dbContext = FirebaseDatabase.getInstance();
                    }
                    unlockedByNotify = true;
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            while (dbContext == null) {
                try {
                    unlockedByNotify = false;
                    lock.wait(LOCK_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "authenticate: waiting on authentication interrupted");
                }
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
}
