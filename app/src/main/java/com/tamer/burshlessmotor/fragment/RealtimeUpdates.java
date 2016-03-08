package com.tamer.burshlessmotor.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.tamer.burshlessmotor.R;

import java.util.Random;

/**
 * Created by liangzr on 16-3-2.
 */
public class RealtimeUpdates extends Fragment {
    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private LineGraphSeries<DataPoint> mSeries;
    private double graphLastXValue = 5d;
    private int speedData = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_speed, container, false);

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);
        /* 设置标题 */
        graph.setTitle("即时速度");
        graph.getGridLabelRenderer().setVerticalAxisTitle("速度 m/s");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("时间 100ms/格");
        /* 设置样式 */
        graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(30);
        graph.setTitleTextSize(50);

        return rootView;
    }

    public void setSpeedData(String speedData) {
        this.speedData = Integer.parseInt(speedData);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Runnable() {
            @Override
            public void run() {
                graphLastXValue += 1d;
                mSeries.appendData(new DataPoint(graphLastXValue, speedData), true, 100);
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer, 100);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer);
        super.onPause();
    }


    double mLastRandom = 10;
    Random mRand = new Random();

    private double getData() {
        return mLastRandom += mRand.nextDouble() * 0.5 - 0.25;
    }
}
