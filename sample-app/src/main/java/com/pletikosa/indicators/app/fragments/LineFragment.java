package com.pletikosa.indicators.app.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.SizeUnit;
import com.pletikosa.indicators.line.LineIndicator;
import com.pletikosa.indicators.app.MainActivity;
import com.pletikosa.indicators.app.R;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LineFragment extends Fragment {

    @InjectView(R.id.seek_width)
    SeekBar mSeekWidth;
    @InjectView(R.id.seek_height)
    SeekBar mSeekHeight;
    @InjectView(R.id.seek_animation)
    SeekBar mAnimationSeek;
    @InjectView(R.id.seek_direction)
    SeekBar mSeekDirection;
    @InjectView(R.id.seek_corner_radius)
    SeekBar mSeekCorners;

    @InjectView(R.id.line_indicator)
    LineIndicator mIndicator;

    private int mSize;

    public static LineFragment newInstance() {
        return new LineFragment();
    }

    public LineFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis) {
        View view = inflater.inflate(R.layout.fragment_line, container, false);
        ButterKnife.inject(this, view);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mSize = (int) (metrics.widthPixels - (2 * marginToPx()));

        startUpdate();
        setSizeSeek();
        setAnimationSeek();
        setDirectionSeek();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(1);
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
                        final float value = rand.nextFloat() * 100;
                        mIndicator.indicate(value);
                    }
                });
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void setSizeSeek() {
        mSeekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIndicator.setSize(SizeUnit.PX, getSize(seekBar), getSize(mSeekHeight));
            }
        });

        mSeekHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIndicator.setSize(SizeUnit.PX, getSize(mSeekWidth), getSize(seekBar));
            }
        });

        mIndicator.setSize(SizeUnit.PX, getSize(mSeekWidth), getSize(mSeekHeight));
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
                mIndicator.setAnimationDuration(seekBar.getProgress());
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
                mIndicator.setDirection(progress == 0 ? Direction.LEFT_RIGHT : progress == 1 ?
                        Direction.RIGHT_LEFT : progress == 2 ? Direction.BOTTOM_TOP : Direction
                        .TOP_BOTTOM);
            }
        });
        mIndicator.setDirection(Direction.LEFT_RIGHT);
    }

    private int getSize(SeekBar seekBar) {
        return (int) (mSize * ((float) seekBar.getProgress() / seekBar.getMax()));
    }

    private float marginToPx() {
        Resources resources = getActivity().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return getActivity().getResources().getDimension(R.dimen.activity_margin) * (metrics.densityDpi / 160f);
    }
}
