package com.example.milestone2.types;

public class AudioData {
    public final int pointsCount;
    public final LongValues xData;
    public final ShortValues yData;

    public AudioData(int pointsCount) {
        this.pointsCount = pointsCount;
        this.xData = new LongValues(pointsCount);
        this.yData = new ShortValues(pointsCount);

        this.xData.setSize(pointsCount);
        this.yData.setSize(pointsCount);
    }
}
