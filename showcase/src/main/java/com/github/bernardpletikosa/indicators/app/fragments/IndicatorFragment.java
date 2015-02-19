package com.github.bernardpletikosa.indicators.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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

    protected int mWidth;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels - dpToPx((int) (2 * getResources().getDimension(R.dimen.activity_margin)));
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getIndicator().setAnimationDuration(seekBar.getProgress());
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

    protected abstract SeekBar getAnimationSeek();

    protected abstract IndicatorView getIndicator();

    protected abstract int getSectionNumber();
}
