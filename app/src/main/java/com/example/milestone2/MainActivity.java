package com.example.milestone2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.milestone2.types.DoubleValues;
import com.example.milestone2.types.ShortValues;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.trello.rxlifecycle3.android.FragmentEvent;
import com.trello.rxlifecycle3.android.RxLifecycleAndroid;

import java.util.Arrays;

import io.reactivex.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {
    private final Recorder dataProvider = new Recorder();
    private final BehaviorSubject<FragmentEvent> lifecycleSubject = BehaviorSubject.create();
    private final Radix2FFT fft = new Radix2FFT(dataProvider.getBufferSize());
    private final int sampleRate = dataProvider.getSampleRate();
    private boolean listening = false;
    public TimeDomain timeDomain = new TimeDomain(sampleRate);
    public FrequencyDomain frequencyDomain = new FrequencyDomain();
    private int slowDown = 0;

    private final DoubleValues fftData = new DoubleValues();
    private final int fftSize = 1024;

    //private final LongValues audioDSx = new LongValues(dataProvider.getBufferSize());
    private final ShortValues audioDSy = new ShortValues(dataProvider.getBufferSize());
    private LineChart frequencyChart;
    Entry[] frequencyPlot = new Entry[fftSize];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeDomain.setTimeChart(findViewById(R.id.timechart));
        frequencyDomain.setSpectrogramChart(findViewById(R.id.scatterchart));

        timeDomain.setupTimeChart();
        frequencyDomain.setupSpectrogramGraph();



        //Python py = Python.getInstance();
        //PyObject module = py.getModule( "Mel_Spectrogram" );


        //frequencyChart = findViewById(R.id.frequencychart);
        //setupFrequencyGraph();
    }

    public void onBtnStartClicked(View view){
        if(!listening){
            listening = true;
            startListening();
        }
        else{
            if(dataProvider.getPaused()){
                dataProvider.setPaused(false);
                dataProvider.tryStart();
            }
        }
    }

    public void onBtnStopClicked(View view){
        dataProvider.setPaused(true);
        dataProvider.tryStop();
        dataProvider.saveAudioToFile(audioDSy);
        audioDSy.clear();
        dataProvider.resetAudioRecord();

        /*
        PySystemState pyState = new PySystemState() ;
        PythonInterpreter pythonInterpreter = new PythonInterpreter(null, pyState);
        pythonInterpreter.exec("import sys");
        pythonInterpreter.exec("sys.path.append('C:\\Users\\tinew\\Documents\\kul\\MaNaMa\\Melspectrogram)"); // Add path to your Python script
        pythonInterpreter.execfile("C:\\Users\\tinew\\Documents\\kul\\MaNaMa\\Melspectrogram\\melspectrogram.py"); // Execute your Python script
        pythonInterpreter.exec("generate_mel_spectrogram()"); // Call your Python function
         */
    }

    public void onBtnDLClicked(View view) {

        TextView text = (TextView) findViewById(R.id.dlOutput);
        text.setText("Something Else");
    }

    protected void startListening() {
        dataProvider.getData().doOnNext(audioData -> {
            try {
                //audioDSx.add(audioData.xData.getItemsArray());
                audioDSy.add(audioData.yData.getItemsArray());

                // ----- Update Time Plot -----
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(0), (float) audioData.yData.getItemsArray()[0]));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(256), (float) audioData.yData.get(256)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(512), (float) audioData.yData.get(512)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(768), (float) audioData.yData.get(768)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1024), (float) audioData.yData.get(1024)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1280), (float) audioData.yData.get(1280)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1536), (float) audioData.yData.get(1536)));
                timeDomain.timePlot.add(new Entry((int) audioData.xData.get(1792), (float) audioData.yData.get(1792)));
                timeDomain.updateTimeGraph();

                // ----- Update Frequency Plot -----
                fft.run(audioData.yData, fftData);
                //updateFrequencyGraph();

                // ----- Update Spectrogram Plot -----
                double[] downScaledArray = downscaleArray(frequencyDomain.getSpectogramYRes());
                frequencyDomain.updateSpectrogram(downScaledArray);

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

    private void setupFrequencyGraph(){
        /*

        Deze lines terug toevoegen aan activity_main.xml
        <com.github.mikephil.charting.charts.LineChart
        android:id="@+id/frequencychart"
        android:layout_width="968dp"
        android:layout_height="79dp" />

         */

        for (int i = 0; i < fftSize; i++) {
            frequencyPlot[i] = new Entry(i, 0);
        }

        Description description = new Description();
        description.setEnabled(false);
        frequencyChart.setDescription(description);
        frequencyChart.getLegend().setEnabled(false);
        frequencyChart.getAxisRight().setDrawLabels(false);

        XAxis xAxisf = frequencyChart.getXAxis();
        xAxisf.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisf.setDrawGridLines(false);
        xAxisf.setDrawGridLines(true);
        xAxisf.setGridLineWidth(1f);
        xAxisf.setGridColor(Color.GRAY);
        xAxisf.setGranularity(1f);
        xAxisf.setGranularityEnabled(true);

        YAxis yAxisf = frequencyChart.getAxisLeft();
        yAxisf.setAxisMinimum(-30);
        yAxisf.setAxisMaximum(+100);
        yAxisf.setAxisLineWidth(2f);
        yAxisf.setAxisLineColor(Color.BLACK);
        yAxisf.setDrawGridLines(false);

        YAxis rightYAxisf = frequencyChart.getAxisRight();
        rightYAxisf.setDrawGridLines(false);
    }

    protected void updateFrequencyGraph(){
        for (int i = 0; i < fftSize; i++) {
            frequencyPlot[i].setY((float) fftData.get(i));
        }

        LineDataSet dataSet1 = new LineDataSet(Arrays.asList(frequencyPlot), "FrequencyWave");
        dataSet1.setColor(Color.TRANSPARENT);
        dataSet1.setLineWidth(0f);
        dataSet1.setDrawCircles(true);
        dataSet1.setDrawCircleHole(false);
        dataSet1.setCircleColor(Color.BLUE);
        dataSet1.setCircleRadius(1f);
        LineData lineData = new LineData(dataSet1);

        frequencyChart.setData(lineData);
        frequencyChart.invalidate();
    }
}