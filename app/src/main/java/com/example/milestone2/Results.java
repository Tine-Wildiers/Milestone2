package com.example.milestone2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    }
}