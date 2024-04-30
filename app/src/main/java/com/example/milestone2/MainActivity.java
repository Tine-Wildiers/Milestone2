package com.example.milestone2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.milestone2.ml.MobileModelV2;
import com.github.mikephil.charting.charts.ScatterChart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    int testClass = 3;
    int wavfile;
    int pngfile;

    private RealTimeGraphs realtime;
    private ModelHandler modelHandler;

    ImageView imageView;
    private boolean listening = false;
    Handler handler = new Handler();
    Runnable saveScatterChartRunnable = new Runnable() {
        @Override
        public void run() {
            saveScatterChart();
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
        handler.postDelayed(saveScatterChartRunnable, 5000); // Start the repeating task to save ScatterChart every 5 seconds
    }

    public void onBtnStopClicked(View view) throws FileNotFoundException {
        handler.removeCallbacksAndMessages(null);
        realtime.dataProvider.setPaused(true);
        realtime.dataProvider.tryStop();
        realtime.audioDSy.clear();
        realtime.dataProvider.resetAudioRecord();

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(new Date());
        String fileName = timeStamp + ".wav";
        File file = realtime.dataProvider.saveAudioToFile(fileName, realtime.audioDSy);

        InputStream inputStream = new FileInputStream(file);
        modelHandler.preProcessImage(inputStream);
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

    private void saveScatterChart() {
        ScatterChart chart = findViewById(R.id.scatterchart);
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        chart.saveToPath("Melspectrogram"+timeStamp, "/DCIM/Melspectrograms");
    }
}