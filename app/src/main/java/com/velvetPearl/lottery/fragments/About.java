package com.velvetPearl.lottery.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.velvetPearl.lottery.BuildConfig;
import com.velvetPearl.lottery.MainActivity;
import com.velvetPearl.lottery.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Stensig on 03-11-2016.
 */

public class About extends Fragment {

    private static final String LOG_TAG = "AboutFragment";

    private WebView webview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);

        initUi(root);

        return root;
    }

    private void initUi(View root) {
        MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.about);
        activity.disableActiveLotteryMenuItems();

        webview = (WebView) root.findViewById(R.id.about_web_view);

        String htmlContent = getAboutHtmlPage();
        // Read the html file into a string
        if (htmlContent == null || htmlContent.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.about_error_loading), Toast.LENGTH_LONG);
            return;
        }

        // Parse runtime information to the string
        htmlContent = String.format(htmlContent, BuildConfig.VERSION_NAME);

        // Display
        Log.d(LOG_TAG, "Displaying about.html page");
        Toast.makeText(activity, R.string.about_language_warning, Toast.LENGTH_SHORT).show();
        webview.loadDataWithBaseURL("", htmlContent, "text/html", "UTF-8", "");
    }

    private String getAboutHtmlPage() {
        InputStream resource = getResources().openRawResource(R.raw.about);
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int readCount;
        try {
            while ((readCount = resource.read(buffer)) != -1) {
                result.write(buffer, 0, readCount);
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Failed reading about.html.", e);
            return new String("");
        }
        return result.toString();
    }
}
