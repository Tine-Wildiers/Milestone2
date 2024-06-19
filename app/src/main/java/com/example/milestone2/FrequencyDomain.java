package com.example.milestone2;

import android.graphics.Color;
import android.graphics.Paint;

import com.example.milestone2.types.MyScatterDataSet;
import com.example.milestone2.types.RectangleScatter;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;

import java.util.ArrayList;
import java.util.List;

public class FrequencyDomain {
    private ScatterChart spectrogramChart;
    private final int spectogramXRes = 128;
    private final int spectogramYRes = 48;
    MyScatterDataSet spectrogramDS;
    List<Integer> colors = new ArrayList<>();
    int index;
    private ColorMapper cM;

    public FrequencyDomain() {
    }

    public void setSpectrogramChart(ScatterChart spectrogramChart, ColorMapper cM) {
        this.spectrogramChart = spectrogramChart;
        this.cM = cM;

        spectrogramChart.getDescription().setEnabled(false);
        spectrogramChart.getLegend().setEnabled(false);
        spectrogramChart.setTouchEnabled(false);
        spectrogramChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        spectrogramChart.getRenderer().getPaintValues().setStyle(Paint.Style.FILL);

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < spectogramXRes; i++) {
            for (int j = 0; j < spectogramYRes; j++){
                int color = cM.getColor(1);
                colors.add(color);
                entries.add(new Entry(i, j));
            }
        }

        spectrogramDS = new MyScatterDataSet(entries, "Custom Data Set");
        spectrogramDS.setShapeRenderer(new RectangleScatter((float) spectrogramChart.getLayoutParams().width /spectogramXRes, (float) spectrogramChart.getLayoutParams().height /spectogramYRes));
        spectrogramDS.setColors(colors);
        ScatterData scatterData = new ScatterData(spectrogramDS);

        XAxis xAxis = spectrogramChart.getXAxis();
        xAxis.setEnabled(false);
        YAxis yAxis = spectrogramChart.getAxisLeft();
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setDrawGridLines(false);

        YAxis rightYAxis = spectrogramChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setEnabled(false);
        spectrogramChart.setData(scatterData);
        spectrogramChart.invalidate();
        spectrogramChart.getAxisLeft().setDrawLabels(false);
        spectrogramChart.buildDrawingCache();
    }

    public int updateSpectrogram(double[] downScaledArray, int ind){


        for (int i = 0; i < spectogramYRes; i++) {
            index = Math.min((int) ((downScaledArray[i] +50) * cM.getColorMapperSize() / 100f), cM.getColorMapperSize() - 1);
            int color = cM.getColor(index);
            colors.set(ind, color);
            ind+=1;
        }

        return ind;
    }

    public void renderSpectrogram(){
        spectrogramDS.setColors(colors);
        ScatterData scatterData = new ScatterData(spectrogramDS);
        spectrogramChart.setData(scatterData);
        spectrogramChart.invalidate();
    }

    public void resetFrequencyPlot() {
        List<Integer> newcolors = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < spectogramXRes; i++) {
            for (int j = 0; j < spectogramYRes; j++){
                int color = cM.getColor(1);
                newcolors.add(color);
                entries.add(new Entry(i, j));
            }
        }

        colors=newcolors;
        renderSpectrogram();
    }

    int getMaxSize(){
        return spectogramXRes*spectogramYRes;
    }

    public int getSpectogramYRes() {
        return spectogramYRes;
    }

}
