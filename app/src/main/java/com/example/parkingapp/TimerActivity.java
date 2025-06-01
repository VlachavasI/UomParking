package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private TextView txtTime, txtFee;
    private Button btnEnd;

    private Handler timerHandler = new Handler();
    private long startTime = 0L;
    private boolean isRunning = false;

    // Set your hourly rate here
    private final double hourlyRate = 2.00;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            // Update time display
            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            txtTime.setText(time);

            // Calculate fee
            double totalHours = (millis / 3600000.0); // milliseconds to hours
            double cost = hourlyRate * totalHours;
            txtFee.setText(String.format(Locale.getDefault(), "Estimated Cost: $%.2f", cost));

            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        txtTime = findViewById(R.id.txtTime);
        txtFee = findViewById(R.id.txtFee);
        btnEnd = findViewById(R.id.btnEnd);

        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
        isRunning = true;

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop timer and go to payment
                timerHandler.removeCallbacks(timerRunnable);
                isRunning = false;

                // Pass data to payment screen
                long elapsedMillis = System.currentTimeMillis() - startTime;
                double totalHours = elapsedMillis / 3600000.0;
                double finalCost = hourlyRate * totalHours;
                String fullSpotText = ((TextView) findViewById(R.id.txtSpot)).getText().toString();
                String location = fullSpotText.replace("Spot: ", "");

                Intent intent = new Intent(TimerActivity.this, PaymentActivity.class);
                intent.putExtra("finalCost", finalCost);
                intent.putExtra("totalTime", txtTime.getText().toString());
                intent.putExtra("location", location);
                startActivity(intent);
            }
        });
    }
}