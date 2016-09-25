package com.velvetpearl.lottery;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.velvetpearl.lottery.dataaccess.firebase.LotteryRepository;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String LOG_TAG = "MainActivity";

    ILotteryRepository lotteryRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new AsyncTask() {
            @Override
            protected void onPostExecute(Object result) {
                ArrayList<Lottery> lotteries = (ArrayList<Lottery>) result;
                TextView txtView = (TextView) findViewById(R.id.fooTextView);
                txtView.setText(String.format("#lotteries: %d", lotteries.size()));
                if (lotteries.size() > 0) {
                    Lottery lottery = lotteries.get(0);
                    lottery.setCreated(new Date().getTime());
                    lotteryRepo.saveLottery(lottery);
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                lotteryRepo = new LotteryRepository();
                return lotteryRepo.getAllLotteries();
            }
        }.execute();

        ((Button) findViewById(R.id.ClckBtn)).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        final Lottery lottery = new Lottery();
        lottery.setPricePerLotteryNum(52.0);
        lottery.setLotteryNumUpperBound(10);
        lottery.setLotteryNumLowerBound(1);
        lottery.setCreated(new Date().getTime());

        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Saving new lottery", true);

        new AsyncTask() {
            @Override
            public void onPostExecute(Object result) {
                progressDialog.dismiss();
                Log.d(LOG_TAG, "onClick:onPostExecute: finished saving new data");
            }

            @Override
            protected Object doInBackground(Object[] params) {
                progressDialog.show();
                Log.d(LOG_TAG, "onClick:doInBackground: saving new data");
                return lotteryRepo.saveLottery(lottery);
            }
        }.execute();

    }
}
