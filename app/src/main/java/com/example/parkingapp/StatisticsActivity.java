package com.example.parkingapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StatisticsActivity extends AppCompatActivity {

    private TextView textViewTotalCarsParked;
    private TextView textViewTotalRevenue;
    private TextView textViewMostPopularLocation;
    private TextView textViewAverageDuration;

    private ParkingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        textViewTotalCarsParked = findViewById(R.id.textViewTotalCarsParked);
        textViewTotalRevenue = findViewById(R.id.textViewTotalRevenue);
        textViewMostPopularLocation = findViewById(R.id.textViewMostPopularLocation);
        textViewAverageDuration = findViewById(R.id.textViewAverageDuration);

        db = new ParkingDatabase(this);

        loadStatistics();
    }

    private void loadStatistics() {
        // Fetch data from ParkingDatabase and update TextViews
        int totalSessions = db.getTotalParkingSessions();
        textViewTotalCarsParked.setText("Total Parking Sessions: " + totalSessions);

        double totalRevenue = db.getTotalRevenue();
        textViewTotalRevenue.setText(String.format("Total Revenue (PPs): %.2f", totalRevenue));

        String mostPopularLocation = db.getMostPopularLocation();
        textViewMostPopularLocation.setText("Most Popular Location: " + mostPopularLocation);

        String averageDuration = db.getAverageParkingDuration();
        textViewAverageDuration.setText("Average Parking Duration: " + averageDuration);
    }
}