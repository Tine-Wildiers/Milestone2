package com.example.milestone2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.milestone2.types.CSVFile;
import com.example.milestone2.types.MyScatterDataSet;
import com.example.milestone2.types.RectangleScatter;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FrequencyDomain {
    private ScatterChart spectrogramChart;

    public int getSpectogramYRes() {
        return spectogramYRes;
    }

    private final int spectogramXRes = 15;
    private final int spectogramYRes = 16;
    MyScatterDataSet spectrogramDS;
    List<Integer> colors = new ArrayList<>();
    List<String> colorMapper = new ArrayList<String>();
    int index;
    String color;
    int colorMapperSize;
    double normalizedY;

    public FrequencyDomain() {

    }

    public void setSpectrogramChart(ScatterChart spectrogramChart) {
        this.spectrogramChart = spectrogramChart;
    }

    public void setColorPalette(List colorPalette){
        colorMapper = colorPalette;
        colorMapperSize = colorPalette.size();
    }

    public void setupSpectrogramGraph(){
        spectrogramChart.getDescription().setEnabled(false);
        spectrogramChart.getLegend().setEnabled(false);
        spectrogramChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        spectrogramChart.getRenderer().getPaintValues().setStyle(Paint.Style.FILL);

        List<Entry> entries = new ArrayList<>();



        for (int i = 0; i < spectogramXRes; i++) {
            for (int j = 0; j < spectogramYRes; j++){
                //int color = Color.HSVToColor(new float[]{(float) (i * j * 120) /(spectogramXRes*spectogramYRes), 1f, 1f});

                double calcVal = ( (double) (i * j) /(spectogramXRes*spectogramYRes) * colorMapperSize);
                index = Math.min((int) calcVal, colorMapperSize - 1);
                String argbVal = colorMapper.get(index);

                // Parse the ARGB value from the string
                int alpha = Integer.parseInt(argbVal.substring(0, 2), 16);
                int red = Integer.parseInt(argbVal.substring(2, 4), 16);
                int green = Integer.parseInt(argbVal.substring(4, 6), 16);
                int blue = Integer.parseInt(argbVal.substring(6, 8), 16);

                int color = Color.argb(alpha, red, green, blue);
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
    }

    public void updateSpectrogram(double[] downScaledArray){
        for (int i = 0; i < spectogramYRes; i++) {
            // Convert HSV color to RGB
            //int color = Color.HSVToColor(new float[]{(float) interpolateHue(downScaledArray[i]), 1f, 1f});


            String argbVal = interpolateARGB(downScaledArray[i]);

            // Parse the ARGB value from the string
            int alpha = Integer.parseInt(argbVal.substring(0, 2), 16);
            int red = Integer.parseInt(argbVal.substring(2, 4), 16);
            int green = Integer.parseInt(argbVal.substring(4, 6), 16);
            int blue = Integer.parseInt(argbVal.substring(6, 8), 16);

            int color = Color.argb(alpha, red, green, blue);
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

    private String interpolateARGB(double value){
        //TODO: heel deze methode efficienter maken
        //TODO: beter snappen waar die Y vandaan komt
        normalizedY = (value +50);
        index = Math.min((int) (normalizedY * colorMapperSize / 100f), colorMapperSize - 1);
        color = colorMapper.get(index);
        return color;
    }
}
