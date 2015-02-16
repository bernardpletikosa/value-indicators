package com.pletikosa.indicators.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.Orientation;
import com.pletikosa.indicators.consts.SizeUnit;
import com.pletikosa.indicators.pie.QuarterPieIndicator;
import com.pletikosa.indicators.app.MainActivity;
import com.pletikosa.indicators.app.R;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class QuarterPieFragment extends Fragment {

    @InjectView(R.id.seek_radius)
    SeekBar mSeekRadius;
    @InjectView(R.id.seek_inner_radius)
    SeekBar mSeekInnerRadius;
    @InjectView(R.id.seek_animation)
    SeekBar mSeekAnimation;
    @InjectView(R.id.seek_direction)
    SeekBar mSeekDirection;
    @InjectView(R.id.seek_orientation)
    SeekBar mSeekOrientation;

    @InjectView(R.id.quarter_pie_indicator)
    QuarterPieIndicator mPieIndicator;

    private int mWidth;

    public static QuarterPieFragment newInstance() {
        return new QuarterPieFragment();
    }

    public QuarterPieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_quarter_pie, container, false);
        ButterKnife.inject(this, view);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidth = metrics.widthPixels;

        startUpdate();
        setSizeSeek();
        startOrientationUpdate();
        setAnimationSeek();
        setDirectionSeek();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(4);
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
                        mPieIndicator.indicate(rand.nextFloat() * 100);
                    }
                });
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void setSizeSeek() {
        mSeekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPieIndicator.setRadius(SizeUnit.PX, (int) (mWidth / 2 * ((float) seekBar
                        .getProgress() / mSeekRadius.getMax())));
            }
        });
        mSeekInnerRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPieIndicator.setInnerRadius(seekBar.getProgress());
            }
        });

        mPieIndicator.setRadius(SizeUnit.PX, (int) (mWidth / 2 * ((float) mSeekRadius.getProgress() / mSeekRadius.getMax())));
        mPieIndicator.setInnerRadius(mSeekInnerRadius.getProgress());
    }

    private void startOrientationUpdate() {
        mSeekOrientation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPieIndicator.setOrientation(Orientation.values()[seekBar.getProgress() + 4]);
            }
        });
        mPieIndicator.setOrientation(Orientation.SOUTH_WEST);
    }

    private void setAnimationSeek() {
        mPieIndicator.setAnimationDuration(mSeekAnimation.getProgress());
        mSeekAnimation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPieIndicator.setAnimationDuration(seekBar.getProgress());
            }
        });
    }

    private void setDirectionSeek() {
        mSeekDirection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int progress = seekBar.getProgress();
                mPieIndicator.setDirection(progress == 0 ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE);
            }
        });
    }
}
