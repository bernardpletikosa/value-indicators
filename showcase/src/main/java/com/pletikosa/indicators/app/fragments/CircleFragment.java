package com.pletikosa.indicators.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.pletikosa.indicators.IndicatorView;
import com.pletikosa.indicators.app.R;
import com.pletikosa.indicators.circle.CircleIndicator;
import com.pletikosa.indicators.consts.SizeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CircleFragment extends IndicatorFragment {

    @InjectView(R.id.seek_radius) SeekBar mSizeRadius;
    @InjectView(R.id.seek_animation) SeekBar mAnimationSeek;
    @InjectView(R.id.circle) CircleIndicator mIndicator;

    public static CircleFragment newInstance() {
        return new CircleFragment();
    }

    public CircleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_circle, container, false);
        ButterKnife.inject(this, view);

        startUpdate();
        setSizeSeek();
        setAnimationSeek();

        return view;
    }

    @Override
    protected SeekBar getAnimationSeek() {
        return mAnimationSeek;
    }

    @Override
    protected IndicatorView getIndicator() {
        return mIndicator;
    }

    @Override
    protected int getSectionNumber() {
        return 0;
    }

    private void setSizeSeek() {
        mIndicator.setRadius(SizeUnit.PX, getRadius());
        mSizeRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIndicator.setRadius(SizeUnit.PX, getRadius());
            }
        });
    }

    private int getRadius() {
        final float ratio = (float) mSizeRadius.getProgress() / mSizeRadius.getMax();
        return (int) (mWidth / 2f * ratio);
    }
}
