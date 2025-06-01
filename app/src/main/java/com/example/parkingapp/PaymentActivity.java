package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale; // Keep Locale if you use String.format for other display

public class PaymentActivity extends AppCompatActivity{

    private TextView txtSummary, txtFinalCostDisplay, txtLocation, txtRemainingParkPoints;
    private Button btnBackToMain;

    private ParkingDatabase db; // For getting remaining park points

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        txtSummary = findViewById(R.id.txtSummary);
        txtFinalCostDisplay = findViewById(R.id.txtFinalCostDisplay);
        txtLocation = findViewById(R.id.txtLocation);
        txtRemainingParkPoints = findViewById(R.id.txtRemainingParkPoints);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        db = new ParkingDatabase(this); // Initialize database for fetching points

        // Get data from intent passed from TimerActivity
        Intent intent = getIntent();
        String totalTime = intent.getStringExtra("totalTime");
        String location = intent.getStringExtra("location");
        int costInPoints = intent.getIntExtra("costInPoints", 0); // *** RECEIVE costInPoints ***

        // Set values to views
        txtSummary.setText("Total Time Parked: " + totalTime);
        txtLocation.setText("Location: " + location);
        txtFinalCostDisplay.setText("Parking Cost: " + costInPoints + " Park Points"); // *** DISPLAY costInPoints ***

        // Display remaining Park Points
        updateRemainingParkPointsDisplay();

        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to MainActivity
                Intent mainIntent = new Intent(PaymentActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears activity stack
                startActivity(mainIntent);
                finish(); // Finish PaymentActivity
            }
        });
    }

    // Helper method to display remaining Park Points
    private void updateRemainingParkPointsDisplay() {
        int remainingPoints = db.getParkPoints();
        txtRemainingParkPoints.setText("Remaining Park Points: " + remainingPoints);
    }
}