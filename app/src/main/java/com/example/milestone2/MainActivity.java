package com.example.milestone2;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    int location = 0;
    boolean firstTry = true;

    private RealTimeGraphs realtime;
    private ModelHandler modelHandler;
    private Measurement[] measurements = new Measurement[24];
    private ColorMapper colorMapper;

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
        findViewById(R.id.image1button).setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        findViewById(R.id.image2button).setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        findViewById(R.id.image3button).setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        updateButtonColors(1);

        imageView = findViewById(R.id.imageView);


        colorMapper = null;
        try {
            colorMapper = new ColorMapper(getResources().openRawResource(R.raw.rgb_values));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        realtime = new RealTimeGraphs(colorMapper,findViewById(R.id.timechart),findViewById(R.id.scatterchart));

        modelHandler = new ModelHandler(findViewById(R.id.result), findViewById(R.id.confidence), imageView, colorMapper, getApplicationContext());

        setRadioButtons();
        setRadioButtonColor(location, Color.BLUE);

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

    public void updateButtonColors(int showImage) {
        Log.d("ButtonColors", "UpdateButtonColors called for image "+showImage);
        int grayColor = ContextCompat.getColor(this, R.color.gray_button_color);
        int lightGrayColor = ContextCompat.getColor(this, R.color.light_gray_button_color);

        switch (showImage) {
            case 1:
                findViewById(R.id.image1button).setBackgroundTintList(ColorStateList.valueOf(grayColor));
                findViewById(R.id.image2button).setBackgroundTintList(ColorStateList.valueOf(lightGrayColor));
                findViewById(R.id.image3button).setBackgroundTintList(ColorStateList.valueOf(lightGrayColor));
                break;
            case 2:
                findViewById(R.id.image1button).setBackgroundTintList(ColorStateList.valueOf(lightGrayColor));
                findViewById(R.id.image2button).setBackgroundTintList(ColorStateList.valueOf(grayColor));
                findViewById(R.id.image3button).setBackgroundTintList(ColorStateList.valueOf(lightGrayColor));
                break;
            case 3:
                findViewById(R.id.image1button).setBackgroundTintList(ColorStateList.valueOf(lightGrayColor));
                findViewById(R.id.image2button).setBackgroundTintList(ColorStateList.valueOf(lightGrayColor));
                findViewById(R.id.image3button).setBackgroundTintList(ColorStateList.valueOf(grayColor));
                break;
            default:
                break;
        }
    }

    public void onBtnIm1Clicked(View view) {
        updateButtonColors(1);
        if(validMeasurement(location)){
            imageView.setImageBitmap(measurements[location*3].image);
            imageView.invalidate();
        }
    }

    public void onBtnIm2Clicked(View view) {
        updateButtonColors(2);
        if(validMeasurement(location)){
            imageView.setImageBitmap(measurements[location*3+1].image);
            imageView.invalidate();
        }
    }

    public void onBtnIm3Clicked(View view) {
        updateButtonColors(3);
        if(validMeasurement(location)){
            imageView.setImageBitmap(measurements[location*3+2].image);
            imageView.invalidate();
        }
    }

    public void onBtnZoomInClicked(View view) {
        realtime.zoom(0);
    }

    public void onBtnZoomOutClicked(View view) {
        realtime.zoom(1);
    }

    public void onBtnStartClicked(View view){
        if(!listening){
            if(!firstTry){
                realtime.timeDomain.resetFrameIndex();
                realtime.resetRealTimeGraphs();
            }
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

    public void onBtnStopClicked(View view) throws IOException {
        if(listening==true){
            firstTry = false;
            listening = false;
            handler.removeCallbacksAndMessages(null);
            realtime.dataProvider.setPaused(true);
            realtime.dataProvider.tryStop();
            realtime.dataProvider.resetAudioRecord();
            realtime.resetIndices();

            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(new Date());

            ShortValues[] epochs = realtime.audioDSy.splitIntoThree();
            File[] files = new File[3];

            for(int i = 0; i<3; i++){
                files[i] = realtime.dataProvider.saveAudioToFile(timeStamp + "_" + i + ".wav", epochs[i]);
                InputStream inputStream = new FileInputStream(files[i]);
                Measurement measurement = modelHandler.preProcessImage(inputStream);
                measurement.setWavFile(files[i]);
                measurement.setLocation(location);
                measurement.setEpoch(i);
                measurements[i+location*3]=measurement;
                inputStream.close();
            }
            imageView.setImageBitmap(measurements[location*3].image);
            imageView.invalidate();
            showResults();

            realtime.audioDSy.clear();
        }
    }

    public void onBtnDLClicked(View view) throws IOException {
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

    public void onBtnNextLocClicked(View view) throws IOException {
        if(!listening) {
            RelativeLayout relativeLayout = findViewById(R.id.mlresults);
            relativeLayout.setVisibility(View.INVISIBLE);

            location += 1;
            realtime.timeDomain.resetFrameIndex();

            if (validMeasurement(location - 1)) {
                setRadioButtonColor(location - 1, Color.GREEN);
            } else {
                setRadioButtonColor(location - 1, Color.BLACK);
            }

            if (location == 7) {
                Button button = findViewById(R.id.btnClear);
                button.setText("Go To Report");
            } else if (location == 8) {
                Intent intent = new Intent(MainActivity.this, Results.class);
                intent.putExtra("measurements", measurements);
                startActivity(intent);
            }

            setRadioButtonColor(location, Color.BLUE);
            if (listening == false) {
                realtime.resetRealTimeGraphs();
            }
        }
    }

    private void showResults(){
        String[] classes = {"0", "1", "2", "3"};
        Measurement m1 = measurements[location*3];
        Measurement m2 = measurements[location*3+1];
        Measurement m3 = measurements[location*3+2];

        // Averaging the confidences
        float[] averageConfidences = new float[classes.length];
        for (int i = 0; i < classes.length; i++) {
            averageConfidences[i] = (m1.confidences[i] + m2.confidences[i] + m3.confidences[i]) / 3.0f;
        }

        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < averageConfidences.length; i++) {
            if (averageConfidences[i] > maxConfidence) {
                maxConfidence = averageConfidences[i];
                maxPos = i;
            }
        }

        TextView result = findViewById(R.id.result);
        TextView confidence = findViewById(R.id.confidence);
        result.setText(classes[maxPos]);

        // Create the confidence string for each class
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(String.format("%-10s %-10s %-10s %-10s\n", "Class", "Breath 1", "Breath 2", "Breath 3"));
        sb.append("--------------------------------------------------------------\n");
        for (int i = 0; i < classes.length; i++) {
            String row = String.format(Locale.US, "%-13s %-14.1f %-14.1f %-14.1f\n", classes[i],
                    m1.confidences[i] * 100, m2.confidences[i] * 100, m3.confidences[i] * 100);
            int start = sb.length();
            sb.append(row);
            int end = sb.length();
            if (i == maxPos) {
                sb.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        confidence.setText(sb);
        RelativeLayout relativeLayout = findViewById(R.id.mlresults);
        relativeLayout.setVisibility(View.VISIBLE);
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

    private void setRadioButtons(){
        ColorStateList colorStateList = ColorStateList.valueOf(Color.BLACK);

        RadioButton loc0 = findViewById(R.id.loc0);
        loc0.setButtonTintList(colorStateList);

        RadioButton loc1 = findViewById(R.id.loc1);
        loc1.setButtonTintList(colorStateList);

        RadioButton loc2 = findViewById(R.id.loc2);
        loc2.setButtonTintList(colorStateList);

        RadioButton loc3 = findViewById(R.id.loc3);
        loc3.setButtonTintList(colorStateList);

        RadioButton loc4 = findViewById(R.id.loc4);
        loc4.setButtonTintList(colorStateList);

        RadioButton loc5 = findViewById(R.id.loc5);
        loc5.setButtonTintList(colorStateList);

        RadioButton loc6 = findViewById(R.id.loc6);
        loc6.setButtonTintList(colorStateList);

        RadioButton loc7 = findViewById(R.id.loc7);
        loc7.setButtonTintList(colorStateList);
    }

    private void setRadioButtonColor(int location, int color) {
        RadioButton radioButton = null;
        switch (location) {
            case 0:
                radioButton = findViewById(R.id.loc0);
                break;
            case 1:
                radioButton = findViewById(R.id.loc1);
                break;
            case 2:
                radioButton = findViewById(R.id.loc2);
                break;
            case 3:
                radioButton = findViewById(R.id.loc3);
                break;
            case 4:
                radioButton = findViewById(R.id.loc4);
                break;
            case 5:
                radioButton = findViewById(R.id.loc5);
                break;
            case 6:
                radioButton = findViewById(R.id.loc6);
                break;
            case 7:
                radioButton = findViewById(R.id.loc7);
                break;
            default:
                // Handle if location is out of range
                return;
        }

        // Set the color for the RadioButton
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        radioButton.setButtonTintList(colorStateList);
    }

    private boolean validMeasurement(int location){
        int count = 0;
        for(Measurement measurement : measurements){
            try{
                if(measurement.location == location){
                    count+=1;
                }
            } catch (Exception e) {
            }
        }
        if(count==3){
            return true;
        }
        else{
            return false;
        }
    }
}