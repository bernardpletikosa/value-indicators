package com.github.bernardpletikosa.indicators.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.app.R;
import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;
import com.github.bernardpletikosa.indicators.pie.PieIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PieFragment extends IndicatorFragment {

    @InjectView(R.id.seek_radius) SeekBar mSeekRadius;
    @InjectView(R.id.seek_inner_radius) SeekBar mSeekInnerRadius;
    @InjectView(R.id.seek_animation) SeekBar mSeekAnimation;
    @InjectView(R.id.seek_angle) SeekBar mSeekAngle;
    @InjectView(R.id.seek_direction) SeekBar mSeekDirection;
    @InjectView(R.id.pie_indicator) PieIndicator mIndicator;

    public static PieFragment newInstance() {
        return new PieFragment();
    }

    public PieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_pie, container, false);
        ButterKnife.inject(this, view);

        startUpdate();
        setSizeSeek();
        startAngleUpdate();
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
        return 2;
    }

    private void setSizeSeek() {
        mSeekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getProgress() != 0)
                    mIndicator.setRadius(SizeUnit.PX, (int) (mSize / 2 * ((float) seekBar
                            .getProgress() / mSeekRadius.getMax())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekInnerRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mIndicator.setInnerRadius(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mIndicator.setRadius(SizeUnit.PX, (int) (mSize / 2 * ((float) mSeekRadius.getProgress() / mSeekRadius.getMax())));
        mIndicator.setInnerRadius(mSeekInnerRadius.getProgress());
    }

    private void startAngleUpdate() {
        mSeekAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mIndicator.setStartingAngle(seekBar.getProgress() * 90);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Starting angle: " + seekBar.getProgress() * 90);
            }
        });
    }

    private void setDirectionSeek() {
        mSeekDirection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mIndicator.setDirection(Direction.values()[seekBar.getProgress()]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Direction: " + (Direction.values()[seekBar.getProgress()].name()));
            }
        });
    }
}
