package com.example.parkingapp; // Ensure this package matches your project's root package

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard); // You'll create this layout

        TextView adminWelcomeText = findViewById(R.id.adminWelcomeText);
        adminWelcomeText.setText("Welcome, Administrator!");

        // Here you would add buttons/functionality for admin tasks (R6, R7 etc.)
        // Example:
        // Button btnManageSpots = findViewById(R.id.btnManageSpots);
        // btnManageSpots.setOnClickListener(...)
    }
}