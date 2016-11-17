package com.velvetPearl.lottery;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.velvetPearl.lottery.fragments.About;
import com.velvetPearl.lottery.fragments.History;
import com.velvetPearl.lottery.fragments.LotteryHome;
import com.velvetPearl.lottery.fragments.NewLottery;
import com.velvetPearl.lottery.fragments.Preferences;
import com.velvetPearl.lottery.fragments.Prizes;
import com.velvetPearl.lottery.fragments.Tickets;
import com.velvetPearl.lottery.fragments.Welcome;
import com.velvetPearl.lottery.fragments.Winners;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
            Log.d(LOG_TAG, "No saved instance state.");
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, new Welcome()).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (ApplicationDomain.getInstance().getActiveLottery() != null) {
            outState.putString("lotteryId", (String) ApplicationDomain.getInstance().getActiveLottery().getId());
        }

        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void initUi() {
        menuLayout = (DrawerLayout) findViewById(R.id.menu_layout);
        navigationMenu = (NavigationView) findViewById(R.id.navigation_menu);
        navigationMenu.setNavigationItemSelectedListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        Drawable menuIcon = getDrawable(R.drawable.ic_menu_white_24dp);
        if (menuIcon != null) {
            DrawableCompat.setTint(menuIcon, ContextCompat.getColor(this, R.color.datapad_accent));
        }
        actionBar.setHomeAsUpIndicator(menuIcon);
        actionBar.setDisplayHomeAsUpEnabled(true);


        navigationMenu.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                ApplicationDomain.getInstance().clearActiveLottery();
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Welcome()).commit();
            }
        });
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

        switch (pressedId) {

            case R.id.menu_new_lottery:
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new NewLottery()).addToBackStack(null).commit();
                break;

            case R.id.menu_history:
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new History()).addToBackStack(null).commit();
                break;

            case R.id.menu_home:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return false;
                }
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LotteryHome()).addToBackStack(null).commit();
                break;

            case R.id.menu_tickets:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return false;
                }
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Tickets()).addToBackStack(null).commit();
                break;

            case R.id.menu_winners:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return false;
                }
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Winners()).addToBackStack(null).commit();
                break;

            case R.id.menu_prizes:
                if (ApplicationDomain.getInstance().getActiveLottery() == null) {
                    showNoActiveLotteryError();
                    return false;
                }
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Prizes()).addToBackStack(null).commit();
                break;

//            case R.id.menu_preferences:
//                closeMenu();
//                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Preferences()).addToBackStack(null).commit();
//                break;

            case R.id.menu_about:
                closeMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new About()).addToBackStack(null).commit();
                break;

            default:
                Log.d(LOG_TAG, "Unexpected menu option.");
                return false;

        }
        return true;
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

    public void disableActiveLotteryMenuItems() {
        navigationMenu.getMenu().findItem(R.id.menu_home).setEnabled(false);
        navigationMenu.getMenu().findItem(R.id.menu_tickets).setEnabled(false);
        navigationMenu.getMenu().findItem(R.id.menu_winners).setEnabled(false);
        navigationMenu.getMenu().findItem(R.id.menu_prizes).setEnabled(false);
    }

    public void enableActiveLotteryMenuItems() {
        navigationMenu.getMenu().findItem(R.id.menu_home).setEnabled(true);
        navigationMenu.getMenu().findItem(R.id.menu_tickets).setEnabled(true);
        navigationMenu.getMenu().findItem(R.id.menu_winners).setEnabled(true);
        navigationMenu.getMenu().findItem(R.id.menu_prizes).setEnabled(true);
    }
}
