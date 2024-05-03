package com.example.milestone2;

import android.graphics.Bitmap;

import java.io.File;

public class Measurement {
    Bitmap image;
    String classification;
    float[] confidences;
    File wavFile;
    int location;
    int epoch;

    public void setWavFile(File wavFile) {
        this.wavFile = wavFile;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public Measurement(Bitmap image, float[] confidences) {
        this.image = image;
        this.confidences = confidences;
        int maxPos = 0;
        float maxConfidence = 0;
        for(int i = 0; i < confidences.length; i++){
            if(confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }
        String[] classes = {"0", "1", "2", "3"};

        this.classification = classes[maxPos];
    }
}
