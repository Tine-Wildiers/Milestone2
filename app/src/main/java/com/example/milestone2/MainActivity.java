package com.example.milestone2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.milestone2.ml.MobileModelV2;
import com.example.milestone2.types.ShortValues;
import com.github.mikephil.charting.charts.ScatterChart;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.stream.IntStream;

//TODO: make sure that app works on new device
//TODO: scaling should take device into account
public class MainActivity extends AppCompatActivity {
    int testClass = 3;
    int interval = 5;
    int wavfile;
    int pngfile;

    private RealTimeGraphs realtime;
    private ModelHandler modelHandler;
    private Measurement[] measurements = new Measurement[24];

    ImageView imageView;
    private boolean listening = false;
    Handler handler = new Handler();
    Runnable saveScatterChartRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                saveScatterChart();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            handler.postDelayed(this, 5000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        ColorMapper colorMapper = new ColorMapper(getResources().openRawResource(R.raw.rgb_values));

        realtime = new RealTimeGraphs(colorMapper,findViewById(R.id.timechart),findViewById(R.id.scatterchart));

        modelHandler = new ModelHandler(findViewById(R.id.result), findViewById(R.id.confidence), imageView, findViewById(R.id.button), colorMapper, getApplicationContext());
        
        if(testClass == 0){
            wavfile = R.raw.wsl_1_1400_0;
            pngfile = R.raw.sl_1_1400_0;
        }
        else if(testClass==1){
            wavfile = R.raw.wpl_7_1400_1;
            pngfile = R.raw.pl_7_1400_1;
        }
        else if(testClass==2){
            wavfile = R.raw.wll_15_1403_2;
            pngfile = R.raw.ll_15_1403_2;
        }
        else{
            //wavfile = R.raw.wbr_19_1402_3;
            //pngfile = R.raw.br_19_1402_3;
            wavfile = R.raw.wlr_25_1403_3;
            pngfile = R.raw.lr_25_1403_3;
        }
    }

    public void onBtnStartClicked(View view){
        if(!listening){
            listening = true;
            realtime.startListening();
        }
        else{
            if(realtime.dataProvider.getPaused()){
                realtime.dataProvider.setPaused(false);
                realtime.dataProvider.tryStart();
            }
        }
        handler.postDelayed(saveScatterChartRunnable, interval*1000);
    }

    public void onBtnStopClicked(View view) throws FileNotFoundException {
        listening = false;
        handler.removeCallbacksAndMessages(null);
        realtime.dataProvider.setPaused(true);
        realtime.dataProvider.tryStop();

        realtime.dataProvider.resetAudioRecord();

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(new Date());

        ShortValues[] epochs = realtime.audioDSy.splitIntoThree();
        File[] files = new File[3];

        for(int i = 0; i<3; i++){
            files[i] = realtime.dataProvider.saveAudioToFile(timeStamp + "_" + i + ".wav", epochs[i]);
            InputStream inputStream = new FileInputStream(files[i]);
            Measurement measurement = modelHandler.preProcessImage(inputStream);
            measurement.setWavFile(files[i]);
            measurement.setLocation(0);
            measurement.setEpoch(i);
            measurements[i]=measurement;
            imageView.setImageBitmap(measurements[i].image);
            imageView.invalidate();
        }
        showResults();

        realtime.audioDSy.clear();
    }

    public void onBtnDLClicked(View view) {
        InputStream inputStream = getResources().openRawResource(wavfile);
        modelHandler.preProcessImage(inputStream);
    }

    public void onBtnTestModelClicked(View view){
        int imageSize = 224;
        InputStream inputStream = getResources().openRawResource(pngfile);
        Bitmap image = BitmapFactory.decodeStream(inputStream);
        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        imageView.setImageBitmap(image);

        try {
            modelHandler.classifySound(image, MobileModelV2.newInstance(getApplicationContext()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onBtnClearClicked(View view){
        if(listening==false){
            ColorMapper colorMapper = new ColorMapper(getResources().openRawResource(R.raw.rgb_values));

            realtime = new RealTimeGraphs(colorMapper,findViewById(R.id.timechart),findViewById(R.id.scatterchart));

            modelHandler = new ModelHandler(findViewById(R.id.result), findViewById(R.id.confidence), imageView, findViewById(R.id.button), colorMapper, getApplicationContext());

        }
    }

    private void showResults(){
        String[] classes = {"0", "1", "2", "3"};

        float[] confidences = measurements[0].confidences;
        int maxPos = 0;
        float maxConfidence = 0;
        for(int i = 0; i < confidences.length; i++){
            if(confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }

        TextView result = findViewById(R.id.result);
        TextView confidence = findViewById(R.id.confidence);
        result.setText(classes[maxPos]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; i++) {
            sb.append(String.format(Locale.US, "%s: %.1f%%, %.1f%%, %.1f%%\n", classes[i], measurements[0].confidences[i] * 100,measurements[1].confidences[i] * 100, measurements[2].confidences[i] * 100));

        }
        confidence.setText(sb.toString());
    }

    private void saveScatterChart() throws FileNotFoundException {
        ScatterChart chart = findViewById(R.id.scatterchart);
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        chart.saveToPath("Melspectrogram"+timeStamp, "/DCIM/Melspectrograms");

        /*
        //TODO: stap overslagen van file aan te maken, de audioDSy array direct gebruiken voor voorspelling.
        //String fileName = timeStamp + ".wav";
        //File file = realtime.dataProvider.saveAudioToFile(fileName, realtime.audioDSy);

        short[] shortArray = realtime.audioDSy.getItemsArray();
        double[] doubleArray = IntStream.range(0, shortArray.length)
                .mapToDouble(i -> shortArray[i])
                .toArray();

        float[] floatArray = new float[shortArray.length];
        for (int i = 0; i < shortArray.length; i++) {
            floatArray[i] = (float) doubleArray[i];
        }

        //InputStream inputStream = new FileInputStream(file);
        modelHandler.preProcessImage(floatArray);
        realtime.audioDSy.clear();

         */
    }
}