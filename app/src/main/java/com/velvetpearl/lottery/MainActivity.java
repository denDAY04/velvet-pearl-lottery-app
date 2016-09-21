package com.velvetpearl.lottery;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.velvetpearl.lottery.dataaccess.models.Lottery;

import org.w3c.dom.Text;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RealmConfiguration dbConfig = new RealmConfiguration.Builder(getBaseContext()).build();
        Realm.setDefaultConfiguration(dbConfig);
        final Realm db = Realm.getDefaultInstance();
        db.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                Lottery lottery = realm.createObject(Lottery.class);
                lottery.setId(db);
                lottery.setCreated(new Date());
                lottery.setLotteryNumLowerBound(1);
                lottery.setLotteryNumUpperBound(100);
                lottery.setPricePerLotteryNum(50.0);
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                TextView tv = (TextView) findViewById(R.id.fooTextView);
                RealmResults<Lottery> res = db.where(Lottery.class).findAll();
                if (res.size() > 0) {
                    tv.setText(String.format("%d items. 1st ID: %d", res.size(), res.get(0).getId()));
                }
            }
        }, new Realm.Transaction.OnError() {

            @Override
            public void onError(Throwable error) {
                TextView tv = (TextView) findViewById(R.id.fooTextView);
                tv.setText("Fail!");
                error.printStackTrace();
            }
        });


    }

}
