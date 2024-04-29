package com.example.milestone2;

import static android.content.ContentValues.TAG;

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
    private final ShortValues audioDSy = new ShortValues(dataProvider.getBufferSize());
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

        CSVFile csvFile2 = new CSVFile(getResources().openRawResource(R.raw.rgb_values));
        frequencyDomain.setRGBPalette(csvFile2.readRGB());

        timeDomain.setTimeChart(findViewById(R.id.timechart));
        frequencyDomain.setSpectrogramChart(findViewById(R.id.scatterchart));

        timeDomain.setupTimeChart();
        frequencyDomain.setupSpectrogramGraph();

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);
        modelHandler = new ModelHandler(result, confidence, imageView, picture);
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
        audioDSy.clear();
        dataProvider.resetAudioRecord();

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(new Date());
        String fileName = timeStamp + ".wav";
        File file = dataProvider.saveAudioToFile(fileName, audioDSy);

        InputStream inputStream = new FileInputStream(file);

        preProcessImage(inputStream);
    }

    public void onBtnDLClicked(View view) throws FileNotFoundException {
        InputStream inputStream = getResources().openRawResource(R.raw.w1400bl10);
        preProcessImage(inputStream);
    }

    public void preProcessImage(InputStream inputStream){
        float[] audioData = WavFileReader.readWavFile(inputStream);

        MelSpectrogram melSpectrogram = new MelSpectrogram();
        float[][] melspec = melSpectrogram.process(audioData);

        int height = melspec.length; // Width of the image
        int width = melspec[0].length; // Height of the image

        int[] values = {195, 193, 109, 71, 53, 43, 35, 30, 27, 24, 21, 19, 18, 16, 15, 14, 14, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

        int sum = 0;
        for (int num : values) {
            sum += num;
        }

        // Create a blank bitmap with the specified width and height
        Bitmap image = Bitmap.createBitmap(width, sum, Bitmap.Config.ARGB_8888);

        int bigindex;

        // Loop through each pixel in the float array and set the corresponding pixel in the bitmap
        for (int y = 0; y < width; y++) {
            bigindex = sum-1;
            for (int x = 0; x < height; x++) {
                //TODO: deze berekening correcter maken
                int index = (int) (melspec[x][y]+100)* frequencyDomain.getColorMapperSize()/100;

                int color = frequencyDomain.getRGBColor(index);

                for(int z = 0; z < values[x]; z++){
                    image.setPixel(y, bigindex, color);// Set pixel color in the bitmap
                    if (bigindex == 0) {
                        // Log the current value of bigIndex
                        Log.d(TAG, "bigIndex is low. Value: " + bigindex);
                    }
                    bigindex -= 1;
                }
            }
        }

        image = scaleBitmap(image);

        // Now, you can use the bitmap in your ImageView
        imageView.setImageBitmap(image);

        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        try {
            modelHandler.classifySound(image, MobileModelV2.newInstance(getApplicationContext()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Bitmap scaleBitmap(Bitmap image){
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // Create a new scaled bitmap
        Bitmap stretchedBitmap = Bitmap.createScaledBitmap(image, 1980, originalHeight, true);

        return stretchedBitmap;
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

    private void saveScatterChart() {
        ScatterChart chart = findViewById(R.id.scatterchart);
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        chart.saveToPath("Melspectrogram"+timeStamp, "/DCIM/Melspectrograms");
    }
}