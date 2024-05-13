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

    private final int spectogramXRes = 50;
    private final int spectogramYRes = 32;
    MyScatterDataSet spectrogramDS;
    List<Integer> colors = new ArrayList<>();
    int index;
    private ColorMapper cM;

    public FrequencyDomain() {
    }

    public void setSpectrogramChart(ScatterChart spectrogramChart) {
        this.spectrogramChart = spectrogramChart;
        setupSpectrogramGraph();
    }

    public void setupSpectrogramGraph(){
        spectrogramChart.getDescription().setEnabled(false);
        spectrogramChart.getLegend().setEnabled(false);
        spectrogramChart.setTouchEnabled(false);
        spectrogramChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        spectrogramChart.getRenderer().getPaintValues().setStyle(Paint.Style.FILL);

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < spectogramXRes; i++) {
            for (int j = 0; j < spectogramYRes; j++){
                //int color = Color.HSVToColor(new float[]{(float) (i * j * 120) /(spectogramXRes*spectogramYRes), 1f, 1f});
                double calcVal = ( (double) (i * j) /(spectogramXRes*spectogramYRes) * cM.getColorMapperSize());
                index = Math.min((int) calcVal, cM.getColorMapperSize() - 1);
                int color = cM.getColor(index);
                colors.add(color);
                entries.add(new Entry(i, j));
            }
        }

        spectrogramDS = new MyScatterDataSet(entries, "Custom Data Set");
        spectrogramDS.setShapeRenderer(new RectangleScatter((float) spectrogramChart.getLayoutParams().width /spectogramXRes, (float) spectrogramChart.getLayoutParams().height /spectogramYRes));
        spectrogramDS.setColors(colors);
        ScatterData scatterData = new ScatterData(spectrogramDS);

        spectrogramChart.setData(scatterData);
        spectrogramChart.invalidate();


        spectrogramChart.buildDrawingCache();
    }

    public void updateSpectrogram(double[] downScaledArray){
        for (int i = 0; i < spectogramYRes; i++) {

            // --- old version ---
            //int color = Color.HSVToColor(new float[]{(float) interpolateHue(downScaledArray[i]), 1f, 1f});

            // --- new version ---
            index = Math.min((int) ((downScaledArray[i] +50) * cM.getColorMapperSize() / 100f), cM.getColorMapperSize() - 1);
            int color = cM.getColor(index);

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

    public void setColorMapper(ColorMapper cM) {
        this.cM = cM;
    }
}
