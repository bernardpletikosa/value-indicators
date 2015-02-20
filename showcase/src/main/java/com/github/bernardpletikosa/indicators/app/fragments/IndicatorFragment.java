package com.github.bernardpletikosa.indicators.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.app.MainActivity;
import com.github.bernardpletikosa.indicators.app.R;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

abstract class IndicatorFragment extends Fragment {

    protected int mSize;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = getScreenOrientation() == 0 ? metrics.widthPixels : metrics.heightPixels;
        mSize = width - dpToPx(getScreenOrientation() == 0 ? (int) (2 * getResources()
                .getDimension(R.dimen.activity_margin)) : 70);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getSectionNumber());
    }

    protected void setAnimationSeek() {
        getIndicator().setAnimationDuration(getAnimationSeek().getProgress());
        getAnimationSeek().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                getIndicator().setAnimationDuration(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Animation: " + seekBar.getProgress() + " [ms]");
            }
        });
    }

    protected void showToast(String text) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    protected int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    protected void startUpdate() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Random rand = new Random();
                        getIndicator().indicate(rand.nextFloat() * 100);
                    }
                });
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    protected int getScreenOrientation() {
        WindowManager mWindowManager = (WindowManager) getActivity().getSystemService(Context
                .WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        return display.getOrientation();
    }

    protected abstract SeekBar getAnimationSeek();

    protected abstract IndicatorView getIndicator();

    protected abstract int getSectionNumber();
}
