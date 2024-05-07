package com.example.milestone2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

public class Measurement implements Parcelable {
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

    // Parcelable implementation
    protected Measurement(Parcel in) {
        //byte[] byteArray = new byte[in.readInt()];
        //in.readByteArray(byteArray);
        //image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        classification = in.readString();
        confidences = in.createFloatArray();
        wavFile = (File) in.readSerializable();
        location = in.readInt();
        epoch = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //byte[] byteArray = stream.toByteArray();
        //dest.writeInt(byteArray.length);
        //dest.writeByteArray(byteArray);
        dest.writeString(classification);
        dest.writeFloatArray(confidences);
        dest.writeSerializable(wavFile);
        dest.writeInt(location);
        dest.writeInt(epoch);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Measurement> CREATOR = new Creator<Measurement>() {
        @Override
        public Measurement createFromParcel(Parcel in) {
            return new Measurement(in);
        }

        @Override
        public Measurement[] newArray(int size) {
            return new Measurement[size];
        }
    };
}
