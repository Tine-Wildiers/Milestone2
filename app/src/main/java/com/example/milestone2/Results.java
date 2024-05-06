package com.example.milestone2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        float[] confidences = m1.confidences;

        int maxPos = 0;
        float maxConfidence = 0;
        for(int i = 0; i < confidences.length; i++){
            if(confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }

        result.setText(classes[maxPos]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; i++) {
            sb.append(String.format(Locale.US, "%s: %.1f%%, %.1f%%, %.1f%%\n", classes[i], m1.confidences[i] * 100,m2.confidences[i] * 100, m3.confidences[i] * 100));

        }
        confidence.setText(sb.toString());
    }
}