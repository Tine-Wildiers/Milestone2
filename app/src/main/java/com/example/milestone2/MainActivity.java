package com.example.milestone2;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Main class that implements the app user interface and basic logic.
 */
public class MainActivity extends AppCompatActivity {
    int wavfile = R.raw.w1403_lr_25;
    int pngfile = R.raw.p1403_lr_25;

    int location = 0;
    boolean firstTry = true;

    //State variables
    private boolean listening = false; //true when real time recording is ongoing
    private boolean calculating = false; //true when deep learning model is processing
    boolean live = true; //false when the realtime button is pressed off

    private RealTimeGraphs realtime; //Class that handles all the real time events
    private ModelHandler modelHandler; //Class that handles the model input and output
    private final Measurement[] measurements = new Measurement[24]; //Storage for finished measurements

    ImageView imageView; //Reference to the input image of the model


    /**
     * Sets up the application and starts audio playback.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Implement button logic
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.image1button).setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        findViewById(R.id.image2button).setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        findViewById(R.id.image3button).setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        updateButtonColors(1);
        imageView = findViewById(R.id.imageView);
        Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                live = !isChecked;
                switch1.setEnabled(false);
            }
        });
        setRadioButtons();
        setRadioButtonColor(location, Color.BLUE);

        //Setup main classes - RealTimeGraph and ModelHandler
        ColorMapper colorMapper = null;
        try {
            colorMapper = new ColorMapper(getResources().openRawResource(R.raw.rgb_values));
            realtime = new RealTimeGraphs(colorMapper,findViewById(R.id.timechart),findViewById(R.id.scatterchart));
            modelHandler = new ModelHandler(findViewById(R.id.result), findViewById(R.id.confidence), imageView, colorMapper, getApplicationContext());
        } catch (IOException e) {
            throw new RuntimeException(e); //Exception for when there is an error with the file
        }

        //Start audio playback
        realtime.pause();
        realtime.startListening();
    }

    /**
     * Checks if recording is already ongoing, if not, starts recording and real time graphs.
     */
    public void onBtnStartClicked(View view){
        if(!listening){
            if(!firstTry){
                realtime.resetRealTimeGraphs();
            }
            listening = true;
            realtime.resume();
        }
        else{
            if(realtime.recorder.getPaused()){
                realtime.recorder.setPaused(false);
                realtime.recorder.tryStart();
            }
        }
    }

    /**
     * Stops real time graphs and saves recent recordings as wav files.
     * If real time graphs are turned on, recordings are processed by the model and results are shown.
     */
    public void onBtnStopClicked(View view) throws IOException {
        if(listening){
            realtime.pause();
            calculating = true;
            firstTry = false;
            listening = false;
            File[] files = realtime.finishMeasurement(location);

            for(int i = 0; i<3; i++){
                Measurement measurement = new Measurement(files[i], location, i);
                if(live){
                    InputStream inputStream = Files.newInputStream(files[i].toPath());
                    Pair<float[], Bitmap> result = modelHandler.processImage(inputStream);
                    float[] confidences = result.first;
                    Bitmap image = result.second;
                    inputStream.close();
                    measurement.setResult(image, confidences);
                }
                measurements[i+location*3]= measurement;
            }

            if(live){
                imageView.setImageBitmap(measurements[location*3].image);
                imageView.invalidate();
                showResults();
            }

            calculating = false;
        }
    }

    /**
     * Implements the logic behind the dots that represent auscultation location.
     * When all locations have been measured, this method starts up the Results page.
     */
    public void onBtnNextLocClicked(View view) throws IOException {
        if(!listening && !calculating) {
            RelativeLayout relativeLayout = findViewById(R.id.mlresults);
            relativeLayout.setVisibility(View.INVISIBLE);

            location += 1;

            if (validMeasurement(location - 1)) {
                setRadioButtonColor(location - 1, Color.GREEN);
            } else {
                setRadioButtonColor(location - 1, Color.BLACK);
            }

            if (location == 7) {
                Button button = findViewById(R.id.btnClear);
                button.setText("Go To Report");
            } else if (location == 8) {
                if(!live){
                    for(Measurement m : measurements){
                        if(m!= null && m.confidences == null){
                            InputStream inputStream = Files.newInputStream(m.wavFile.toPath());
                            Pair<float[], Bitmap> result = modelHandler.processImage(inputStream);
                            float[] confidences = result.first;
                            Bitmap image = result.second;
                            inputStream.close();
                            m.setResult(image, confidences);
                        }
                    }
                }

                Intent intent = new Intent(MainActivity.this, Results.class);
                intent.putExtra("measurements", measurements);
                startActivity(intent);
            }

            setRadioButtonColor(location, Color.BLUE);
            realtime.resetRealTimeGraphs();
        }
    }

    /**
     * Shows the input, final classification and confidences of the deep learning model prediction.
     */
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

    /**
     * Helper Methods
     */

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
                return;
        }
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