package com.velvetPearl.lottery.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.velvetPearl.lottery.ApplicationDomain;
import com.velvetPearl.lottery.R;
import com.velvetPearl.lottery.dataAccess.models.Lottery;
import com.velvetPearl.lottery.dataAccess.models.LotteryNumber;
import com.velvetPearl.lottery.dataAccess.models.Prize;
import com.velvetPearl.lottery.dataAccess.models.Ticket;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by Stensig on 19-Nov-16.
 */

public class PrintDlg extends DialogFragment implements View.OnClickListener {

    private static final String LOG_TAG = "DialogFragment";

    // XML tags and attributes names
    private static final String NAMESPACE = null;
    private static final String ID_ATTR = "Id";
    private static final String LOTTERY_ROOT_TAG = "VelvetPearLottery";
    private static final String NAME_TAG = "Name";
    private static final String CREATED_TAG = "Created";
    private static final String RANGE_MIN_TAG = "MinLotteryNumber";
    private static final String RANGE_MAX_TAG = "MaxLotteryNumber";
    private static final String ALLOW_MULTI_WINNING_PER_TICKET_TAG = "AllowMultipleWinningsPerTicket";
    private static final String TICKETS_ROOT_TAG = "Tickets";
    private static final String TICKET_TAG = "Ticket";
    private static final String TICKET_NUMBERS_ROOT_TAG = "LotteryNumbers";
    private static final String TICKET_NUMBER_TAG = "LotteryNumber";
    private static final String TICKET_NUMBER_PRICE_ATTR = "Price";
    private static final String TICKET_OWNER_TAG = "Owner";
    private static final String PRIZES_ROOT_TAG = "Prizes";
    private static final String PRIZE_TAG = "Prize";
    private static final String PRIZE_WINNING_NUMBER_ID_ATTR = "LotteryNumber";


    private static final String FILE_EXTENSION = ".vplf";
    private static final int FILE_EXTENSION_LENGTH = FILE_EXTENSION.length();
    private File outputFile;
    private Lottery lottery;
    private AsyncTask printingTask;
    private ProgressDialog printingProgressDlg;

    // UI elements
    private Button cancelBtn;
    private Button confirmBtn;
    private Switch storageDeviceSwitch;
    private EditText saveAsInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_print_dlg, container, false);

        lottery = ApplicationDomain.getInstance().getActiveLottery();
        initUI(root);

        return root;
    }

    private void initUI(View root) {
        cancelBtn = (Button) root.findViewById(R.id.print_cancel);
        cancelBtn.setOnClickListener(this);
        confirmBtn = (Button) root.findViewById(R.id.print_confirm);
        confirmBtn.setOnClickListener(this);

        storageDeviceSwitch = (Switch) root.findViewById(R.id.print_storage_device);
        storageDeviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    storageDeviceSwitch.setText(R.string.external);
                } else {
                    storageDeviceSwitch.setText(R.string.internal);
                }
            }
        });

        saveAsInput = (EditText) root.findViewById(R.id.print_save_as);
        saveAsInput.setText(lottery.getName());
    }

    @Override
    public void onClick(View view) {
        if (view == cancelBtn) {
            getFragmentManager().popBackStack();
        } else if (view == confirmBtn) {
            // Get filename
            String fileName = saveAsInput.getText().toString();
            if (fileName == null || fileName.isEmpty()) {
                Toast.makeText(getContext(), R.string.missing_file_name, Toast.LENGTH_SHORT).show();
                return;
            }

            // Add file extension (.vplf) if missing
            if (fileName.length() < FILE_EXTENSION_LENGTH || !fileName.substring(fileName.length() - FILE_EXTENSION_LENGTH, fileName.length()).equals(FILE_EXTENSION)) {
                fileName += FILE_EXTENSION;
            }

            // Open file handle either on external or internal storage
            if (storageDeviceSwitch.isChecked()) {
                if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    Toast.makeText(getContext(), R.string.no_external_drive, Toast.LENGTH_SHORT).show();
                    return;
                }

                File destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!destDir.exists()) {
                    if (!destDir.mkdirs()) {
                        Log.d(LOG_TAG, String.format("Failed to create directory %s for output file.", destDir.getPath()));
                    } else {
                        Log.d(LOG_TAG, String.format("Createed directory %s for output file.", destDir.getPath()));
                    }
                }
                outputFile = new File(destDir, fileName);
            } else {
                outputFile = new File(getContext().getFilesDir(), fileName);
            }

            // Don't overwrite existing file without warning
            if (outputFile.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
                builder.setMessage(R.string.file_exists)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                printLotteryToFile();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                builder.create().show();
            } else {
                printLotteryToFile();
            }
        }
    }

    private void printLotteryToFile() {
        printingProgressDlg = new ProgressDialog(getContext());
        printingProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        printingProgressDlg.setMessage(getString(R.string.print_to_file_progress_message));
        printingProgressDlg.setProgress(0);
        printingProgressDlg.setIndeterminate(false);
        printingProgressDlg.setCancelable(true);
        printingProgressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                printingTask.cancel(true);
                printingProgressDlg.dismiss();
                getFragmentManager().popBackStack();
            }
        });
        printingProgressDlg.show();

        printingTask = new AsyncTask() {
            @Override
            protected void onPostExecute(Object o) {
                if (printingProgressDlg != null && printingProgressDlg.isShowing()) {
                    printingProgressDlg.dismiss();
                }

                String message = getString(R.string.print_to_file_finished);
                if (o != null) {
                    message = (String) o;
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                if (printingProgressDlg != null && printingProgressDlg.isShowing()) {
                    Log.d(LOG_TAG, String.format("Updating progress by %d", (int)values[0]));
                    printingProgressDlg.incrementProgressBy((int)values[0]);
                }
            }

            @Override
            protected void onCancelled(Object o) {
                if (printingProgressDlg != null && printingProgressDlg.isShowing()) {
                    printingProgressDlg.dismiss();
                }
                Toast.makeText(getContext(), R.string.print_to_file_cancelled, Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                if (lottery == null || isCancelled()) {
                    return null;
                }

                Map<Object, Ticket> tickets = lottery.getTickets();
                Map<Object, Prize> prizes = lottery.getPrizes();

                // Calculate the weight of each step in the process, with progress dialog being full at 1000 elements
                int progressWeight =  100 / (tickets.size() + prizes.size());

                XmlSerializer xmlWriter = Xml.newSerializer();
                StringWriter xmlStringContainer = new StringWriter();

                try {
                    xmlWriter.setOutput(xmlStringContainer);
                    xmlWriter.startDocument("UTF-8", true);
                    xmlWriter.startTag(NAMESPACE, LOTTERY_ROOT_TAG);

                    xmlWriter.attribute(NAMESPACE, ID_ATTR, (String)lottery.getId());

                    xmlWriter.startTag(NAMESPACE, NAME_TAG);
                    xmlWriter.text(lottery.getName());
                    xmlWriter.endTag(NAMESPACE, NAME_TAG);

                    xmlWriter.startTag(NAMESPACE, CREATED_TAG);
                    xmlWriter.text(Long.toString(lottery.getCreated()));
                    xmlWriter.endTag(NAMESPACE, CREATED_TAG);

                    xmlWriter.startTag(NAMESPACE, RANGE_MIN_TAG);
                    xmlWriter.text(Integer.toString(lottery.getLotteryNumLowerBound()));
                    xmlWriter.endTag(NAMESPACE, RANGE_MIN_TAG);

                    xmlWriter.startTag(NAMESPACE, RANGE_MAX_TAG);
                    xmlWriter.text(Integer.toString(lottery.getLotteryNumUpperBound()));
                    xmlWriter.endTag(NAMESPACE, RANGE_MAX_TAG);

                    xmlWriter.startTag(NAMESPACE, ALLOW_MULTI_WINNING_PER_TICKET_TAG);
                    xmlWriter.text(Boolean.toString(lottery.isTicketMultiWinEnabled()));
                    xmlWriter.endTag(NAMESPACE, ALLOW_MULTI_WINNING_PER_TICKET_TAG);

                    xmlWriter.startTag(NAMESPACE, TICKETS_ROOT_TAG);
                    for (Object ticketId : tickets.keySet()) {
                        if (isCancelled()) {
                            return null;
                        }

                        Ticket ticket = tickets.get(ticketId);
                        if (ticket != null) {
                            xmlWriter.startTag(NAMESPACE, TICKET_TAG);
                            xmlWriter.attribute(NAMESPACE, ID_ATTR, (String)ticket.getId());

                            xmlWriter.startTag(NAMESPACE, TICKET_NUMBERS_ROOT_TAG);
                            for (LotteryNumber number : ticket.getLotteryNumbers()) {
                                xmlWriter.startTag(NAMESPACE, TICKET_NUMBER_TAG);
                                xmlWriter.attribute(NAMESPACE, ID_ATTR, (String)number.getId());
                                xmlWriter.attribute(NAMESPACE, TICKET_NUMBER_PRICE_ATTR, Double.toString(number.getPrice()));
                                xmlWriter.text(Integer.toString(number.getLotteryNumber()));
                                xmlWriter.endTag(NAMESPACE, TICKET_NUMBER_TAG);
                            }
                            xmlWriter.endTag(NAMESPACE, TICKET_NUMBERS_ROOT_TAG);

                            xmlWriter.startTag(NAMESPACE, TICKET_OWNER_TAG);
                            xmlWriter.text(ticket.getOwner());
                            xmlWriter.endTag(NAMESPACE, TICKET_OWNER_TAG);
                            xmlWriter.endTag(NAMESPACE, TICKET_TAG);
                        }
                        publishProgress(progressWeight);
                    }
                    xmlWriter.endTag(NAMESPACE, TICKETS_ROOT_TAG);

                    xmlWriter.startTag(NAMESPACE, PRIZES_ROOT_TAG);
                    for (Object prizeId : lottery.getPrizes().keySet()) {
                        if (isCancelled()) {
                            return null;
                        }

                        Prize prize = prizes.get(prizeId);
                        if (prize != null) {
                            xmlWriter.startTag(NAMESPACE, PRIZE_TAG);
                            xmlWriter.attribute(NAMESPACE, ID_ATTR, (String)prize.getId());
                            if (prize.getNumberId() != null) {
                                xmlWriter.attribute(NAMESPACE, PRIZE_WINNING_NUMBER_ID_ATTR, (String)prize.getNumberId());
                            }
                            xmlWriter.text(prize.getName());
                            xmlWriter.endTag(NAMESPACE, PRIZE_TAG);
                        }
                        publishProgress(progressWeight);
                    }
                    xmlWriter.endTag(NAMESPACE, PRIZES_ROOT_TAG);
                    xmlWriter.endTag(NAMESPACE, LOTTERY_ROOT_TAG);
                    xmlWriter.endDocument();
                    xmlWriter.flush();
                    byte[] xmlData = xmlStringContainer.toString().getBytes();
                    Log.d(LOG_TAG, String.format("XML file will take up %f kB", xmlData.length / 1000.0));

                    // Check that there's space for the file
                    StatFs stat = new StatFs(outputFile.getParent());
                    if (stat.getAvailableBytes() < xmlData.length) {
                        return getString(R.string.print_to_file_space_error);
                    }

                    FileOutputStream fileStream = new FileOutputStream(outputFile);
                    fileStream.write(xmlData);
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return getString(R.string.an_error_happened);
                }

                return null;
            }
        };

        printingTask.execute();
    }

}
