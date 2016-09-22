package com.velvetpearl.lottery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.localdb.LotteryRepository;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
