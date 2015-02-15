package com.pletikosa.indicators.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.pletikosa.indicators.circle.CircleIndicator;
import com.pletikosa.indicators.line.LineIndicator;
import com.pletikosa.indicators.pie.PieIndicator;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IndicatorsActivity extends ActionBarActivity {

    private int mStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        final CircleIndicator circle = (CircleIndicator) findViewById(R.id.circle);
        final LineIndicator line = (LineIndicator) findViewById(R.id.line);
        final PieIndicator pie = (PieIndicator) findViewById(R.id.pie);
        final PieIndicator pieHalf = (PieIndicator) findViewById(R.id.pie_half);
        final PieIndicator pieQuarter = (PieIndicator) findViewById(R.id.pie_quarter);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStep = (int) (20 * new Random().nextFloat());
                        circle.indicate(mStep);
                        line.indicate(mStep);
                        pie.indicate(mStep);
                        pieHalf.indicate(mStep);
                        pieQuarter.indicate(mStep);
                    }
                });
            }
        }, 0, 2, TimeUnit.SECONDS);
    }
}
