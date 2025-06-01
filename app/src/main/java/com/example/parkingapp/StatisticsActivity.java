package com.example.parkingapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StatisticsActivity extends AppCompatActivity {

    private TextView textViewTotalCarsParked;
    private TextView textViewTotalRevenue;
    private TextView textViewMostPopularLocation;
    private TextView textViewAverageDuration;

    private ParkingDatabase db; // We'll use this to fetch statistics

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        textViewTotalCarsParked = findViewById(R.id.textViewTotalCarsParked);
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue);
        textViewMostPopularLocation = findViewById(R.id.textViewMostPopularLocation);
        textViewAverageDuration = findViewById(R.id.textViewAverageDuration);

        db = new ParkingDatabase(this);

        // Call a method to load and display statistics
        loadStatistics();
    }

    private void loadStatistics() {
        // Placeholder values for now. We will replace these with actual database calls.
        textViewTotalCarsParked.setText("Total Parking Sessions: N/A");
        textViewTotalRevenue.setText("Total Revenue (PPs): N/A");
        textViewMostPopularLocation.setText("Most Popular Location: N/A");
        textViewAverageDuration.setText("Average Parking Duration: N/A");
    }
}