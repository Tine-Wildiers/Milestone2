package com.example.milestone2;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TimeDomain {
    public List<Entry> timePlot = new ArrayList<>();
    private LineChart timeChart;
    private final int sampleRate;
    private final int maxSize = 1000; //In setup method nog aanpassing nodig als ge deze naar 2000 doet bvb.
    private int zoomLevel = 1500;

    public TimeDomain(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    public void setTimeChart(LineChart tC) {
        timeChart = tC;
        setupTimeChart();
    }

    public void setupTimeChart(){
        int i = 0;
        while (timePlot.size() != maxSize) {
            timePlot.add(new Entry(i, 0.0f));
            i += 1;
        }

        XAxis xAxis = timeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1f);
        xAxis.setGridColor(Color.GRAY);
        xAxis.setGranularityEnabled(true); // Enable granularity
        xAxis.setGranularity(344f);

        // Set custom labels for gridlines
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 344f) {
                    return "2"; // Label for 344
                } else if (value == 688f) {
                    return "4"; // Label for 688
                } else {
                    return ""; // Hide labels for other gridlines
                }
            }
        });

        Description description = new Description();
        description.setEnabled(false);
        timeChart.setDescription(description);
        timeChart.setTouchEnabled(false);
        timeChart.getLegend().setEnabled(false);
        timeChart.getAxisRight().setDrawLabels(false);

        timeChart.getAxisLeft().setDrawLabels(false);

        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-zoomLevel);
        yAxis.setAxisMaximum(+zoomLevel);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setDrawGridLines(false);

        YAxis rightYAxis = timeChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);

        LineDataSet dataSet = new LineDataSet(timePlot, "SoundWave");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(1f);
        dataSet.setDrawCircles(false);
        LineData lineData = new LineData(dataSet);

        timeChart.setData(lineData);
        timeChart.invalidate();

    }

    protected void updateTimeGraph(int timeindex){

        while (timePlot.size() > maxSize) {
            timePlot.remove(0);
        }

        XAxis xAxis = timeChart.getXAxis();
        xAxis.removeAllLimitLines(); // Remove any existing limit lines

        LimitLine limitLine = new LimitLine(timeindex, "");
        limitLine.setLineColor(Color.RED);
        limitLine.setLineWidth(2f);
        xAxis.addLimitLine(limitLine);

        // Update dataSet
        LineDataSet dataSet = new LineDataSet(timePlot, "SoundWave");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(1f);
        dataSet.setDrawCircles(false);
        LineData lineData = new LineData(dataSet);

        timeChart.setData(lineData);
        timeChart.invalidate();
    }

    public static int[] getVerticalGridLineLocations(int rate, int minRange, int maxRange) {
        int start = (minRange % rate == 0) ? minRange : minRange + (rate - (minRange % rate));
        int count = (maxRange - start) / rate + 1;
        int[] multiples = new int[count];

        for (int i = 0; i < count; i++) {
            multiples[i] = start + i * rate;
        }
        return multiples;
    }

    void resetTimePlot(){
        timePlot = new ArrayList<>();
        //int i = -256000;
        int i = 0;
        while (timePlot.size() !=  maxSize) {
            timePlot.add(new Entry(i, 0.0f));
            //i+= 256;
            i+=1;
        }
    }

    int getMaxSize(){
        return maxSize;
    }

    public void zoomIn(){
        zoomLevel -= 200;
        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-zoomLevel);
        yAxis.setAxisMaximum(+zoomLevel);
        timeChart.invalidate();
    }

    public void zoomOut(){
        zoomLevel += 200;
        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-zoomLevel);
        yAxis.setAxisMaximum(+zoomLevel);
        timeChart.invalidate();
    }
}
