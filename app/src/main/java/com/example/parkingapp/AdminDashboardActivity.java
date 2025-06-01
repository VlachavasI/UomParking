package com.example.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Remove this import if no other toasts are used here
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageParkingAreas;
    private Button btnViewStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView adminWelcomeText = findViewById(R.id.adminWelcomeText);
        adminWelcomeText.setText("Welcome, Administrator!");

        btnManageParkingAreas = findViewById(R.id.btnManageParkingAreas);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);

        // Set up click listener for R6: Manage Parking Areas
        btnManageParkingAreas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ParkingAreaManagementActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for R7: View Statistics
        btnViewStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, StatisticsActivity.class); // Uncomment this line
                startActivity(intent); // Uncomment this line
                // Toast.makeText(AdminDashboardActivity.this, "Statistics feature coming soon!", Toast.LENGTH_SHORT).show(); // Remove or comment this line
            }
        });
    }
}