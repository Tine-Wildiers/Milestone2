package com.example.milestone2;

import android.util.Log;

import com.example.milestone2.helpers.Radix2FFT;
import com.example.milestone2.helpers.Recorder;
import com.example.milestone2.types.DoubleValues;
import com.example.milestone2.types.ShortValues;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.trello.rxlifecycle3.android.FragmentEvent;
import com.trello.rxlifecycle3.android.RxLifecycleAndroid;

import io.reactivex.subjects.BehaviorSubject;

public class RealTimeGraphs {
    //de Recorder() roept al een failed to call close op maar dit gebeurt in een api functie
    Recorder dataProvider = new Recorder();
    private final BehaviorSubject<FragmentEvent> lifecycleSubject = BehaviorSubject.create();
    private final Radix2FFT fft = new Radix2FFT(dataProvider.getBufferSize());
    private final int sampleRate = dataProvider.getSampleRate();
    public TimeDomain timeDomain = new TimeDomain(sampleRate);
    public FrequencyDomain frequencyDomain = new FrequencyDomain();
    private int slowDown = 0;
    private final DoubleValues fftData = new DoubleValues();
    ShortValues audioDSy = new ShortValues(dataProvider.getBufferSize());
    private int timeindex = 0;
    private int frequencyindex = 0;

    public RealTimeGraphs(ColorMapper cM, LineChart timeChart, ScatterChart spectrogram) {
        timeDomain.setTimeChart(timeChart);
        frequencyDomain.setColorMapper(cM);
        frequencyDomain.setSpectrogramChart(spectrogram);
    }

    protected void startListening() {
        dataProvider.getData().doOnNext(audioData -> {
            try {
                audioDSy.add(audioData.yData.getItemsArray());

                // ----- Update Time Plot -----
                /*
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(0), (float) audioData.yData.getItemsArray()[0]));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(256), (float) audioData.yData.get(256)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(512), (float) audioData.yData.get(512)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(768), (float) audioData.yData.get(768)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1024), (float) audioData.yData.get(1024)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1280), (float) audioData.yData.get(1280)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1536), (float) audioData.yData.get(1536)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1792), (float) audioData.yData.get(1792)));
                */
                if(timeindex > timeDomain.getMaxSize()-8){
                    timeindex = 0;
                }
                timeDomain.timePlot.set(timeindex, new Entry(timeindex, (float) audioData.yData.getItemsArray()[0]));
                timeDomain.timePlot.set(timeindex+1, new Entry(timeindex +1, (float) audioData.yData.get(256)));
                timeDomain.timePlot.set(timeindex+2, new Entry(timeindex +2, (float) audioData.yData.get(512)));
                timeDomain.timePlot.set(timeindex+3, new Entry(timeindex +3, (float) audioData.yData.get(768)));
                timeDomain.timePlot.set(timeindex+4, new Entry(timeindex +4, (float) audioData.yData.get(1024)));
                timeDomain.timePlot.set(timeindex+5, new Entry(timeindex +5, (float) audioData.yData.get(1280)));
                timeDomain.timePlot.set(timeindex+6, new Entry(timeindex +6, (float) audioData.yData.get(1536)));
                timeDomain.timePlot.set(timeindex+7, new Entry(timeindex +7, (float) audioData.yData.get(1792)));
                timeindex += 8;

                timeDomain.updateTimeGraph(timeindex);

                // ----- Update Frequency Plot -----
                fft.run(audioData.yData, fftData);
                //updateFrequencyGraph();

                // ----- Update Spectrogram Plot -----\
                if(frequencyindex > frequencyDomain.getMaxSize()-1){
                    frequencyindex = 0;
                }

                double[] downScaledArray = downscaleArray(frequencyDomain.getSpectogramYRes());
                frequencyindex = frequencyDomain.updateSpectrogram(downScaledArray, frequencyindex);

                if(slowDown%3 == 0){
                    frequencyDomain.renderSpectrogram();
                }

                slowDown++;

            } catch (Exception e) {
                Log.e("Error", "An error occurred: " + e.getMessage());
            }
        }).compose(RxLifecycleAndroid.bindFragment(lifecycleSubject)).subscribe();
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

    void resetRealTimeGraphs(){
        //TODO: kijken welke parameters gereset moeten worden
        //TODO: spectrogram resetten


        //Gebeurt al in de button Stop methode
        //dataProvider.resetAudioRecord();
        //dataProvider.resetRecorder();
        dataProvider = new Recorder();
        timeDomain.resetTimePlot();
        timeDomain.updateTimeGraph(0);
        //frequencyDomain = new FrequencyDomain();
    }


}
