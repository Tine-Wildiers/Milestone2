package com.example.milestone2.types;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RectangleScatter implements IShapeRenderer {

    private final float mWidth;
    private final float mHeight;

    public RectangleScatter(float width, float height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    @Override
    public void renderShape(Canvas c, IScatterDataSet dataSet, ViewPortHandler viewPortHandler, float posX, float posY, Paint renderPaint) {
        c.drawRect(posX - mWidth / 2f, posY - mHeight / 2f, posX + mWidth / 2f, posY + mHeight / 2f, renderPaint);
    }
}

