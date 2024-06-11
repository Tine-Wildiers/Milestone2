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
    private final int maxSize = 1033; //In setup method nog aanpassing nodig als ge deze naar 2000 doet bvb.
    private int zoomLevel = 1500;
    public int index = 0;

    public TimeDomain(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    public void setTimeChart(LineChart tC) {
        timeChart = tC;
        setupTimeChart();
    }

    public void setupTimeChart(){
        //General setup
        Description description = new Description();
        description.setEnabled(false);
        timeChart.setDescription(description);
        timeChart.setTouchEnabled(false);
        timeChart.getLegend().setEnabled(false);
        timeChart.getAxisRight().setDrawLabels(false);
        timeChart.getAxisLeft().setDrawLabels(false);
        timeChart.getXAxis().setDrawLabels(false);
        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-zoomLevel);
        yAxis.setAxisMaximum(+zoomLevel);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setDrawGridLines(false);
        YAxis rightYAxis = timeChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        XAxis xAxis = timeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        //xAxis.setGridLineWidth(1f);
        //xAxis.setGridColor(Color.GRAY);

        /*
        // Setup X axis labels
        float[] gridLinePositions = {172f, 344f, 516f, 860f, 1032f};
        List<String> timeLabels = new ArrayList<>();
        for (int i = 0; i < gridLinePositions.length; i++) {
            timeLabels.add(String.format(Locale.getDefault(), "%d", i));
        }

        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeLabels));


         */
        setXLabels();

        // Initiate dataset
        int i = 0;
        while (timePlot.size() != maxSize) {
            timePlot.add(new Entry(i, 0.0f));
            i += 1;
        }

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

        setXLabels();

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
        index=0;
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
        zoomLevel -= 300;
        if(zoomLevel <300){
            zoomLevel = 300;
        }
        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-zoomLevel);
        yAxis.setAxisMaximum(+zoomLevel);
        timeChart.invalidate();
    }

    public void zoomOut(){
        zoomLevel += 300;
        YAxis yAxis = timeChart.getAxisLeft();
        yAxis.setAxisMinimum(-zoomLevel);
        yAxis.setAxisMaximum(+zoomLevel);
        timeChart.invalidate();
    }

    public void updateFrameIndex(){
        index+=1;
    }

    public void setXLabels() {
        XAxis xAxis = timeChart.getXAxis();
        String[] initialTimes = {"00:01", "00:02", "00:03", "00:04", "00:05", "00:06"};
        float[] positions = {172f, 344f, 516f, 688f, 860f, 1032f};

        for (int i = 0; i < positions.length; i++) {
            String[] timeParts = initialTimes[i].split(":");
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = Integer.parseInt(timeParts[1]);

            int totalSeconds = (minutes * 60 + seconds) + index*6;
            int newMinutes = totalSeconds / 60;
            int newSeconds = totalSeconds % 60;

            String newLabel = String.format(Locale.getDefault(), "%02d:%02d", newMinutes, newSeconds);

            LimitLine limitLine = new LimitLine(positions[i], newLabel);
            limitLine.setLineColor(Color.GRAY);
            limitLine.setLineWidth(1f);
            limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            xAxis.addLimitLine(limitLine);
        }
    }

}
