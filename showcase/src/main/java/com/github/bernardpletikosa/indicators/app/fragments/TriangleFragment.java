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
import com.github.bernardpletikosa.indicators.line.LineIndicator;
import com.github.bernardpletikosa.indicators.triangle.TriangleIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TriangleFragment extends IndicatorFragment {

    @InjectView(R.id.seek_width) SeekBar mSeekWidth;
    @InjectView(R.id.seek_height) SeekBar mSeekHeight;
    @InjectView(R.id.seek_animation) SeekBar mAnimationSeek;
    @InjectView(R.id.seek_direction) SeekBar mSeekDirection;
    @InjectView(R.id.triangle_indicator) TriangleIndicator mIndicator;

    public static TriangleFragment newInstance() {
        return new TriangleFragment();
    }

    public TriangleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_triangle, container, false);
        ButterKnife.inject(this, view);

        startUpdate();
        setSizeSeek();
        setAnimationSeek();
        setDirectionSeek();

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
        return 5;
    }

    private void setSizeSeek() {
        mSeekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getProgress() != 0)
                    mIndicator.setSize(SizeUnit.PX, getSize(seekBar), getSize(mSeekHeight));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSeekHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getProgress() != 0)
                    mIndicator.setSize(SizeUnit.PX, getSize(mSeekWidth), getSize(seekBar));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mIndicator.setSize(SizeUnit.PX, getSize(mSeekWidth), getSize(mSeekHeight));
    }

    private void setDirectionSeek() {
        mSeekDirection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mIndicator.setDirection(Direction.values()[seekBar.getProgress() + 2]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Direction: " + (Direction.values()[seekBar.getProgress() + 2].name()));
            }
        });
        mIndicator.setDirection(Direction.LEFT_RIGHT);
    }

    private int getSize(SeekBar seekBar) {
        return (int) (mSize * ((float) seekBar.getProgress() / seekBar.getMax()));
    }
}
