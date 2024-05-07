package com.example.milestone2.helpers;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

import com.example.milestone2.types.AudioData;
import com.example.milestone2.types.ShortValues;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Recorder {

    protected Observable<AudioData> dataObservable;
    private volatile boolean isStarted = false;
    public volatile boolean isPaused = false;

    private final int sampleRate;
    private final int minBufferSize;

    private AudioRecord audioRecord;
    private AudioData audioData;

    private long time = 0L;

    public void tryStop() {
        if(!isStarted)return;

        try {
            onStop();
        } finally {
            isStarted = false;
        }
    }

    public void tryStart() {
        if(isStarted) return;

        try {
            onStart();
        } finally {
            isStarted = true;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public Recorder(int sampleRate, int minBufferSize) {
        dataObservable = Observable.interval(sampleRate / minBufferSize, TimeUnit.MILLISECONDS)
                .map( l -> onNext())
                .doOnError(throwable -> Log.e("DataProvider", "onError", throwable))
                .doOnSubscribe(disposable -> tryStart())
                .doOnTerminate(this::tryStop)
                .doOnDispose(this::tryStop)
                .subscribeOn(Schedulers.single())
                .filter(__ -> !isPaused);

        this.sampleRate = sampleRate;
        this.minBufferSize = minBufferSize;
        this.audioRecord = new AudioRecord(1, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        this.audioData = new AudioData(minBufferSize);

        if(this.audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new UnsupportedOperationException("This devices doesn't support AudioRecord");
        }
    }

    public Recorder() {
        this(44100, 2048);
    }

    protected void onStart() {
        audioRecord.startRecording();
    }

    protected void onStop() {
        audioRecord.stop();
    }

    protected AudioData onNext() {
        audioRecord.read(audioData.yData.getItemsArray(), 0, minBufferSize);

        final long[] itemsArray = audioData.xData.getItemsArray();
        for (int i = 0; i < minBufferSize; i++) {
            itemsArray[i] = time++;
        }
        return audioData;
    }

    public void resetRecorder(){
        audioRecord.release();
        audioRecord = new AudioRecord(1, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        audioData = new AudioData(minBufferSize);
    }

    public void resetAudioRecord() {
        if (audioRecord != null) {
            audioRecord.release(); // Release the existing AudioRecord instance
        }

        // Create a new AudioRecord instance
        audioRecord = new AudioRecord(1, sampleRate,  AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        //audioData = new AudioData(minBufferSize);

        //if(this.audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            //throw new UnsupportedOperationException("This devices doesn't support AudioRecord");
        //}
    }


    public int getBufferSize() {
        return minBufferSize;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public Observable<AudioData> getData() {
        return dataObservable;
    }

    public void setPaused(boolean state){
        isPaused = state;
    }

    public boolean getPaused(){
        return isPaused;
    }

    public File saveAudioToFile(String fileName, ShortValues yData) {
        // Directory for StethoscopeData folder
        File documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File stethoscopeDataDirectory = new File(documentsDirectory, "StethoscopeData");

        // Check if the directory exists, if not, create it
        if (!stethoscopeDataDirectory.exists()) {
            if (!stethoscopeDataDirectory.mkdirs()) {
                Log.e("Recorder", "Failed to create directory: " + stethoscopeDataDirectory.getAbsolutePath());
            }
        }

        File file = new File(stethoscopeDataDirectory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            // Write WAV file header
            writeWavHeader(fos, yData.size());

            // Write audio data
            for (short value : yData.getItemsArray()) {
                fos.write(value & 0xff);
                fos.write((value >> 8) & 0xff);
            }

            Log.d("Recorder", "Audio data saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("Recorder", "Error saving audio data to file: " + file.getAbsolutePath(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    private void writeWavHeader(FileOutputStream fos, int audioDataLength) throws IOException {
        // WAV file format header
        long totalDataLen = audioDataLength * 2; // 2 bytes per sample
        long totalAudioLen = totalDataLen + 36; // 36 bytes for header
        long longSampleRate = sampleRate;
        int channels = 1; // Mono
        long byteRate = 16 * sampleRate * channels / 8;

        // Write WAV header
        byte[] header = new byte[44];
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalAudioLen & 0xff);
        header[5] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[6] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[7] = (byte) ((totalAudioLen >> 24) & 0xff);
        header[8] = 'W'; // 'WAVE' chunk
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' sub-chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 16 for PCM
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // AudioFormat, 1 for PCM
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 8 / 8); // Block align
        header[33] = 0;
        header[34] = 16; // Bits per sample
        header[35] = 0;
        header[36] = 'd'; // 'data' chunk
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalDataLen & 0xff);
        header[41] = (byte) ((totalDataLen >> 8) & 0xff);
        header[42] = (byte) ((totalDataLen >> 16) & 0xff);
        header[43] = (byte) ((totalDataLen >> 24) & 0xff);
        fos.write(header, 0, 44);
    }

}
