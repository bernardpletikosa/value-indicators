package com.pletikosa.indicators.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.pletikosa.indicators.circle.CircleIndicator;
import com.pletikosa.indicators.consts.SizeUnit;
import com.pletikosa.indicators.app.MainActivity;
import com.pletikosa.indicators.app.R;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CircleFragment extends Fragment {

    @InjectView(R.id.seek_radius)
    SeekBar mSizeRadius;
    @InjectView(R.id.seek_animation)
    SeekBar mAnimationSeek;
    
    @InjectView(R.id.circle)
    CircleIndicator mCircleIndicator;

    private int mWidth;

    public static CircleFragment newInstance() {
        return new CircleFragment();
    }

    public CircleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_circle, container, false);
        ButterKnife.inject(this, view);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels;

        startUpdate();
        setSizeSeek();
        setAnimationSeek();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(0);
    }

    private void startUpdate() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Random rand = new Random();
                        mCircleIndicator.indicate(rand.nextFloat() * 100);
                    }
                });
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void setSizeSeek() {
        mSizeRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCircleIndicator.setRadius(SizeUnit.PX, getRadius());
            }
        });

        mCircleIndicator.setRadius(SizeUnit.PX, getRadius());
    }

    private void setAnimationSeek() {
        mAnimationSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCircleIndicator.setAnimationDuration(seekBar.getProgress());
            }
        });

        mCircleIndicator.setAnimationDuration(mAnimationSeek.getProgress());
    }

    private int getRadius() {
        return (int) (mWidth / 2 * ((float) mSizeRadius.getProgress() / mSizeRadius.getMax()));
    }
}
