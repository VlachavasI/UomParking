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

    private TextView txtTime, txtFee, txtSpot, txtLicensePlate; // txtFee will now display points
    private Button btnEnd;

    private Handler timerHandler = new Handler();
    private long startTime = 0L;
    private boolean isRunning = false;

    // No longer need hourlyRate if cost is strictly based on points from MainActivity
    // private final double hourlyRate = 2.00;

    private String currentPlate;
    private String currentLocation;
    private int initialPointsNeeded; // To store points passed from MainActivity
    private ParkingDatabase db;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            txtTime.setText(time);

            // Display the points needed (which were calculated in MainActivity)
            txtFee.setText("Cost: " + initialPointsNeeded + " Park Points");

            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        txtTime = findViewById(R.id.txtTime);
        txtFee = findViewById(R.id.txtFee);
        txtSpot = findViewById(R.id.txtSpot);
        txtLicensePlate = findViewById(R.id.txtLicensePlate);
        btnEnd = findViewById(R.id.btnEnd);

        db = new ParkingDatabase(this);

        Intent intent = getIntent();
        currentPlate = intent.getStringExtra("plate");
        currentLocation = intent.getStringExtra("location");
        initialPointsNeeded = intent.getIntExtra("pointsNeeded", 0); // *** RECEIVE pointsNeeded ***

        if (txtSpot != null) {
            txtSpot.setText("Spot: " + currentLocation);
        }
        if (txtLicensePlate != null) {
            txtLicensePlate.setText("License Plate: " + currentPlate);
        }

        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
        isRunning = true;

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerHandler.removeCallbacks(timerRunnable);
                isRunning = false;

                // End the parking session in the database
                if (currentPlate != null) {
                    if (db.endParkingSession(currentPlate)) {
                        Toast.makeText(TimerActivity.this, "Συνεδρία στάθμευσης ολοκληρώθηκε για " + currentPlate + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TimerActivity.this, "Σφάλμα ολοκλήρωσης συνεδρίας για " + currentPlate + ". Δεν βρέθηκε ενεργή συνεδρία.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TimerActivity.this, "Σφάλμα: Δεν βρέθηκε πινακίδα οχήματος.", Toast.LENGTH_SHORT).show();
                }

                // Pass final summary data (including the fixed points cost) to PaymentActivity
                Intent paymentIntent = new Intent(TimerActivity.this, PaymentActivity.class);
                paymentIntent.putExtra("totalTime", txtTime.getText().toString());
                paymentIntent.putExtra("location", currentLocation);
                paymentIntent.putExtra("costInPoints", initialPointsNeeded); // *** PASS pointsNeeded as cost ***

                startActivity(paymentIntent);

                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}