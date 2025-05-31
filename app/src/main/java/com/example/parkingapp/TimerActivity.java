package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private TextView txtTime, txtFee, txtSpot, txtLicensePlate; // Added txtLicensePlate and txtSpot
    private Button btnEnd;

    private Handler timerHandler = new Handler();
    private long startTime = 0L;
    private long targetDurationMillis = 0L; // To store the parking duration in milliseconds
    private boolean isRunning = false;

    // Set your hourly rate here
    private final double hourlyRate = 2.00;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;

            // Check if the target duration has been reached
            if (targetDurationMillis > 0 && millis >= targetDurationMillis) {
                // Timer reached the end
                timerHandler.removeCallbacks(this);
                isRunning = false;
                updateTimeAndFee(targetDurationMillis); // Update one last time with the final duration
                Toast.makeText(TimerActivity.this, "Parking session ended automatically.", Toast.LENGTH_LONG).show();
                btnEnd.setText("Go to Payment"); // Change button text to indicate next step
                // Optionally, you might want to automatically navigate to PaymentActivity here
                // However, for user control, keeping the button is often better.
                return;
            }

            updateTimeAndFee(millis);
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        txtTime = findViewById(R.id.txtTime);
        txtFee = findViewById(R.id.txtFee);
        txtSpot = findViewById(R.id.txtSpot); // Assuming you have a TextView with id txtSpot in activity_timer.xml
        txtLicensePlate = findViewById(R.id.txtLicensePlate); // Assuming you have a TextView with id txtLicensePlate in activity_timer.xml
        btnEnd = findViewById(R.id.btnEnd);

        // Get data from Intent
        Intent intent = getIntent();
        String location = intent.getStringExtra("location");
        String durationStr = intent.getStringExtra("time");
        String licensePlate = intent.getStringExtra("license");

        // Display location and license plate
        if (location != null) {
            txtSpot.setText("Spot: " + location);
        }
        if (licensePlate != null) {
            txtLicensePlate.setText("License Plate: " + licensePlate);
        }

        // Parse the duration string to milliseconds
        targetDurationMillis = parseDurationToMillis(durationStr);

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
                if (targetDurationMillis > 0 && elapsedMillis > targetDurationMillis) {
                    elapsedMillis = targetDurationMillis; // Ensure elapsed time doesn't exceed planned duration if ended manually after auto-stop
                }

                double totalHours = elapsedMillis / 3600000.0;
                double finalCost = hourlyRate * totalHours;

                Intent paymentIntent = new Intent(TimerActivity.this, PaymentActivity.class);
                paymentIntent.putExtra("finalCost", finalCost);
                paymentIntent.putExtra("totalTime", txtTime.getText().toString());
                // Use the location retrieved from the intent, not from the TextView which might have "Spot: " prefix
                paymentIntent.putExtra("location", location);
                startActivity(paymentIntent);
                finish(); // Finish this activity so user can't go back to it
            }
        });
    }

    private void updateTimeAndFee(long millis) {
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
    }

    private long parseDurationToMillis(String durationStr) {
        if (durationStr == null) {
            return 0;
        }

        // Example durations: "30 λεπτά", "1 ώρα", "2 ώρες", "3 ώρες"
        if (durationStr.contains("λεπτά")) {
            String minutesStr = durationStr.replace(" λεπτά", "").trim();
            try {
                return Long.parseLong(minutesStr) * 60 * 1000;
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (durationStr.contains("ώρα")) { // Handles "1 ώρα" and "2 ώρες"
            String hoursStr = durationStr.replace(" ώρα", "").replace(" ώρες", "").trim();
            try {
                return Long.parseLong(hoursStr) * 60 * 60 * 1000;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0; // Default to 0 if duration string is not recognized
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks when the activity is destroyed
        timerHandler.removeCallbacks(timerRunnable);
    }
}