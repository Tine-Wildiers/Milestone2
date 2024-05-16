package com.example.milestone2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Results extends AppCompatActivity {
    Measurement[] measurements;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra("measurements");

        if (parcelables != null && parcelables.length > 0) {
            measurements = new Measurement[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                measurements[i] = (Measurement) parcelables[i];
            }
        }
        
        for(int i=0; i<8; i++){
            if(validMeasurement(i)){
                Measurement[] ms = new Measurement[3];

                int count = 0;
                for(Measurement measurement : measurements){
                    try{
                        if(measurement.location == i){
                            ms[count] = measurement;
                            count+=1;
                        }
                    } catch (Exception e) {
                    }
                }
                
                showResults(getTV(i), getConf(i), ms[0], ms[1], ms[2]);
            }
        }
    }

    public void onBtnSaveClicked(View view){
        takeScreenshot();
    }
    
    private TextView getTV(int location){
        if(location==0){
            return findViewById(R.id.result0);
        }
        else if(location==1){
            return findViewById(R.id.result1);
        }
        else if(location==2){
            return findViewById(R.id.result2);
        }
        else if(location==3){
            return findViewById(R.id.result3);
        }
        else if(location==4){
            return findViewById(R.id.result4);
        }
        else if(location==5){
            return findViewById(R.id.result5);
        }
        else if(location==6){
            return findViewById(R.id.result6);
        }
        else{
            return findViewById(R.id.result7);
        }
    }

    private TextView getConf(int location){
        if(location==0){
            return findViewById(R.id.confidence0);
        }
        else if(location==1){
            return findViewById(R.id.confidence1);
        }
        else if(location==2){
            return findViewById(R.id.confidence2);
        }
        else if(location==3){
            return findViewById(R.id.confidence3);
        }
        else if(location==4){
            return findViewById(R.id.confidence4);
        }
        else if(location==5){
            return findViewById(R.id.confidence5);
        }
        else if(location==6){
            return findViewById(R.id.confidence6);
        }
        else{
            return findViewById(R.id.confidence7);
        }
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

    private void showResults(TextView result, TextView confidence, Measurement m1, Measurement m2, Measurement m3){
        String[] classes = {"0", "1", "2", "3"};
        float[] totalConfidences = new float[classes.length];

        // Calculate total confidences for each class across all measurements
        for (int i = 0; i < classes.length; i++) {
            totalConfidences[i] = (m1.confidences[i] + m2.confidences[i] + m3.confidences[i]) / 3;
        }

        // Find the index of the class with the maximum total confidence
        int maxPos = 0;
        float maxConfidence = totalConfidences[0];
        for(int i = 1; i < totalConfidences.length; i++) {
            if (totalConfidences[i] > maxConfidence) {
                maxConfidence = totalConfidences[i];
                maxPos = i;
            }
        }

        // Find the index of the class with the second maximum total confidence
        int secondPos = 0;
        float secondConfidence = 0;
        for(int i = 0; i < totalConfidences.length; i++) {
            if (i != maxPos && totalConfidences[i] > secondConfidence) {
                secondConfidence = totalConfidences[i];
                secondPos = i;
            }
        }

        // Set the result text to the most likely class
        result.setText(classes[maxPos]);

        // Build the confidence text
        StringBuilder sb = new StringBuilder();
        float average = totalConfidences[maxPos] * 100;
        if (average < 80) {
            float saverage = totalConfidences[secondPos] * 100;
            sb.append(String.format(Locale.US, "%.1f%%\n(%s -> %.1f%%)", average, classes[secondPos], saverage));
        } else {
            sb.append(String.format(Locale.US, "%.1f%%", average));
        }

        confidence.setText(sb.toString());
    }

    private void ssshowResults(TextView result, TextView confidence, Measurement m1, Measurement m2, Measurement m3){
        String[] classes = {"0", "1", "2", "3"};

        float[] confidences = m1.confidences;

        int maxPos = 0;
        int secondPos = 0;
        float maxConfidence = 0;
        float secondConfidence = 0;
        for(int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence) {
                secondConfidence = maxConfidence;
                secondPos = maxPos;
                maxConfidence = confidences[i];
                maxPos = i;
            } else if (confidences[i] > secondConfidence) {
                secondConfidence = confidences[i];
                secondPos = i;
            }
        }
        result.setText(classes[maxPos]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; i++) {
            if(i==maxPos){
                String second = "";
                float average = (m1.confidences[i] * 100 + m2.confidences[i] * 100 + m3.confidences[i] * 100)/3;
                if(average<80){
                    float saverage = (m1.confidences[secondPos] * 100 + m2.confidences[secondPos] * 100 + m3.confidences[secondPos] * 100)/3;
                    second = String.format(Locale.US, "(%s -> %.1f%%)", classes[secondPos], saverage);
                }
                sb.append(String.format(Locale.US, "%.1f%%\n %s", average, second));
            }
        }
        confidence.setText(sb.toString());
    }

    private void takeScreenshot() {
        Date now = new Date();

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
            String mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Reports/" + timeStamp+  ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}