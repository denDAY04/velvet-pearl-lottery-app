package com.velvetPearl.lottery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.velvetPearl.lottery.fragments.Home;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, new Home()).commit();
        }
    }

}
