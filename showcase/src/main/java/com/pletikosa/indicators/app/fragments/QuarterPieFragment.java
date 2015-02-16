package com.pletikosa.indicators.app.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.pletikosa.indicators.IndicatorView;
import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.Orientation;
import com.pletikosa.indicators.consts.SizeUnit;
import com.pletikosa.indicators.pie.QuarterPieIndicator;
import com.pletikosa.indicators.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class QuarterPieFragment extends IndicatorFragment {

    @InjectView(R.id.seek_radius) SeekBar mSeekRadius;
    @InjectView(R.id.seek_inner_radius) SeekBar mSeekInnerRadius;
    @InjectView(R.id.seek_animation) SeekBar mSeekAnimation;
    @InjectView(R.id.seek_direction) SeekBar mSeekDirection;
    @InjectView(R.id.seek_orientation) SeekBar mSeekOrientation;
    @InjectView(R.id.quarter_pie_indicator) QuarterPieIndicator mIndicator;

    public static QuarterPieFragment newInstance() {
        return new QuarterPieFragment();
    }

    public QuarterPieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_quarter_pie, container, false);
        ButterKnife.inject(this, view);

        startUpdate();
        setSizeSeek();
        startOrientationUpdate();
        setAnimationSeek();
        setDirectionSeek();

        return view;
    }

    @Override
    protected SeekBar getAnimationSeek() {
        return mSeekAnimation;
    }

    @Override
    protected IndicatorView getIndicator() {
        return mIndicator;
    }

    @Override
    protected int getSectionNumber() {
        return 4;
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
                mIndicator.setRadius(SizeUnit.PX, (int) (mWidth * ((float) seekBar
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
                mIndicator.setInnerRadius(seekBar.getProgress());
            }
        });

        mIndicator.setRadius(SizeUnit.PX, (int) (mWidth * ((float) mSeekRadius.getProgress() / mSeekRadius.getMax())));
        mIndicator.setInnerRadius(mSeekInnerRadius.getProgress());
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
                mIndicator.setOrientation(Orientation.values()[seekBar.getProgress() + 4]);
                showToast("Orientation: " + (Orientation.values()[seekBar.getProgress() + 4].name()));
            }
        });
        mIndicator.setOrientation(Orientation.SOUTH_WEST);
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
                mIndicator.setDirection(Direction.values()[seekBar.getProgress()]);
                showToast("Direction: " + (Direction.values()[seekBar.getProgress()].name()));
            }
        });
    }
}
