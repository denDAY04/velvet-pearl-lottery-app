package com.velvetPearl.lottery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.velvetPearl.lottery.fragments.Welcome;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "MainActivity";

    private ListView menuList;
    private String[] menuTitles;
    private DrawerLayout menuLayout;
    private CharSequence menuTitle;
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, new Welcome()).commit();
        }
    }

    private void initUi() {
        menuTitles = new String[] {
                getString(R.string.welcome_new_lottery),
                getString(R.string.welcome_lottery_history),
                getString(R.string.tickets),
                getString(R.string.winners),
                getString(R.string.prizes),
                getString(R.string.about),
        };
        menuList = (ListView) findViewById(R.id.left_menu);

        menuList.setAdapter(new ArrayAdapter(this, R.layout.listitem_prize, R.id.list_item_prize_name ,menuTitles));
        menuList.setOnItemClickListener(this);

        title = menuTitle = getTitle();
        menuLayout = (DrawerLayout) findViewById(R.id.menu_layout);
        menuLayout.addDrawerListener(new ActionBarDrawerToggle(this, menuLayout, R.string.menu_open, R.string.menu_close));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 2:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return;
                }

                break;

            case 3:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return;
                }

                break;

            case 4:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return;
                }

                break;
        }
    }

    private void showNoActiveLotteryError() {
        Toast.makeText(this, R.string.no_active_lottery, Toast.LENGTH_LONG).show();
    }

}
