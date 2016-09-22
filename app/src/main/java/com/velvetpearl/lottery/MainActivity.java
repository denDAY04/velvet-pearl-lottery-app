package com.velvetpearl.lottery;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.localdb.LotteryRepository;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth dbAuth = FirebaseAuth.getInstance();
        FirebaseAuth.AuthStateListener dbAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                else
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
            }
        };

        dbAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(LOG_TAG, "signInAnonymously:onComplete: " + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(LOG_TAG, "signInAnonymously", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference entityRef = db.getReference("msg");
        entityRef.setValue("Hello, World!");

/*
        RealmConfiguration dbConfig = new RealmConfiguration.Builder(getBaseContext()).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(dbConfig);

        ILotteryRepository lotteryRepo = new LotteryRepository();
        Lottery lottery = lotteryRepo.getLottery(1);
        TextView tv = (TextView) findViewById(R.id.fooTextView);
        if (lottery == null) {
            tv.setText("Not found");
        } else {
            java.text.DateFormat dateFormat =  DateFormat.getDateFormat(getApplicationContext());
            tv.setText(String.format("ID: %d, ticket price pr num: %.2f \n Created: %s", lottery.getId(), lottery.getPricePerLotteryNum(),  dateFormat.format(lottery.getCreated())));
        }
*/
    }

}
