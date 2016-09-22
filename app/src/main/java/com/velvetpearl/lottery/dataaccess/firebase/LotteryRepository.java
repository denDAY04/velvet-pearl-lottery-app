package com.velvetpearl.lottery.dataaccess.firebase;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

/**
 * Created by Stensig on 22-Sep-16.
 */
public class LotteryRepository implements ILotteryRepository {

    private static final String LOG_TAG = "LotteryRepository";
    private final FirebaseAuth dbAuth;
    private FirebaseDatabase dbContext = null;

    public LotteryRepository() {
        dbAuth = FirebaseAuth.getInstance();
        dbAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(LOG_TAG, "signInAnonymously:onComplete: " + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(LOG_TAG, "signInAnonymously", task.getException());
                } else {
                    dbContext = FirebaseDatabase.getInstance();
                }
            }
        });
    }

    public boolean isAuthenticated() {
        return dbContext != null;
    }

    @Override
    public Lottery getLottery(long id) {
        return null;
    }

    @Override
    public Lottery saveLottery(Lottery lottery) {
        dbContext.getReference("lotteries").equalTo()
        return null;
    }
}
