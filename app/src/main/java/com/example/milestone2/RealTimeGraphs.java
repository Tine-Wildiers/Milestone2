package com.example.milestone2;

import android.util.Log;
import com.example.milestone2.helpers.Radix2FFT;
import com.example.milestone2.helpers.Recorder;
import com.example.milestone2.types.DoubleValues;
import com.example.milestone2.types.ShortValues;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.trello.rxlifecycle3.android.FragmentEvent;
import com.trello.rxlifecycle3.android.RxLifecycleAndroid;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Class that implements the two real time graphs.
 */
public class RealTimeGraphs {
    Recorder recorder = new Recorder(); //Object that will provide the real time data

    private final BehaviorSubject<Boolean> isPaused = BehaviorSubject.createDefault(false); //Decides if real time graphs are producing an output
    private final BehaviorSubject<FragmentEvent> lifecycleSubject = BehaviorSubject.create(); //Allows for continuous monitoring of the Recorder object
    private final Radix2FFT fft = new Radix2FFT(recorder.getBufferSize()); //Class that computes the real time spectrogram

    private int timeindex = 0;
    private int frequencyindex = 0;
    ShortValues rawData = new ShortValues(recorder.getBufferSize());
    private final DoubleValues fftData = new DoubleValues();

    public TimeDomain timeDomain = new TimeDomain(); //Object that handles the time graph
    public FrequencyDomain frequencyDomain = new FrequencyDomain(); //Object that handles the spectrogram graph


    public RealTimeGraphs(ColorMapper cM, LineChart timeChart, ScatterChart spectrogram) {
        timeDomain.setTimeChart(timeChart);
        frequencyDomain.setSpectrogramChart(spectrogram, cM);
    }

    protected void startListening() {
        Disposable audioSubscription = recorder.getData()
                .filter(audioData -> !isPaused.getValue())
                .doOnNext(audioData -> {
                    try {
                        //update the raw data array
                        rawData.add(audioData.yData.getItemsArray());

                        //update the time plot
                        if (timeindex > timeDomain.getMaxSize() - 8) {
                            timeindex = 0;
                            timeDomain.updateFrameIndex();
                        }
                        timeDomain.timePlot.set(timeindex, new Entry(timeindex, (float) audioData.yData.getItemsArray()[0]));
                        timeDomain.timePlot.set(timeindex + 1, new Entry(timeindex + 1, (float) audioData.yData.get(256)));
                        timeDomain.timePlot.set(timeindex + 2, new Entry(timeindex + 2, (float) audioData.yData.get(512)));
                        timeDomain.timePlot.set(timeindex + 3, new Entry(timeindex + 3, (float) audioData.yData.get(768)));
                        timeDomain.timePlot.set(timeindex + 4, new Entry(timeindex + 4, (float) audioData.yData.get(1024)));
                        timeDomain.timePlot.set(timeindex + 5, new Entry(timeindex + 5, (float) audioData.yData.get(1280)));
                        timeDomain.timePlot.set(timeindex + 6, new Entry(timeindex + 6, (float) audioData.yData.get(1536)));
                        timeDomain.timePlot.set(timeindex + 7, new Entry(timeindex + 7, (float) audioData.yData.get(1792)));
                        timeindex += 8;

                        timeDomain.updateTimeGraph(timeindex);

                        //calculate spectrogram
                        fft.run(audioData.yData, fftData);

                        //update spectrogram
                        if (frequencyindex > frequencyDomain.getMaxSize() - 1) {
                            frequencyindex = 0;
                        }
                        double[] downScaledArray = downscaleArray(frequencyDomain.getSpectogramYRes());
                        frequencyindex = frequencyDomain.updateSpectrogram(downScaledArray, frequencyindex);
                        frequencyDomain.renderSpectrogram();

                    } catch (Exception e) {
                        Log.e("Error", "An error occurred: " + e.getMessage());
                    }
                })
                .compose(RxLifecycleAndroid.bindFragment(lifecycleSubject))
                .subscribe();
    }

    void resetRealTimeGraphs(){
        //Reset recorder
        resetRecorder();

        //Reset graphs
        timeDomain.resetTimePlot();
        timeDomain.updateTimeGraph(0);
        frequencyDomain.resetFrequencyPlot();
    }

    public void pause() {
        isPaused.onNext(true);
    }

    public void resume() {
        isPaused.onNext(false);
    }

    public File[] finishMeasurement(int location){

        //Store audio files per location
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        recorder.saveAudioToFile(timeStamp + "_loc_" + location + ".wav", rawData);

        //Store audio files per epoch
        ShortValues[] epochs = rawData.splitIntoThree();
        File[] files = new File[3];
        for(int i = 0; i<3; i++) {
            files[i] = recorder.saveAudioToFile(timeStamp + "_" + i + ".wav", epochs[i]);
        }
        
        //Reset visualisations and dataset
        resetIndices();
        rawData.clear();

        return files;
    }

    public double[] downscaleArray(int newSize) {
        double[] downScaledArray = new double[newSize];
        double[] originalArray = fftData.getItemsArray();
        double scaleFactor = (double) (originalArray.length - 1) / (newSize - 1);

        for (int i = 0; i < newSize; i++) {
            double index = i * scaleFactor;
            int lowIndex = (int) Math.floor(index);
            int highIndex = (int) Math.ceil(index);

            if (lowIndex == highIndex) {
                downScaledArray[i] = originalArray[lowIndex];
            } else {
                double lowWeight = highIndex - index;
                double highWeight = index - lowIndex;
                downScaledArray[i] = lowWeight * originalArray[lowIndex] + highWeight * originalArray[highIndex];
            }
        }
        return downScaledArray;
    }

    public void zoom(int mode){
        if(mode==0){
            timeDomain.zoomIn();
            timeDomain.updateTimeGraph(timeindex);
        }
        else{
            timeDomain.zoomOut();
            timeDomain.updateTimeGraph(timeindex);
        }
    }

    public void resetIndices(){
        timeindex = 0;
        frequencyindex = 0;
    }

    public void resetRecorder(){
        recorder = new Recorder();
    }
}
