package com.example.milestone2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.milestone2.ml.MobileModel;
import com.example.milestone2.ml.MobileModelV2;
import com.example.milestone2.ml.Model;
import com.example.milestone2.types.CSVFile;
import com.example.milestone2.types.DoubleValues;
import com.example.milestone2.types.ShortValues;
import com.example.milestone2.types.WavFileReader;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.trello.rxlifecycle3.android.FragmentEvent;
import com.trello.rxlifecycle3.android.RxLifecycleAndroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private ModelHandler modelHandler;

    TextView result, confidence;
    ImageView imageView;
    Button picture;
    int imageSize = 224;
    Handler handler = new Handler();
    Runnable saveScatterChartRunnable = new Runnable() {
        @Override
        public void run() {
            saveScatterChart();
            handler.postDelayed(this, 5000); // Execute this Runnable again after 5 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream inputStream = getResources().openRawResource(R.raw.magma_distinct);
        CSVFile csvFile = new CSVFile(inputStream);
        List palette = csvFile.read();
        frequencyDomain.setColorPalette(palette);

        timeDomain.setTimeChart(findViewById(R.id.timechart));
        frequencyDomain.setSpectrogramChart(findViewById(R.id.scatterchart));

        timeDomain.setupTimeChart();
        frequencyDomain.setupSpectrogramGraph();

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);
        modelHandler = new ModelHandler(result, confidence, imageView, picture);

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
        handler.postDelayed(saveScatterChartRunnable, 5000); // Start the repeating task to save ScatterChart every 5 seconds
    }

    public void onBtnStopClicked(View view) throws FileNotFoundException {
        handler.removeCallbacksAndMessages(null);
        dataProvider.setPaused(true);
        dataProvider.tryStop();
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(new Date());
        String fileName = timeStamp + ".wav";
        File file = dataProvider.saveAudioToFile(fileName, audioDSy);
        audioDSy.clear();
        dataProvider.resetAudioRecord();

        //InputStream inputStream = getResources().openRawResource(R.raw.w1400bl10);
        InputStream inputStream = new FileInputStream(file);


        float[] audioData = WavFileReader.readWavFile(inputStream);

        MelSpectrogram melSpectrogram = new MelSpectrogram();
        float[][] melspec = melSpectrogram.process(audioData);

        int width = melspec.length; // Width of the image
        int height = melspec[0].length; // Height of the image

        // Create a blank bitmap with the specified width and height
        Bitmap image = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

        // Loop through each pixel in the float array and set the corresponding pixel in the bitmap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Assuming the values in floatArray are in the range [0, 1]
                //int pixelValue = (int) ((melspec[x][y]+100)/100 * 255); // Scale float value to [0, 255]
                //int color = Color.rgb(pixelValue, pixelValue, pixelValue); // Create grayscale color
                int index = (int) (melspec[x][y]+100)* frequencyDomain.getColorMapperSize()/100;
                //TODO: deze capping beter instellen
                if(index<0){
                    index = 0;
                } else if (index>426) {
                    index = 426;
                }
                int color = frequencyDomain.getColor(index);
                image.setPixel(y, width - x - 1, color); // Set pixel color in the bitmap
            }
        }

        // Now, you can use the bitmap in your ImageView
        imageView.setImageBitmap(image);

    }

    public void onBtnDLClicked(View view) throws FileNotFoundException {

        InputStream inputStream = getResources().openRawResource(R.raw.w1400bl10);

        float[] audioData = WavFileReader.readWavFile(inputStream);

        MelSpectrogram melSpectrogram = new MelSpectrogram();
        float[][] melspec = melSpectrogram.process(audioData);

        int width = melspec.length; // Width of the image
        int height = melspec[0].length; // Height of the image

        // Create a blank bitmap with the specified width and height
        Bitmap image = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

        // Loop through each pixel in the float array and set the corresponding pixel in the bitmap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Assuming the values in floatArray are in the range [0, 1]
                //int pixelValue = (int) ((melspec[x][y]+100)/100 * 255); // Scale float value to [0, 255]
                //int color = Color.rgb(pixelValue, pixelValue, pixelValue); // Create grayscale color
                int index = (int) (melspec[x][y]+100)* frequencyDomain.getColorMapperSize()/100;
                //TODO: deze capping beter instellen
                if(index<0){
                    index = 0;
                } else if (index>426) {
                    index = 426;
                }
                int color = frequencyDomain.getColor(index);
                image.setPixel(y, width - x - 1, color); // Set pixel color in the bitmap
            }
        }

// Now, you can use the bitmap in your ImageView
        imageView.setImageBitmap(image);


        TextView text = findViewById(R.id.dlOutput);
        text.setText("Something Else");
    }

    public void onBtnTestModelClicked(View view){
        InputStream inputStream = getResources().openRawResource(R.raw.bl10);

        Bitmap image = BitmapFactory.decodeStream(inputStream);
        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
        imageView.setImageBitmap(image);

        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        try {
            modelHandler.classifySound(image, MobileModelV2.newInstance(getApplicationContext()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void saveScatterChart() {
        ScatterChart chart = findViewById(R.id.scatterchart);
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        chart.saveToPath("Melspectrogram"+timeStamp, "/DCIM/Melspectrograms");
    }
}