package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class UserStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_statistics);

        Button btnParkingSessions = findViewById(R.id.btn_parking_sessions);
        Button btnDurationStats = findViewById(R.id.btn_duration_stats);
        Button btnCostStats = findViewById(R.id.btn_cost_stats);

        btnParkingSessions.setOnClickListener(v -> {
            Intent intent = new Intent(UserStatisticsActivity.this, ParkingSessionsGraphActivity.class);
            startActivity(intent);
        });

        btnDurationStats.setOnClickListener(v -> {
            Intent intent = new Intent(UserStatisticsActivity.this, DurationGraphActivity.class);
            startActivity(intent);
        });

        btnCostStats.setOnClickListener(v -> {
            Intent intent = new Intent(UserStatisticsActivity.this, CostGraphActivity.class);
            startActivity(intent);
        });
    }
}