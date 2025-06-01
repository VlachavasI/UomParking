package com.example.parkingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView; // Important: Added this import for TextView

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editPlate;
    Spinner editLocation;
    Spinner editDuration;
    Button btnStart;
    Button btnGoToAddPoints;
    Button btnGoToAdminLogin; // New button for admin login
    private ParkingDatabase db;
    private TextView textCurrentParkPoints; // Declare TextView for points display

    // Hardcoded parking point rules
    private static final int COST_PER_HOUR_PP = 10;
    private static final int COST_PER_30_MIN_PP = 5; // 30 minutes is half an hour


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPlate = findViewById(R.id.editPlate);
        editLocation = findViewById(R.id.editLocation);
        editDuration = findViewById(R.id.editDuration);
        btnStart = findViewById(R.id.btnStart);
        btnGoToAddPoints = findViewById(R.id.btnGoToAddPoints);
        btnGoToAdminLogin = findViewById(R.id.btnGoToAdminLogin); // Initialize the new button
        textCurrentParkPoints = findViewById(R.id.textCurrentParkPoints); // Initialize TextView

        db = new ParkingDatabase(this);

        String[] locations = {
                "Επιλέξτε τοποθεσία",
                "Κέντρο",
                "Βενιζέλου",
                "Παπάφη",
                "Νέα Ελβετία"
        };

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editLocation.setAdapter(locationAdapter);

        String[] durations = {
                "Επιλέξτε διάρκεια",
                "30 λεπτά",
                "1 ώρα",
                "2 ώρες",
                "3 ώρες"
        };

        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDuration.setAdapter(durationAdapter);

        // Initial update of park points display when activity starts
        updateParkPointsDisplay();


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plate = editPlate.getText().toString().trim();
                String location = editLocation.getSelectedItem().toString();
                String duration = editDuration.getSelectedItem().toString();

                // 1. Input validation
                if (plate.isEmpty() || location.equals("Επιλέξτε τοποθεσία") || duration.equals("Επιλέξτε διάρκεια")) {
                    Toast.makeText(MainActivity.this, "Συμπληρώστε όλα τα πεδία!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Calculate parking points needed
                int pointsNeeded = 0;
                switch (duration) {
                    case "30 λεπτά":
                        pointsNeeded = COST_PER_30_MIN_PP;
                        break;
                    case "1 ώρα":
                        pointsNeeded = COST_PER_HOUR_PP;
                        break;
                    case "2 ώρες":
                        pointsNeeded = COST_PER_HOUR_PP * 2;
                        break;
                    case "3 ώρες":
                        pointsNeeded = COST_PER_HOUR_PP * 3;
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Επιλέξτε έγκυρη διάρκεια!", Toast.LENGTH_SHORT).show();
                        return;
                }

                // 3. Check if plate is already parked to prevent UNIQUE constraint violation
                if (db.isPlateParked(plate)) {
                    Toast.makeText(MainActivity.this, "Το όχημα με αυτή την πινακίδα είναι ήδη σταθμευμένο!", Toast.LENGTH_LONG).show();
                    return;
                }

                // 4. Check if user has enough points
                int currentPoints = db.getParkPoints();
                if (currentPoints < pointsNeeded) {
                    Toast.makeText(MainActivity.this, "Δεν έχετε αρκετούς Park Points! Απαιτούνται: " + pointsNeeded + ", Διαθέσιμα: " + currentPoints, Toast.LENGTH_LONG).show();
                    return;
                }

                // 5. If all checks pass, attempt to insert parking session first
                boolean sessionInserted = db.insertParkingSession(plate, location, duration);

                if (sessionInserted) {
                    // 6. If session inserted successfully, then deduct points
                    boolean pointsDeducted = db.deductParkPoints(pointsNeeded);

                    if (pointsDeducted) {
                        Toast.makeText(MainActivity.this, "Συνεδρία στάθμευσης καταχωρήθηκε και " + pointsNeeded + " Park Points αφαιρέθηκαν!", Toast.LENGTH_LONG).show();
                        // Clear fields after successful insertion and deduction
                        editPlate.setText("");
                        editLocation.setSelection(0); // Reset spinner
                        editDuration.setSelection(0); // Reset spinner
                    } else {
                        // This case should ideally not happen if deductParkPoints logic is correct.
                        // If points deduction fails AFTER session insertion, it's an inconsistent state.
                        // You might want to add logic to revert the session insertion or log this error.
                        Toast.makeText(MainActivity.this, "Συνεδρία καταχωρήθηκε, αλλά υπήρξε σφάλμα στην αφαίρεση Park Points.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // This else block should theoretically not be reached because `isPlateParked`
                    // check is performed beforehand. It's here for robustness.
                    Toast.makeText(MainActivity.this, "Αποτυχία καταχώρησης συνεδρίας.", Toast.LENGTH_SHORT).show();
                }

                // Always update Park Points display after any attempt to start a session
                updateParkPointsDisplay();
            }
        });

        btnGoToAddPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPointsActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the admin login button
        btnGoToAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class); // Will navigate to Admin Login Activity
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update points display every time MainActivity becomes active (e.g., returning from AddPointsActivity)
        updateParkPointsDisplay();
    }

    // Method to update the displayed Park Points
    private void updateParkPointsDisplay() {
        int points = db.getParkPoints();
        textCurrentParkPoints.setText("Διαθέσιμοι Park Points: " + points);
    }
}