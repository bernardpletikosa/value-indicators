package com.github.bernardpletikosa.indicators.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.circle.CircleIndicator;
import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.Orientation;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;
import com.github.bernardpletikosa.indicators.line.LineIndicator;
import com.github.bernardpletikosa.indicators.pie.HalfPieIndicator;
import com.github.bernardpletikosa.indicators.pie.PieIndicator;
import com.github.bernardpletikosa.indicators.pie.QuarterPieIndicator;
import com.github.bernardpletikosa.indicators.triangle.TriangleIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IndicatorsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        final List<IndicatorView> indicators = getAllIndicators(findViewById(R.id.indicators));

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float step = (20f * new Random().nextFloat()) - 10f;
                        for (IndicatorView in : indicators) in.indicate(step);
                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private List<IndicatorView> getAllIndicators(View viewById) {
        List<View> all = getAllChildren(viewById);

        List<IndicatorView> indicators = new ArrayList<>();
        for (View v : all) if (v instanceof IndicatorView) indicators.add((IndicatorView) v);

        return indicators;
    }

    private List<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup))
            return Arrays.asList(v);

        List<View> result = new ArrayList<>();
        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            List<View> views = new ArrayList<>();
            views.add(v);
            views.addAll(getAllChildren(viewGroup.getChildAt(i)));

            result.addAll(views);
        }

        return result;
    }

    //How to create an indicator
    //    private void setIndicators() {
    //        final CircleIndicator circle = (CircleIndicator) findViewById(R.id.circle);
    //        circle.setRange(0, 40);
    //        circle.setAnimationDuration(500);
    //        circle.setMainColor(getResources().getColor(R.color.main));
    //        circle.setBackGroundColor(getResources().getColor(R.color.background));
    //        circle.setAnimationListener(null);
    //        circle.setInterpolator(new AccelerateDecelerateInterpolator());
    //        circle.setRadius(SizeUnit.DP, 100);
    //
    //        final LineIndicator line = (LineIndicator) findViewById(R.id.line);
    //        line.setRange(0, 20);
    //        line.setAnimationDuration(500);
    //        line.setMainColor(getResources().getColor(R.color.main));
    //        line.setBackGroundColor(getResources().getColor(R.color.background));
    //        line.setAnimationListener(null);
    //        line.setInterpolator(new AccelerateDecelerateInterpolator());
    //        line.setDirection(Direction.LEFT_RIGHT);
    //        line.setSize(SizeUnit.DP, 200, 50);
    //
    //        final PieIndicator pie = (PieIndicator) findViewById(R.id.pie);
    //        pie.setRange(0, 20);
    //        pie.setAnimationDuration(500);
    //        pie.setMainColor(getResources().getColor(R.color.main));
    //        pie.setBackGroundColor(getResources().getColor(R.color.background));
    //        pie.setAnimationListener(null);
    //        pie.setInterpolator(new AccelerateDecelerateInterpolator());
    //        pie.setCenterPaint(getResources().getColor(R.color.center));
    //        pie.setDirection(Direction.CLOCKWISE);
    //        pie.setRadius(SizeUnit.DP, 100);
    //        pie.setInnerRadius(70);
    //        pie.setStartingAngle(0);
    //
    //        final HalfPieIndicator pieHalf = (HalfPieIndicator) findViewById(R.id.pie_half);
    //        pieHalf.setRange(0, 20);
    //        pieHalf.setAnimationDuration(500);
    //        pieHalf.setMainColor(getResources().getColor(R.color.main));
    //        pieHalf.setBackGroundColor(getResources().getColor(R.color.background));
    //        pieHalf.setAnimationListener(null);
    //        pieHalf.setInterpolator(new AccelerateDecelerateInterpolator());
    //        pieHalf.setCenterPaint(getResources().getColor(R.color.center));
    //        pieHalf.setDirection(Direction.CLOCKWISE);
    //        pieHalf.setRadius(SizeUnit.DP, 100);
    //        pieHalf.setInnerRadius(70);
    //        pieHalf.setOrientation(Orientation.NORTH);
    //
    //        final QuarterPieIndicator pieQuarter = (QuarterPieIndicator) findViewById(R.id.pie_quarter);
    //        pieQuarter.setRange(0, 20);
    //        pieQuarter.setAnimationDuration(500);
    //        pieQuarter.setMainColor(getResources().getColor(R.color.main));
    //        pieQuarter.setBackGroundColor(getResources().getColor(R.color.background));
    //        pieQuarter.setAnimationListener(null);
    //        pieQuarter.setInterpolator(new AccelerateDecelerateInterpolator());
    //        pieQuarter.setCenterPaint(getResources().getColor(R.color.center));
    //        pieQuarter.setDirection(Direction.CLOCKWISE);
    //        pieQuarter.setRadius(SizeUnit.DP, 100);
    //        pieQuarter.setInnerRadius(70);
    //        pieQuarter.setOrientation(Orientation.NORTH_EAST);
    //    }
}
