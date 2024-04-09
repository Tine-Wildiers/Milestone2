package com.example.milestone2.types;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.List;

public class MyScatterDataSet extends ScatterDataSet {
    static int currentIndex = 0;

    public MyScatterDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getColor(int index) {
        int ret =super.getColor(currentIndex);
        currentIndex+=1;
        return ret;
    }
}
