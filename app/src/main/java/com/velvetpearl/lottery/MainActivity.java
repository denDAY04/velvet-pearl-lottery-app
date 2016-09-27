package com.velvetpearl.lottery;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.velvetpearl.lottery.dataaccess.ILotteryRepository;
import com.velvetpearl.lottery.dataaccess.firebase.LotteryRepository;
import com.velvetpearl.lottery.dataaccess.models.Lottery;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "MainActivity";

    ILotteryRepository lotteryRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Loading data", true);

        new AsyncTask() {
            @Override
            protected void onPostExecute(Object result) {
                progressDialog.dismiss();
                if (result.getClass() == TimeoutException.class) {
                       Log.d(LOG_TAG, "onCreate:onPostExecute: getAllLotteries timed out");
                } else {
                    ArrayList<Lottery> lotteries = (ArrayList<Lottery>) result;
                    TextView txtView = (TextView) findViewById(R.id.fooTextView);
                    txtView.setText(String.format("#lotteries: %d", lotteries.size()));
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                lotteryRepo = new LotteryRepository();
                progressDialog.show();
                try {
                    return lotteryRepo.getAllLotteries();
                } catch (TimeoutException e) {
                    return e;
                }
            }
        }.execute();

        findViewById(R.id.ClckBtn).setOnClickListener(this);

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
                if (result.getClass() == TimeoutException.class) {
                    Log.d(LOG_TAG, "onClick:onPostExecute: timed out saving new data");
                } else {
                    Log.d(LOG_TAG, "onClick:onPostExecute: finished saving new data");
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                progressDialog.show();
                Log.d(LOG_TAG, "onClick:doInBackground: saving new data");
                try {
                    return lotteryRepo.saveLottery(lottery);
                } catch (TimeoutException e) {
                    return e;
                }
            }
        }.execute();
    }
}
