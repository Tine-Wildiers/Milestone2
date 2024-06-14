package com.example.milestone2.readers;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFileReader {
    public static float[] readWavFile(InputStream inputStream) {
        try {
            byte[] header = new byte[44];
            inputStream.read(header, 0, header.length);

            int channels = 1;
            int bitsPerSample = 16;

            int numSamples = (inputStream.available() - 44) / (channels * (bitsPerSample / 8));
            double[] waveform = new double[numSamples];

            byte[] data = new byte[numSamples * channels * (bitsPerSample / 8)];
            inputStream.read(data, 0, data.length);

            for (int i = 0; i < numSamples; i++) {
                if (bitsPerSample == 16) {
                    short sample = ByteBuffer.wrap(data, i * 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    waveform[i] = (double) sample / Short.MAX_VALUE;
                } else if (bitsPerSample == 8) {
                    byte sample = data[i];
                    waveform[i] = (double) (sample - 128) / Byte.MAX_VALUE;
                }
            }

            //I noticed that for some reason, the method I use always has 3541 to much values that don't make sense. This is how I cut them off.
            int size = waveform.length - 3541;
            float[] fltWaveform = new float[size];

            // Converting double[] to float[]
            for (int i = 0; i < size; i++) {
                fltWaveform[i] = (float) waveform[i];
            }

            return fltWaveform;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

