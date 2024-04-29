package com.example.milestone2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import com.example.milestone2.types.CSVFile;
import com.example.milestone2.types.MyScatterDataSet;
import com.example.milestone2.types.RectangleScatter;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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

    private final int spectogramXRes = 30;
    private final int spectogramYRes = 32;
    MyScatterDataSet spectrogramDS;
    List<Integer> colors = new ArrayList<>();
    List<Integer> colorMapper = new ArrayList<Integer>();
    List<Integer> rgbMapper = new ArrayList<Integer>();
    int index;
    int colorMapperSize;

    public FrequencyDomain() {

    }

    public void setSpectrogramChart(ScatterChart spectrogramChart) {
        this.spectrogramChart = spectrogramChart;
    }

    public void setRGBPalette(List<List<Float>> colorPalette){
        colorMapperSize = colorPalette.size();
        for (List<Float> row : colorPalette) {
            float red = row.get(0);
            float green = row.get(1);
            float blue = row.get(2);
            rgbMapper.add(Color.argb(1, red, green, blue));
        }
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
                int color = getRGBColor(index);
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
            index = Math.min((int) ((downScaledArray[i] +50) * colorMapperSize / 100f), colorMapperSize - 1);
            int color = rgbMapper.get(index);

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

    public int getRGBColor(int index){
        if(index<0){
            index = 0;
        } else if (index>=colorMapperSize) {
            index = colorMapperSize-1;
        }
        return rgbMapper.get(index);
    }

    public int getColorMapperSize(){
        return colorMapperSize;
    }
}
