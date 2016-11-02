package com.velvetPearl.lottery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.velvetPearl.lottery.fragments.History;
import com.velvetPearl.lottery.fragments.Prizes;
import com.velvetPearl.lottery.fragments.Tickets;
import com.velvetPearl.lottery.fragments.Welcome;
import com.velvetPearl.lottery.fragments.Winners;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainActivity";

    private NavigationView navigationMenu;
    private DrawerLayout menuLayout;
    private Toolbar toolbar;
    private ActionBar actionBar;

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
        menuLayout = (DrawerLayout) findViewById(R.id.menu_layout);
        navigationMenu = (NavigationView) findViewById(R.id.navigation_menu);
        navigationMenu.setNavigationItemSelectedListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void showNoActiveLotteryError() {
        Toast.makeText(this, R.string.no_active_lottery, Toast.LENGTH_LONG).show();
    }

    private void closeMenu() {
        menuLayout.closeDrawer(GravityCompat.START, true);
    }

    private void openMenu() {
        menuLayout.openDrawer(GravityCompat.START, true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int pressedId = item.getItemId();

        if (pressedId == R.id.menu_new_lottery) {
            closeMenu();
            // TODO switch to new lottery view

        } else if (pressedId == R.id.menu_history) {
            closeMenu();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new History()).addToBackStack(null).commit();
        } else if (pressedId == R.id.menu_tickets) {
            if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                showNoActiveLotteryError();
                return false;
            }
            closeMenu();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Tickets()).addToBackStack(null).commit();

        } else if (pressedId == R.id.menu_winners) {
            if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                showNoActiveLotteryError();
                return false;
            }
            closeMenu();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Winners()).addToBackStack(null).commit();

        } else if (pressedId == R.id.menu_prizes) {
            if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                showNoActiveLotteryError();
                return false;
            }
            closeMenu();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Prizes()).addToBackStack(null).commit();

        } else if (pressedId == R.id.menu_about) {
            closeMenu();
            // TODO switch to about page
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!menuLayout.isDrawerOpen(GravityCompat.START)) {
                openMenu();
            } else {
                closeMenu();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
