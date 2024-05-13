package com.example.milestone2;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimeDomain {
    public List<Entry> timePlot = new ArrayList<>();
    private LineChart timeChart;
    private final int sampleRate;
    private final int maxSize = 1000; //In setup method nog aanpassing nodig als ge deze naar 2000 doet bvb.

    public TimeDomain(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    public void setTimeChart(LineChart timeChart) {
        this.timeChart = timeChart;
        setupTimeChart();
    }

    public void setupTimeChart(){
        /*
        //int[] verticalGridLines = getVerticalGridLineLocations(sampleRate, -256000*2, -256000 + maxSize * 256); als ge maxSize verdubbelt
        int[] verticalGridLines = getVerticalGridLineLocations(sampleRate, -256000, -256000 + maxSize * 256);
        float[] gridLinePositions = new float[verticalGridLines.length];
        for (int i = 0; i < verticalGridLines.length; i++) {
            gridLinePositions[i] = verticalGridLines[i];
        }

        //int i = -256000*2;
        int i = -256000;
        while (timePlot.size() !=  maxSize) {
            timePlot.add(new Entry(i, 0.0f));
            i+= 256;
        }

        // Calculate the duration of each sample in seconds
        double sampleDurationInSeconds = 1.0 / sampleRate;

        // Calculate the time values for each grid line
        List<String> timeLabels = new ArrayList<>();
        for (float gridLinePosition : gridLinePositions) {
            double timeInSeconds = gridLinePosition * sampleDurationInSeconds;
            timeLabels.add(String.format(Locale.getDefault(), "%.2f", timeInSeconds));
        }

         */

        int i = 0;
        while (timePlot.size() !=  maxSize) {
            timePlot.add(new Entry(i, 0.0f));
            i+= 1;
        }

        XAxis xAxis = timeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1f);
        xAxis.setGridColor(Color.GRAY);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        //xAxis.setValueFormatter(new IndexAxisValueFormatter(timeLabels));

        Description description = new Description();
        description.setEnabled(false);
        timeChart.setDescription(description);
        timeChart.setTouchEnabled(false);
        timeChart.getLegend().setEnabled(false);
        timeChart.getAxisRight().setDrawLabels(false);
        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-5000);
        yAxis.setAxisMaximum(+5000);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setDrawGridLines(false);

        YAxis rightYAxis = timeChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        timeChart.invalidate();
    }

    protected void updateTimeGraph(){

        while (timePlot.size() > maxSize) {
            timePlot.remove(0);
        }
        // Set a gridline for each second that passes
        int firstIndex = (int) timePlot.get(0).getX();
        int lastIndex = (int) timePlot.get(timePlot.size() - 1).getX();
        int[] verticalGridLines = getVerticalGridLineLocations(sampleRate, firstIndex, lastIndex);
        float[] gridLinePositions = new float[verticalGridLines.length];
        for (int i = 0; i < verticalGridLines.length; i++) {
            gridLinePositions[i] = verticalGridLines[i];
        }
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
        int i = -256000;
        while (timePlot.size() !=  maxSize) {
            timePlot.add(new Entry(i, 0.0f));
            i+= 256;
        }
    }

    int getMaxSize(){
        return maxSize;
    }

}
