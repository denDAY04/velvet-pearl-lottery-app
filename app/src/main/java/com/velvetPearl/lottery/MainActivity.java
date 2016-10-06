package com.velvetPearl.lottery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.velvetPearl.lottery.fragments.Welcome;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private ProgressDialog bootDialog;
    private AlertDialog bootDialogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, new Welcome()).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeConstraints();
    }

    /**
     * Initialize the constraints of the application and the running device.
     */
    private void initializeConstraints() {
        bootDialog = ProgressDialog.show(this, null, getString(R.string.initializing_app), true);
        bootDialog.show();

        new AsyncTask() {
            @Override
            public void onPostExecute(Object result) {
                bootDialog.dismiss();

                boolean bootSuccess = (boolean) result;
                if (!bootSuccess) {
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(MainActivity.this);
                    dlgBuilder
                            .setTitle(R.string.error)
                            .setMessage(R.string.error_network_required)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    bootDialogError.dismiss();
                                    finish();       // close app
                                }
                            });
                    bootDialogError = dlgBuilder.create();
                    bootDialogError.show();
                }
            }


            @Override
            protected Object doInBackground(Object[] params) {
                boolean bootSuccess;
                bootSuccess = isConnectedToNetwork();
                // Add more as needed ...
                return bootSuccess;
            }
        }.execute();

    }

    /**
     * Test if the device has an active internet connection.
     * <p>
     * This method blocks the calling thread.
     * @return True if the device has an internet connection. False otherwise.
     */
    private boolean isConnectedToNetwork() {
        final int timeout = 3 * 1000;
        final String host = "firebase.com";

        try {
            if (!InetAddress.getByName(host).isReachable(timeout)) {
                Log.d(LOG_TAG, "isConnectedToNetwork: network connection good.");
                return true;
            }
        } catch (IOException e) {
            // Do nothing.
        }
        return false;
    }

}
