package com.github.bernardpletikosa.indicators.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.app.R;
import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.Orientation;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;
import com.github.bernardpletikosa.indicators.pie.HalfPieIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HalfPieFragment extends IndicatorFragment {

    @InjectView(R.id.seek_radius) SeekBar mSeekRadius;
    @InjectView(R.id.seek_inner_radius) SeekBar mSeekInnerRadius;
    @InjectView(R.id.seek_animation) SeekBar mSeekAnimation;
    @InjectView(R.id.seek_direction) SeekBar mSeekDirection;
    @InjectView(R.id.seek_orientation) SeekBar mSeekOrientation;
    @InjectView(R.id.half_pie_indicator) HalfPieIndicator mIndicator;

    public static HalfPieFragment newInstance() {
        return new HalfPieFragment();
    }

    public HalfPieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_half_pie, container, false);
        ButterKnife.inject(this, view);

        startUpdate();
        setSizeSeek();
        startOrientationUpdate();
        setAnimationSeek();
        setDirectionSeek();

        return view;
    }

    @Override
    protected IndicatorView getIndicator() {
        return mIndicator;
    }

    @Override
    protected int getSectionNumber() {
        return 3;
    }

    @Override
    protected SeekBar getAnimationSeek() {
        return mSeekAnimation;
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
                mIndicator.setRadius(SizeUnit.PX, (int) (mWidth / 2 * ((float) seekBar.getProgress() / mSeekRadius
                        .getMax())));
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

        mIndicator.setRadius(SizeUnit.PX, (int) (mWidth / 2 * ((float) mSeekRadius.getProgress() / mSeekRadius.getMax())));
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
                mIndicator.setOrientation(Orientation.values()[seekBar.getProgress()]);
                showToast("Orientation: " + Orientation.values()[seekBar.getProgress()].name());
            }
        });
        mIndicator.setOrientation(Orientation.SOUTH);
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
