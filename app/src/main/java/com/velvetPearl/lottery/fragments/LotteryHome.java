package com.velvetPearl.lottery.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.dataAccess.DataAccessEvent;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;


public class LotteryHome extends Fragment implements View.OnClickListener, Observer {

    private static final String LOG_TAG = "LotteryHome";

    // UI refs
    private TextView lotteryNameLab = null;
    private TextView timestampLab = null;
    private TextView lotteryNumRangeLab = null;
    private TextView pricePerNumLab = null;
    private TextView lotteryNumSoldCount = null;
    private Button ticketsBtn = null;
    private Button winnersBtn = null;
    private Button prizesBtn = null;
    private CheckBox allowMultiWinOnTicket = null;
    private MenuItem deleteMenu = null;

    private ProgressDialog loadingDialog = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lottery_home, container, false);
        setHasOptionsMenu(true);

        ApplicationDomain.getInstance().addObserver(this);

        if (savedInstanceState == null && ApplicationDomain.getInstance().getActiveLottery() == null) {
            Bundle args = getArguments();
            String lotteryId = args.getString("lotteryId");
            Log.d(LOG_TAG, "loading ID " + lotteryId);

            initLoadingDialog();
            initUi(root);
            loadingDialog.show();

            ApplicationDomain.getInstance().lotteryNumberRepository.clearState();
            ApplicationDomain.getInstance().lotteryRepository.loadLottery(lotteryId);

        } else {
            initLoadingDialog();
            initUi(root);
            updateUi();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "unsubscribing from model updates");
        ApplicationDomain.getInstance().deleteObserver(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.lottery_home_menu, menu);

        // Color icon by inspiration of
        // https://futurestud.io/tutorials/android-quick-tips-8-how-to-dynamically-tint-actionbar-menu-icons
        tintMenuIcon(menu, R.id.menu_lottery_home_print, R.color.datapad_accent);
        tintMenuIcon(menu, R.id.menu_lottery_home_delete, R.color.datapad_accent);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void tintMenuIcon(Menu menu, @IdRes int item, @ColorRes int color) {
        if (menu == null) {
            return;
        }

        MenuItem menuItem = menu.findItem(item);
        Drawable wrappedIcon = DrawableCompat.wrap(menuItem.getIcon());
        DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(getContext(), color));
        menuItem.setIcon(wrappedIcon);
    }

    /**
     * Initialize the UI references and set basic handlers.
     * @param view The view on which to collect the UI references.
     */
    private void initUi(View view) {
        getActivity().setTitle(R.string.lottery);
        ((MainActivity) getActivity()).enableActiveLotteryMenuItems();

        lotteryNameLab = (TextView) view.findViewById(R.id.lotteryhome_name);
        timestampLab = (TextView) view.findViewById(R.id.lotteryhome_timestamp);
        lotteryNumRangeLab = (TextView) view.findViewById(R.id.lotteryhome_num_range_var);
        pricePerNumLab = (TextView) view.findViewById(R.id.lotteryhome_price_per_number_var);
        lotteryNumSoldCount = (TextView) view.findViewById(R.id.lotteryhome_numbers_sold_var);
        ticketsBtn = (Button) view.findViewById(R.id.lotteryhome_tickets_btn);
        winnersBtn = (Button) view.findViewById(R.id.lotteryhome_winners_btn);
        prizesBtn = (Button) view.findViewById(R.id.lotteryhome_prizes_btn);
        allowMultiWinOnTicket = (CheckBox) view.findViewById(R.id.multi_winner_checkbox);

        ticketsBtn.setOnClickListener(this);
        winnersBtn.setOnClickListener(this);
        prizesBtn.setOnClickListener(this);
    }

    /**
     * Initialize the loading dialog, without showing it.
     */
    private void initLoadingDialog() {
        loadingDialog = ProgressDialog.show(getContext(),
                null,       /* timestampLab */
                getString(R.string.lotteryhome_loading_lottery),
                false,      /* indeterminate */
                true,       /* cancelable */
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d(LOG_TAG, "canceling lottery fetch");
                        getFragmentManager().popBackStack();
                        loadingDialog.dismiss();
                    }
                });
    }

    public void updateUi() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();

        Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
        lotteryNameLab.setText(lottery.getName());
        Locale locale = Locale.getDefault();
        String timestamp = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(lottery.getCreated()));
        timestampLab.setText(String.format(locale, "%s %s", getString(R.string.created), timestamp));
        lotteryNumRangeLab.setText(String.format(locale, "%d - %d", lottery.getLotteryNumLowerBound(), lottery.getLotteryNumUpperBound()));
        pricePerNumLab.setText(String.format(locale, "%.2f", lottery.getPricePerLotteryNum()));
        allowMultiWinOnTicket.setChecked(lottery.isTicketMultiWinEnabled());

        int count = 0;
        if (lottery.getTickets() != null) {
            for (Object key : lottery.getTickets().keySet()) {
                Ticket ticket = lottery.getTickets().get(key);
                count += ticket.getLotteryNumbers().size();
            }
        }
        lotteryNumSoldCount.setText(Integer.toString(count));
    }


    @Override
    public void onClick(View v) {
        if (v == ticketsBtn) {
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Tickets()).addToBackStack(null).commit();
        } else if (v == winnersBtn) {
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Winners()).addToBackStack(null).commit();
        } else if (v == prizesBtn) {
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new Prizes()).addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lottery_home_print:
                AlertDialog.Builder printDialog = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
                printDialog.setMessage(R.string.print_to_file_confirm_message)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Open dialog to enter new lottery number
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                                if (prev != null) {
                                    ft.remove(prev);
                                }
                                ft.addToBackStack(null);

                                // Create and show the dialog.
                                DialogFragment printDlg = new PrintDlg();
                                printDlg.show(ft, "dialog");
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                printDialog.create().show();
                return true;

            case R.id.menu_lottery_home_delete:
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
                confirmDialog
                        .setTitle(R.string.attention)
                        .setMessage(R.string.delete_lottery_confirm)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ApplicationDomain.getInstance().lotteryRepository.deleteLottery(ApplicationDomain.getInstance().getActiveLottery());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                confirmDialog.create().show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.getClass() == DataAccessEvent.class) {
            updateUi();
        }
    }

//    private void printLotteryToFile() {
//        Log.d(LOG_TAG, "Printing lottery");
//        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.print_to_file_progress_message), false, true, new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                // Todo stop the printing process
//
//                getFragmentManager().popBackStack();
//            }
//        });
//        progressDialog.setProgress(0);
//
//        new AsyncTask() {
//
//            @Override
//            protected void onPostExecute(Object o) {
//                progressDialog.dismiss();
//                Toast.makeText(getContext(), R.string.print_to_file_finished, Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            protected void onProgressUpdate(Object[] values) {
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.setProgress((int)values[0]);
//                }
//            }
//
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                progressDialog.show();
//
//                Lottery lottery = ApplicationDomain.getInstance().getActiveLottery();
//
//                File file;
//                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
//                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), )
//                }
//
//                return null;
//            }
//        }.execute();
//    }
}
