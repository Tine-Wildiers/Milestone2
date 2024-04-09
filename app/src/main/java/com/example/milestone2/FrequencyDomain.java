package com.example.milestone2;

import android.graphics.Color;
import android.graphics.Paint;

import com.example.milestone2.types.MyScatterDataSet;
import com.example.milestone2.types.RectangleScatter;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;

import java.util.ArrayList;
import java.util.List;

public class FrequencyDomain {
    private ScatterChart spectrogramChart;

    public int getSpectogramYRes() {
        return spectogramYRes;
    }

    private final int spectogramXRes = 30;
    private final int spectogramYRes = 64;
    MyScatterDataSet spectrogramDS;
    List<Integer> colors = new ArrayList<>();

    public FrequencyDomain() {
    }

    public void setSpectrogramChart(ScatterChart spectrogramChart) {
        this.spectrogramChart = spectrogramChart;
    }

    public void setupSpectrogramGraph(){
        spectrogramChart.getDescription().setEnabled(false);
        spectrogramChart.getLegend().setEnabled(false);
        spectrogramChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        spectrogramChart.getRenderer().getPaintValues().setStyle(Paint.Style.FILL);

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < spectogramXRes; i++) {
            for (int j = 0; j < spectogramYRes; j++){
                int color = Color.HSVToColor(new float[]{(float) (i * j * 120) /(spectogramXRes*spectogramYRes), 1f, 1f});
                colors.add(color);
                entries.add(new Entry(i, j));
            }
        }

        spectrogramDS = new MyScatterDataSet(entries, "Custom Data Set");
        spectrogramDS.setShapeRenderer(new RectangleScatter((float) spectrogramChart.getLayoutParams().width /spectogramXRes, spectrogramChart.getLayoutParams().height/spectogramYRes));

        spectrogramDS.setColors(colors);
        ScatterData scatterData = new ScatterData(spectrogramDS);
        spectrogramChart.setData(scatterData);
        spectrogramChart.invalidate();
    }

    public void updateSpectrogram(double[] downScaledArray){
        for (int i = 0; i < spectogramYRes; i++) {
            // Convert HSV color to RGB
            int color = Color.HSVToColor(new float[]{(float) interpolateHue(downScaledArray[i]), 1f, 1f});
            colors.add(color);
        }

        while(colors.size() > spectogramYRes*spectogramXRes){
            colors.remove(0);
        }
    }

    public void renderSpectrogram(){
        spectrogramDS.setColors(colors);
        ScatterData scatterData = new ScatterData(spectrogramDS);
        spectrogramChart.setData(scatterData);
        spectrogramChart.invalidate();
    }

    private double interpolateHue(double yValue) {
        double normalizedY = (yValue +20) / 100f;
        return 240f * (1f - normalizedY);
    }
}
