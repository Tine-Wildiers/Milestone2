package com.example.milestone2;

import android.graphics.Color;

import com.example.milestone2.readers.CSVFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ColorMapper {
    List<Integer> colorMapper = new ArrayList<Integer>();
    int colorMapperSize;

    public ColorMapper(InputStream rgbValues) {
        CSVFile csvFile = new CSVFile(rgbValues);
        setRGBPalette(csvFile.readRGB());
    }

    public void setRGBPalette(List<List<Float>> colorPalette){
        colorMapperSize = colorPalette.size();
        for (List<Float> row : colorPalette) {
            float red = row.get(0);
            float green = row.get(1);
            float blue = row.get(2);
            colorMapper.add(Color.argb(1, red, green, blue));
        }
    }

    public int getColor(int index){
        if(index<0){
            index = 0;
        } else if (index>=colorMapperSize) {
            index = colorMapperSize-1;
        }
        return colorMapper.get(index);
    }

    public int getColorMapperSize(){
        return colorMapperSize;
    }
}
