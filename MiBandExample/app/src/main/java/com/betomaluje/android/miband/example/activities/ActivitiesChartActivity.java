package com.betomaluje.android.miband.example.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.betomaluje.android.miband.example.R;
import com.betomaluje.miband.DateUtils;
import com.betomaluje.miband.models.ActivityData;
import com.betomaluje.miband.sqlite.ActivitySQLite;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by betomaluje on 7/16/15.
 */
public class ActivitiesChartActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();
    private BarChart mChart;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_sleepchart;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.pieChart).setVisibility(View.GONE);

        createGraph();
        createChartLegend();

        populateChart();
    }

    private void createGraph() {
        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.setDescription("");

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);

        mChart.setDrawGridBackground(false);
    }

    private void createChartLegend() {
        /*
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        l.setYOffset(0f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);
        */

        XAxis x = mChart.getXAxis();
        x.setDrawLabels(true);
        x.setDrawGridLines(false);
        x.setEnabled(true);
        x.setDrawLimitLinesBehindData(true);

        YAxis y = mChart.getAxisLeft();
        y.setAxisMaxValue(1f);
        y.setDrawTopYLabelEntry(false);
        y.setEnabled(true);

        mChart.getAxisRight().setEnabled(false);
    }

    private void populateChart() {
        Calendar before = Calendar.getInstance();
        before.add(Calendar.DAY_OF_WEEK, -7);

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        //Log.i(TAG, "data from " + DateUtils.convertString(before) + " to " + DateUtils.convertString(today));

        ArrayList<ActivityData> allActivities = ActivitySQLite.getInstance(ActivitiesChartActivity.this)
                .getActivitySamples(before.getTimeInMillis(), today.getTimeInMillis());
        //.getAllActivities();

        ArrayList<String> xVals = new ArrayList<String>();

        ArrayList<BarEntry> unknown = new ArrayList<BarEntry>();

        int i = 0;

        Calendar cal = Calendar.getInstance();
        cal.clear();
        Date date;
        String dateStringFrom = "";
        String dateStringTo = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        SimpleDateFormat annotationDateFormat = new SimpleDateFormat("HH:mm");

        float movementDivisor = 180.0f;

        float value;

        for (ActivityData ad : allActivities) {

            // determine start and end dates
            if (i == 0) {
                cal.setTimeInMillis(ad.getTimestamp() * 1000L); // make sure it's converted to long
                date = cal.getTime();
                dateStringFrom = dateFormat.format(date);
            } else if (i == allActivities.size() - 1) {
                cal.setTimeInMillis(ad.getTimestamp() * 1000L); // same here
                date = cal.getTime();
                dateStringTo = dateFormat.format(date);
            }

            String xLabel = "";
            cal.setTimeInMillis(ad.getTimestamp() * 1000L);
            date = cal.getTime();
            String dateString = annotationDateFormat.format(date);
            xLabel = dateString;

            xVals.add(xLabel);

            Log.i(TAG, "date " + dateString);
            Log.i(TAG, "steps " + ad.getSteps());

            value = ((float) ad.getIntensity() / movementDivisor);

            unknown.add(new BarEntry(value, i));

            i++;
        }

        //BarDataSet set1 = new BarDataSet(deep, "Deep Sleep");
        //set1.setColor(Color.BLUE);
        //BarDataSet set2 = new BarDataSet(light, "Light Sleep");
        //set2.setColor(Color.CYAN);
        BarDataSet set3 = new BarDataSet(unknown, "Activity");
        set3.setColor(Color.RED);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        //dataSets.add(set1);
        //dataSets.add(set2);
        dataSets.add(set3);

        BarData data = new BarData(xVals, dataSets);
//        data.setValueFormatter(new LargeValueFormatter());

        // add space between the dataset groups in percent of bar-width
        data.setGroupSpace(0);

        mChart.setDescription(String.format("From %1$s to %2$s",
                dateStringFrom, dateStringTo));
        mChart.setData(data);
        mChart.invalidate();

        mChart.animateY(2000, Easing.EasingOption.EaseInOutQuart);
    }
}
