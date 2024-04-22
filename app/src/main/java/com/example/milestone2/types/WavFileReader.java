package com.example.milestone2.types;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.milestone2.R;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFileReader {
    public static float[] readWavFile(InputStream inputStream) {
        try {
            // Open the WAV file from the resources


            byte[] header = new byte[44];
            inputStream.read(header, 0, header.length);

            int sampleRate = ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int channels = 1;
            int bitsPerSample = 16;

            int numSamples = (inputStream.available() - 44) / (channels * (bitsPerSample / 8));
            //TODO: valt te overwegen om hier floats te gebruiken, gebruikt helft van het geheugen
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
            float[] fltWaveform = new float[waveform.length];

            // Converting double[] to float[]
            for (int i = 0; i < waveform.length; i++) {
                fltWaveform[i] = (float) waveform[i];
            }

            return fltWaveform;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

